package practice.virtualcurrency.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import practice.virtualcurrency.VirtualCurrencyConst;
import practice.virtualcurrency.dto.UpbitCoinDTO;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static practice.virtualcurrency.DataInit.CRYPTO_MAP;

@Component
@Slf4j
public class UpbitWebSocketClient extends WebSocketClient {

    private final SimpMessagingTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public UpbitWebSocketClient(SimpMessagingTemplate template, ObjectMapper objectMapper) throws URISyntaxException {
        super(new URI(VirtualCurrencyConst.UPBIT_URL));
        this.template = template;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        JSONObject ticketObj = new JSONObject();
        ticketObj.put("ticket", "test");

        JSONObject typeObj = new JSONObject();
        typeObj.put("type", "ticker");
        Object[] codesArray = CRYPTO_MAP.keySet().toArray();
        typeObj.put("codes", new JSONArray(Arrays.toString(codesArray)));

        JSONArray requestArray = new JSONArray();
        requestArray.put(ticketObj);
        requestArray.put(typeObj);

        this.send(requestArray.toString());
    }

    @Override
    public void onMessage(String message) {
        log.info("Received string message: " + message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] byteData = new byte[bytes.remaining()];
        bytes.get(byteData);
        String message = new String(byteData, StandardCharsets.UTF_8);
        try {
            UpbitCoinDTO upbitCoin = objectMapper.readValue(message, UpbitCoinDTO.class);
            upbitCoin.setName(CRYPTO_MAP.get(upbitCoin.getCode()));
            long price = upbitCoin.getTradePrice().longValue();
            upbitCoin.setPrice(price);
            //log.info("price = {}",price);
            if(upbitCoin.getName().equals("비트코인"))template.convertAndSend("/coin", upbitCoin);
        } catch (JsonProcessingException e) { //ToDo : Handle RunTimeException!
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("An error occurred:" + ex);
    }

    @PreDestroy
    public void onClose() {
        this.close();
    }
}
