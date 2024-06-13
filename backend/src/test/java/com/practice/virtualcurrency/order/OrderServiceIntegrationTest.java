package com.practice.virtualcurrency.order;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.*;
import com.practice.virtualcurrency.dto.command.OrderCommand;
import com.practice.virtualcurrency.exception.InsufficientCashException;
import com.practice.virtualcurrency.repository.member.MemberRepository;
import com.practice.virtualcurrency.repository.order.OrderRepository;
import com.practice.virtualcurrency.service.member.MemberService;
import com.practice.virtualcurrency.service.order.OrderService;
import lombok.SneakyThrows;
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
class OrderServiceIntegrationTest {

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
    private String buyMember1Username,buyMember2Username,sellMember1Username,sellMember2Username;

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

        buyMember1Username = buyMember1.getUsername();
        buyMember2Username = buyMember2.getUsername();
        sellMember1Username = sellMember1.getUsername();
        sellMember2Username = sellMember2.getUsername();
    }
    @AfterEach
    void stateReset(){
        memberRepository.deleteAll();
        orderRepository.deleteAll();
        orderBook.clear();
    }

    @Test
    void simpleDesignatedPriceTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 10000.0, 0.0, 1.0, Trade.PRICE));

        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 100.0, 15000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void simpleDesignatedPriceByQuantityTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 0.0, 50.0, 1.0, Trade.QUANTITY));

        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 100.0, 0.0, 150.0, 1.0, Trade.QUANTITY));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void emptyOrderBookTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 100.0, 10000.0, 0.0, 5.0, Trade.PRICE));

        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 50.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 50.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 10000.0, 0.0, 5.0, Trade.PRICE));

        assertThat(getCoinAndCash(sellMember1)).containsExactly(-200.0, 90000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-400.0, 96000.0);

        printResult();
    }

    @Test
    void simpleMarketPriceTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 10000.0, 0.0, 1.0, Trade.PRICE));

        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember1Username, "BTC", 0.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 20000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(150.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-175.0, 80000.0);

        printResult();
    }

    @Test
    void simpleMarketPriceByQuantityTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 0.0, 50.0, 1.0, Trade.QUANTITY));

        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember1Username, "BTC", 0.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 20000.0, 0.0, 1.0, Trade.PRICE));

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
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 10000.0, 0.0, 3.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 10000.0, 0.0, 2.0, Trade.PRICE));

        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 200.0, 5000.0, 0.0, 4.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 100.0, 15000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(200.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-225.0, 80000.0);

        printResult();
    }

    @Test
    void leverageTestByQuantity() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 0.0, 300.0, 3.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 0.0, 100.0, 2.0, Trade.QUANTITY));

        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 200.0, 0.0, 100.0, 4.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 100.0, 0.0, 150.0, 1.0, Trade.QUANTITY));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(200.0, 80000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-225.0, 80000.0);

        printResult();
    }

    @Test
    void sellingInTrancheTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 5000.0, 0.0, 2.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 5000.0, 0.0, 2.0, Trade.PRICE));

        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 5000.0, 0.0, 3.0, Trade.PRICE));

        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 300.0, 10000.0, 0.0, 2.0, Trade.PRICE));

        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, buyMember1Username, "BTC", 0.0, 0.0, 50.0, 1.0, Trade.QUANTITY));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(50.0, 105000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(50.0, 90000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-25.0, 95000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-75.0, 95000.0);

        printResult();
    }
    //Member(Total,leverage)
    //----------------Init Order Book(Buy)-----------------
    //| 300 : B1(50000,5.0)                               |
    //| 200 : B1(30000,3.0)                               |
    //| 100 : B2(40000,4.0)                               |
    //-----------------------------------------------------
    //----------------Request Order(Sell)------------------
    //| 300 : S1(30000,3.0)->S1(20000,3.0)                |
    //| 200 : S1(10000,3.0)                               |
    //-----------------------------------------------------
    //buyMember1's order = {price=276.9230769230769, quantity=216.66666666666669, total=60000.0 leverage=4.5,
    //liquidation price =215.38461538461536}
