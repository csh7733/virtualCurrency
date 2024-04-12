package practice.virtualcurrency.service.order;

import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.domain.coin.Coin;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.State;
import practice.virtualcurrency.exception.InsufficientCashException;
import practice.virtualcurrency.repository.member.MemberRepository;
import practice.virtualcurrency.repository.order.OrderRepository;
import practice.virtualcurrency.service.member.MemberService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@SpringBootTest
@Slf4j
class OrderServiceTest {

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
    void memberReset(){
        memberRepository.deleteAll();
        orderRepository.deleteAll();
        orderService.clearOrderBook();
    }

    @Test
    void simpleDesignatedPriceTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0);

        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,5000.0);
        orderService.sellDesignatedPrice(sellMember2,"BTC",200.0,5000.0);
        orderService.sellDesignatedPrice(sellMember2,"BTC",100.0,15000.0);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void simpleMarketPriceTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0);

        orderService.sellMarketPrice(sellMember1,"BTC",0.0,5000.0);
        orderService.sellMarketPrice(sellMember2,"BTC",0.0,20000.0);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void insufficientCashExceptionTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,50000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,50000.0);
        assertThat(getCoinAndCash(buyMember1)).containsExactly(0.0, 0.0);

        assertThatThrownBy(() -> {
            orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,50000.0);
            orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,60000.0);
        }).isInstanceOf(InsufficientCashException.class);

        printResult();
    }

    //----------------Init Order Book(Buy)-----------------
    //| 200 : M1(8000)->M2(6000)->M1(4000)                |
    //| 100 : M1(10000)->M2(9000)->M1(7000)->M1(5000)     |
    //-----------------------------------------------------
    @Test
    void ComplicatedTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,10000.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,9000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,8000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,7000.0);
        orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,6000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,5000.0);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,4000.0);
        //-----------------------------------------------------
        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,11000.0);

        orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,3000.0);

        orderService.sellMarketPrice(sellMember2,"BTC",0.0,8000.0);
        orderService.sellDesignatedPrice(sellMember1,"BTC",200.0,5000.0);
        orderService.sellMarketPrice(sellMember1,"BTC",0.0,25000.0);

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