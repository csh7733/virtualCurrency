package practice.virtualcurrency.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import practice.virtualcurrency.test.domain.TestItem;
import practice.virtualcurrency.test.repository.TestRepository;
import practice.virtualcurrency.test.service.TestService;

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {
    private final TestService testService;

    /**
     * 확인용 초기 데이터 추가
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("test data init");
        testService.save(new TestItem("비트코인2",500L));
        testService.save(new TestItem("이더리움",200L));
    }

}