//    @SneakyThrows
//    @Test
//    void liquidationTest() {
//        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0,0.0,4.0, Trade.PRCIE);
//        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0,0.0,3.0, Trade.PRCIE);
//        orderService.buyDesignatedPrice(buyMember1,"BTC",300.0,10000.0,0.0,5.0, Trade.PRCIE);
//
//        Thread.sleep(1000);
//        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,0.0,3.0, Trade.PRCIE);
//        Thread.sleep(1000);
//        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,0.0,3.0, Trade.PRCIE);
//        Thread.sleep(1000);
//        //buyMember1 liquidation
//        assertThat(getCoinAndCash(buyMember1)).containsExactly(0.0, 80000.0);
//        printResult();
//    }
//    @SneakyThrows
//    @Test
//    void liquidationTestByQuantity() {
//        orderService.buyDesignatedPrice(buyMember2,"BTC",100.0,10000.0,400.0,4.0, Trade.QUANTITY);
//        orderService.buyDesignatedPrice(buyMember1,"BTC",200.0,10000.0,150.0,3.0, Trade.QUANTITY);
//        orderService.buyDesignatedPrice(buyMember1,"BTC",300.0,10000.0,0.0,5.0, Trade.PRCIE);
//
//        Thread.sleep(1000);
//        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,0.0,3.0, Trade.PRCIE);
//        Thread.sleep(1000);
//        orderService.sellMarketPrice(sellMember1,"BTC",0.0,10000.0,0.0,3.0, Trade.PRCIE);
//        Thread.sleep(1000);
//        //buyMember1 liquidation
//        assertThat(getCoinAndCash(buyMember1)).containsExactly(0.0, 80000.0);
//        printResult();
//    }

    //Member(Total)
    //----------------Init Order Book(Buy)-----------------
    //| 200 : M1(8000)->M2(6000)->M1(4000)                |
    //| 100 : M1(10000)->M2(9000)->M1(7000)->M1(5000)     |
    //-----------------------------------------------------
    @Test
    void ComplicatedTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 9000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 8000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 7000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 6000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 4000.0, 0.0, 1.0, Trade.PRICE));
        //-----------------------------------------------------
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 11000.0, 0.0, 1.0, Trade.PRICE));

        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 3000.0, 0.0, 1.0, Trade.PRICE));

        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 8000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 5000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember1Username, "BTC", 0.0, 25000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(220.0, 66000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(135.0, 82000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-315.0, 59000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-40.0, 92000.0);

        printResult();
    }

    @Test
    void ComplicatedTestByQuantity() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 0.0, 100.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 0.0, 90.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 0.0, 40.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 7000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 6000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 0.0, 50.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 4000.0, 0.0, 1.0, Trade.PRICE));
        //-----------------------------------------------------
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 11000.0, 0.0, 1.0, Trade.PRICE));

        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 3000.0, 0.0, 1.0, Trade.PRICE));

        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 8000.0, 0.0, 1.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 0.0, 25.0, 1.0, Trade.QUANTITY));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember1Username, "BTC", 0.0, 25000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(220.0, 66000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(135.0, 82000.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-315.0, 59000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-40.0, 92000.0);

        printResult();
    }

    @Test
    void ComplicatedTest2() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 150.0, 9000.0, 0.0, 2.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 8000.0, 0.0, 3.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 11000.0, 0.0, 2.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 7000.0, 0.0, 4.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 6000.0, 0.0, 3.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 100.0, 11000.0, 0.0, 2.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, buyMember2Username, "BTC", 100.0, 0.0, 30.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 4000.0, 0.0, 3.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 8000.0, 0.0, 4.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(385.0, 62000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(60.0, 92500.0);
        assertThat(getCoinAndCash(sellMember1)).containsExactly(-110.0, 89000.0);
        assertThat(getCoinAndCash(sellMember2)).containsExactly(-335.0, 81000.0);

        printResult();
    }

    @Test
    void ComplicatedTest2ByQuantity() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 10000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 150.0, 0.0, 120.0, 2.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 8000.0, 0.0, 3.0, Trade.PRICE));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember1Username, "BTC", 200.0, 0.0, 110.0, 2.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 7000.0, 0.0, 4.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 0.0, 90.0, 3.0, Trade.QUANTITY));
        orderService.sellDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, sellMember2Username, "BTC", 100.0, 11000.0, 0.0, 2.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, buyMember2Username, "BTC", 100.0, 0.0, 30.0, 1.0, Trade.QUANTITY));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 4000.0, 0.0, 3.0, Trade.PRICE));
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember2Username, "BTC", 0.0, 8000.0, 0.0, 4.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(385.0, 62000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(60.0, 92500.0);
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