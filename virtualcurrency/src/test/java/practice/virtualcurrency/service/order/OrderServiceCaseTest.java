package practice.virtualcurrency.service.order;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;
import practice.virtualcurrency.exception.InsufficientCashException;
import practice.virtualcurrency.repository.member.MemberRepository;
import practice.virtualcurrency.repository.order.OrderRepository;
import practice.virtualcurrency.service.member.MemberService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@Slf4j
class OrderServiceCaseTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderRepository orderRepository;

    private Member buyMember1, buyMember2, sellMember1,sellMember2;

    @BeforeEach
    void memberInit(){
        buyMember1 = new Member("buyMember1", "test123");
        buyMember1.getWallet().put("KRW",100000.0);
        memberService.join(buyMember1);
        buyMember2 = new Member("buyMember2", "test123");
        buyMember2.getWallet().put("KRW",100000.0);
        memberService.join(buyMember2);
        sellMember1 = new Member("sellMember1", "test123");
        sellMember1.getWallet().put("KRW",100000.0);
        memberService.join(sellMember1);
        sellMember2 = new Member("sellMember2", "test123");
        sellMember2.getWallet().put("KRW",100000.0);
        memberService.join(sellMember2);
    }
    @AfterEach
    void stateReset(){
        memberRepository.deleteAll();
        orderRepository.deleteAll();
        orderService.clearOrderBook();
    }

    @Test
    void simpleDesignatedPriceTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0,1.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0,1.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0,1.0);

        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,5000.0,1.0);
        orderService.sellDesignatedPrice(sellMember2,"BTC",200.0,5000.0,1.0);
        orderService.sellDesignatedPrice(sellMember2,"BTC",100.0,15000.0,1.0);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void simpleMarketPriceTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0,1.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0,1.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0,1.0);

        orderService.sellMarketPrice(sellMember1,"BTC",0.0,5000.0,1.0);
        orderService.sellMarketPrice(sellMember2,"BTC",0.0,20000.0,1.0);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }
    //Member(Total,leverage)
    //----------------Init Order Book(Buy)-----------------
    //| 200 : B1(20000,2.0)                               |
    //| 100 : B1(10000,1.0)->B2(30000,3.0)                |
    //-----------------------------------------------------
    //----------------Request Order(Sell)------------------
    //| 200 : S1(5000,1.0)->S2(20000,4.0)                 |
    //| 100 : S2(15000,1.0)                               |
    //-----------------------------------------------------
    @Test
    void leverageTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0,1.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0,3.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0,2.0);

        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,5000.0,1.0);
        orderService.sellDesignatedPrice(sellMember2,"BTC",200.0,5000.0,4.0);
        orderService.sellDesignatedPrice(sellMember2,"BTC",100.0,15000.0,1.0);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(200.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-225.0, 80000.0);

        printResult();
    }
    //Member(Total,leverage)
    //----------------Init Order Book(Buy)-----------------
    //| 300 : B1(50000,5.0)                               |
    //| 200 : B1(30000,3.0)                               |
    //| 100 : B2(40000,4.0)                               |
    //-----------------------------------------------------
    //----------------Request Order(Sell)------------------
    //| 200 : S1(5000,1.0)->S2(20000,4.0)                 |
    //| 100 : S2(15000,1.0)                               |
    //-----------------------------------------------------
    @SneakyThrows
    @Test
    void liquidationTest() {
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0,4.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0,3.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",300.0,10000.0,5.0);

        Thread.sleep(1000);
        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,3.0);
        Thread.sleep(1000);
        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,3.0);
        Order testOrder = buyMember1.getOrders().stream()
                .filter(o -> o.getCoinName().equals("BTC"))
                .findFirst()
                .get();
        log.info("price : {}",testOrder.getPrice());
        Thread.sleep(1000);
        Order testOrder2 = buyMember1.getOrders().stream()
                .filter(o -> o.getCoinName().equals("BTC"))
                .findFirst()
                .get();
        log.info("price : {}",testOrder2.getPrice());
        //printResult();
    }
    //Member(Total)
    //----------------Init Order Book(Buy)-----------------
    //| 200 : M1(8000)->M2(6000)->M1(4000)                |
    //| 100 : M1(10000)->M2(9000)->M1(7000)->M1(5000)     |
    //-----------------------------------------------------
    @Test
    void ComplicatedTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0,1.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,9000.0,1.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,8000.0,1.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,7000.0,1.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,6000.0,1.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,5000.0,1.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,4000.0,1.0);
        //-----------------------------------------------------
        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,11000.0,1.0);

        orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,3000.0,1.0);

        orderService.sellMarketPrice(sellMember2,"BTC",0.0,8000.0,1.0);
        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,5000.0,1.0);
        orderService.sellMarketPrice(sellMember1,"BTC",0.0,25000.0,1.0);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(220.0, 66000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(135.0, 82000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-315.0, 59000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-40.0, 92000.0);

        printResult();
    }

    private static List<Double> getCoinAndCash(Member member) {
        List<Double> coinAndCash = new ArrayList<>();
        Double coinQuantity = member.getWallet().get("BTC");
        Double cashQuantity = member.getWallet().get("KRW");

        coinAndCash.add(coinQuantity);
        coinAndCash.add(cashQuantity);

        return coinAndCash;
    }

    private void printResult() {
        orderService.printMap(State.BUY,"BTC");
        orderService.printMap(State.SELL,"BTC");

        memberService.printWallet(buyMember1);
        memberService.printWallet(buyMember2);
        memberService.printWallet(sellMember1);
        memberService.printWallet(sellMember2);

        memberService.printOrder(buyMember1);
        memberService.printOrder(buyMember2);
        memberService.printOrder(sellMember1);
        memberService.printOrder(sellMember2);
    }
}