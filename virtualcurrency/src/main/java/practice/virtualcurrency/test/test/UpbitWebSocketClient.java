package practice.virtualcurrency.test.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import practice.virtualcurrency.dto.UpbitCoinDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

//@Component
@Slf4j
public class UpbitWebSocketClient extends WebSocketClient {

    //@Autowired
    private SimpMessagingTemplate template;
    //@Autowired
    private ObjectMapper objectMapper;

    public UpbitWebSocketClient() throws URISyntaxException {
        // WebSocketClient의 생성자에 null을 전달. 실제 URI 설정은 나중에 수행
        super(new URI("wss://api.upbit.com/websocket/v1"));
    }

    @PostConstruct
    public void init() {
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected");
        // 연결 후 데이터 요청
        JSONObject ticketObj = new JSONObject();
        ticketObj.put("ticket", "test");

        JSONObject typeObj = new JSONObject();
        typeObj.put("type", "ticker");
        typeObj.put("codes", new JSONArray(new String[]{"KRW-BTC"}));

        JSONArray requestArray = new JSONArray();
        requestArray.put(ticketObj);
        requestArray.put(typeObj);

        this.send(requestArray.toString());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received string message: " + message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] byteData = new byte[bytes.remaining()];
        bytes.get(byteData);  // ByteBuffer를 byte 배열로 변환
        // byteData를 처리하는 로직 추가
        // 예: 문자열로 변환하여 출력
        String message = new String(byteData, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(message);
        try {
            UpbitCoinDTO upbitCoinDTO = objectMapper.readValue(message, UpbitCoinDTO.class);
            long price = upbitCoinDTO.getTradePrice().longValue();
            String formattedNumber = String.format("%,d", price);
            template.convertAndSend("/coin/price", formattedNumber);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //Double tradePrice = jsonObject.getDouble("trade_price");
        //System.out.println("Bitcoin Price is : " + "[" + formattedNumber + "]");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred:" + ex);
    }

    @PreDestroy
    public void onClose() {
        this.close();
        System.out.println("WebSocket closed");
    }
}
