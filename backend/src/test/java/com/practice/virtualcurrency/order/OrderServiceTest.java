package com.practice.virtualcurrency.order;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Order;
import com.practice.virtualcurrency.domain.order.OrderBook;
import com.practice.virtualcurrency.domain.order.State;
import com.practice.virtualcurrency.domain.order.Trade;
import com.practice.virtualcurrency.dto.command.OrderCommand;
import com.practice.virtualcurrency.repository.member.MemberRepository;
import com.practice.virtualcurrency.repository.order.OrderRepository;
import com.practice.virtualcurrency.service.member.MemberService;
import com.practice.virtualcurrency.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @Autowired
    private OrderBook orderBook;

    private Member buyMember1, buyMember2, sellMember1,sellMember2;

    @BeforeEach
    void memberInit(){
        buyMember1 = new Member("buyMember1", "temp","test123");
        buyMember1.getWallet().put("USDT",100000.0);
        memberService.join(buyMember1);
        buyMember2 = new Member("buyMember2", "temp","test123");
        buyMember2.getWallet().put("USDT",100000.0);
        memberService.join(buyMember2);
        sellMember1 = new Member("sellMember1","temp", "test123");
        sellMember1.getWallet().put("USDT",100000.0);
        memberService.join(sellMember1);
        sellMember2 = new Member("sellMember2","temp", "test123");
        sellMember2.getWallet().put("USDT",100000.0);
        memberService.join(sellMember2);
    }
    @AfterEach
    void stateReset(){
        memberRepository.deleteAll();
        orderRepository.deleteAll();
        orderBook.clear();
    }

    @Test
    void test(){
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",100.0,50000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",200.0,50000.0,0.0,1.0, Trade.PRCIE));

        orderService.buyMarketPrice(new OrderCommand(buyMember1,"BTC",0.0,80000.0,0.0,1.0, Trade.PRCIE));

        printResult();
    }

    @Test
    void simpleDesignatedPriceTest() {
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",100.0,10000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2,"BTC",100.0,10000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",200.0,10000.0,0.0,1.0, Trade.PRCIE));

        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",200.0,5000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",200.0,4000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",300.0,30000.0,0.0,1.0, Trade.PRCIE));

        printResult();
    }

    @Test
    void simpleDesignatedPriceTest2() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",100.0,10000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",100.0,10000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",200.0,10000.0,0.0,1.0, Trade.PRCIE));

        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",200.0,5000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2,"BTC",200.0,4000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2,"BTC",100.0,30000.0,0.0,1.0, Trade.PRCIE));

        printResult();
    }


    @Test
    void simpleDesignatedPriceTest3() {
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",100.0,10000.0,100.0,1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2,"BTC",100.0,10000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",200.0,10000.0,50.0,1.0, Trade.QUANTITY));

        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",200.0,5000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",200.0,4000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",200.0,0.0,300.0,1.0, Trade.QUANTITY));

        printResult();
    }

    @Test
    void simpleDesignatedPriceTest4() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",100.0,0.0,100.0,1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",100.0,10000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",200.0,0.0,50.0,1.0, Trade.QUANTITY));

        orderService.sellDesignatedPrice(new OrderCommand(sellMember1,"BTC",200.0,5000.0,0.0,1.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(sellMember2,"BTC",0.0,4000.0,0.0,2.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2,"BTC",100.0,0.0,300.0,1.0, Trade.QUANTITY));

        printResult();
    }

    @Test
    void calcelOrderTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1,"BTC",200.0,8000.0,0.0,1.0, Trade.PRCIE));
        Order cancelOrder = orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 6000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 200.0, 4000.0,0.0,1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2,"BTC",200.0,2000.0,10.0,1.0, Trade.QUANTITY));
        //-----------------------------------------------------
        orderService.cancelOrder(cancelOrder);
        orderService.sellMarketPrice(new OrderCommand(sellMember1,"BTC",0.0,10000.0,0.0,1.0, Trade.PRCIE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(40.0, 92000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(10.0, 94000.0);

        printResult();
    }

    @Test
    void simpleDesignatedPriceByQuantityTest() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 0.0, 50.0, 1.0, Trade.QUANTITY));

        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 100.0, 0.0, 150.0, 1.0, Trade.QUANTITY));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void simpleMarketPriceTest() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 10000.0, 0.0, 1.0, Trade.PRCIE));

        orderService.sellMarketPrice(new OrderCommand(sellMember1, "BTC", 0.0, 5000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(sellMember2, "BTC", 0.0, 20000.0, 0.0, 1.0, Trade.PRCIE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void leverageTest() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 100.0, 10000.0, 0.0, 3.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 10000.0, 0.0, 2.0, Trade.PRCIE));

        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 200.0, 5000.0, 0.0, 4.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 100.0, 15000.0, 0.0, 1.0, Trade.PRCIE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(200.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-225.0, 80000.0);

        printResult();
    }

    @Test
    void leverageTestByQuantity() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 100.0, 0.0, 300.0, 3.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 0.0, 100.0, 2.0, Trade.QUANTITY));

        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 200.0, 0.0, 100.0, 4.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 100.0, 0.0, 150.0, 1.0, Trade.QUANTITY));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(200.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-225.0, 80000.0);

        printResult();
    }

    @Test
    void ComplicatedTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 100.0, 9000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 8000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 7000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 200.0, 6000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 5000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 4000.0, 0.0, 1.0, Trade.PRCIE));
        //-----------------------------------------------------
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 11000.0, 0.0, 1.0, Trade.PRCIE));

        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 200.0, 3000.0, 0.0, 1.0, Trade.PRCIE));

        orderService.sellMarketPrice(new OrderCommand(sellMember2, "BTC", 0.0, 8000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(sellMember1, "BTC", 0.0, 25000.0, 0.0, 1.0, Trade.PRCIE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(220.0, 66000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(135.0, 82000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-315.0, 59000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-40.0, 92000.0);

        printResult();
    }
    @Test
    void ComplicatedTest2() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 150.0, 9000.0, 0.0, 2.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 8000.0, 0.0, 3.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 11000.0, 0.0, 2.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 7000.0, 0.0, 4.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 200.0, 6000.0, 0.0, 3.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 100.0, 11000.0, 0.0, 2.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(buyMember2, "BTC", 100.0, 0.0, 30.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 4000.0, 0.0, 3.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(sellMember2, "BTC", 0.0, 8000.0, 0.0, 4.0, Trade.PRCIE));

        /**
         * 현재 market 으로 quantity로 하면 돈이 안빠져나가기때문에 오류가났음(밑에서 3번째줄)
         */
        assertThat(getCoinAndCash(buyMember1)).containsExactly(385.0, 62000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(60.0, 94000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-110.0, 89000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-335.0, 81000.0);

        printResult();
    }

    @Test
    void ComplicatedTest2ByQuantity() {
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 150.0, 0.0, 120.0, 2.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 8000.0, 0.0, 3.0, Trade.PRCIE));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember1, "BTC", 200.0, 0.0, 110.0, 2.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 100.0, 7000.0, 0.0, 4.0, Trade.PRCIE));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember2, "BTC", 200.0, 0.0, 90.0, 3.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(new OrderCommand(sellMember2, "BTC", 100.0, 11000.0, 0.0, 2.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(buyMember2, "BTC", 100.0, 0.0, 30.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(new OrderCommand(buyMember1, "BTC", 200.0, 4000.0, 0.0, 3.0, Trade.PRCIE));
        orderService.sellMarketPrice(new OrderCommand(sellMember2, "BTC", 0.0, 8000.0, 0.0, 4.0, Trade.PRCIE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(385.0, 62000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(60.0, 94000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-110.0, 89000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-335.0, 81000.0);

        printResult();
    }




    private static List<Double> getCoinAndCash(Member member) {
        List<Double> coinAndCash = new ArrayList<>();
        Double coinQuantity = member.getWallet().get("BTC");
        Double cashQuantity = member.getWallet().get("USDT");

        coinAndCash.add(coinQuantity);
        coinAndCash.add(cashQuantity);

        return coinAndCash;
    }

    private void printResult() {
        orderBook.printMap(State.BUY,"BTC");
        orderBook.printMap(State.SELL,"BTC");
    }
}