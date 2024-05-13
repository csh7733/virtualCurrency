package practice.virtualcurrency.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @Autowired
    private SimpMessagingTemplate template;

    @GetMapping("/test")
    public String test(){
        updatePrice("500");
        return "test/test";
    }

    // 실제 환경에서는 외부 거래소로부터 가격 정보를 받는 로직이 필요함
    public void updatePrice(String price) {
        // "/topic/price" 주소로 구독 중인 클라이언트에게 가격 정보를 전송
        template.convertAndSend("/topic/price", price);
    }
}
