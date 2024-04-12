package practice.virtualcurrency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.service.member.MemberService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DataInit {

    private final MemberService memberService;
    public static final Map<String, String> CRYPTO_MAP;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("Local DataInit Start");

        Optional<Member> admin = memberService.findMember("admin", "admin123");

        if(admin.isEmpty()){
            memberService.join(new Member("admin","admin123"));
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
