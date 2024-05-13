package practice.virtualcurrency.service.order;

import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;
import practice.virtualcurrency.domain.order.Trade;
import practice.virtualcurrency.exception.InsufficientCashException;
import practice.virtualcurrency.repository.member.MemberRepository;
import practice.virtualcurrency.repository.order.OrderRepository;
import practice.virtualcurrency.service.member.MemberService;

import java.util.ArrayList;
import java.util.List;

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
    void stateReset(){
        memberRepository.deleteAll();
        orderRepository.deleteAll();
        orderService.clearOrderBook();
    }

    @Test
    void insufficientCashExceptionTest() {
        orderService.buyDesignatedPrice(buyMember1,"BTC",100.0,50000.0,0.0,1.0, Trade.PRCIE);
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,50000.0,0.0,1.0, Trade.PRCIE);
        assertThat(getCoinAndCash(buyMember1)).containsExactly(0.0, 0.0);

        assertThatThrownBy(() -> {
            orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,50000.0,0.0,1.0, Trade.PRCIE);
            orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,60000.0,0.0,1.0, Trade.PRCIE);
        }).isInstanceOf(InsufficientCashException.class);

        printResult();
    }
    //Member(Total)
    //----------------Init Order Book(Buy)-----------------
    //| 200 : M1(8000)->M1(6000)->M2(4000)->M2(2000)      |
    //-----------------------------------------------------
    @Test
    void calcelOrderTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,8000.0,0.0,1.0, Trade.PRCIE);
        Order cancelOrder = orderService.buyDesignatedPrice(buyMember1, "BTC", 200.0, 6000.0,0.0,1.0, Trade.PRCIE);
        orderService.buyDesignatedPrice(buyMember2, "BTC", 200.0, 4000.0,0.0,1.0, Trade.PRCIE);
        orderService.buyDesignatedPrice(buyMember2,"BTC",200.0,2000.0,0.0,1.0, Trade.PRCIE);
        //-----------------------------------------------------
        orderService.cancelOrder(cancelOrder);
        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,0.0,1.0, Trade.PRCIE);

        assertThat(getCoinAndCash(buyMember1)).containsExactly(40.0, 92000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(10.0, 94000.0);

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