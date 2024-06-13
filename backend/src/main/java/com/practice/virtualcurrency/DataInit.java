package com.practice.virtualcurrency;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Order;
import com.practice.virtualcurrency.domain.order.OrderType;
import com.practice.virtualcurrency.domain.order.State;
import com.practice.virtualcurrency.service.member.MemberService;
import com.practice.virtualcurrency.service.order.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DataInit {

    private final MemberService memberService;
    public static final Map<String, String> CRYPTO_MAP;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        log.info("Local DataInit Start");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = now.format(formatter);

        Optional<Member> adminOptional = memberService.findMember("admin@gmail.com", "admin123");

        if(adminOptional.isEmpty()){
            Member admin = memberService.join(new Member("admin","admin@gmail.com", "admin123"));
            Order order = Order.builder()
                    .orderType(OrderType.LIMIT)
                    .member(admin)
                    .time(time)
                    .coinName("BTC")
                    .price(7333.33334)
                    .quantity(32.3232)
                    .state(State.SELL)
                    .leverage(121.0)
                    .build();
            admin.getWallet().put("USDT",12345.67);
            admin.getOrders().add(order);
        }



    }

    static {
        CRYPTO_MAP = new HashMap<>();
        CRYPTO_MAP.put("KRW-BTC", "비트코인");
        CRYPTO_MAP.put("KRW-ETH", "이더리움");
        CRYPTO_MAP.put("KRW-XRP", "리플");
        CRYPTO_MAP.put("KRW-DOGE", "도지코인");
        CRYPTO_MAP.put("KRW-ZRX", "제로엑스");
        CRYPTO_MAP.put("KRW-SOL", "솔라나");
    }
}
