package com.practice.virtualcurrency.order;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.*;
import com.practice.virtualcurrency.dto.command.OrderCommand;
import com.practice.virtualcurrency.exception.InsufficientCashException;
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
class OrderServiceUnitTest {

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
    void insufficientCashExceptionTest() {
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 100.0, 50000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 50000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(0.0, 0.0);

        assertThatThrownBy(() -> {
            orderService.buyDesignatedPrice(
                    new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 100.0, 50000.0, 0.0, 1.0, Trade.PRICE));
            orderService.buyDesignatedPrice(
                    new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 60000.0, 0.0, 1.0, Trade.PRICE));
        }).isInstanceOf(InsufficientCashException.class);

        printResult();
    }

    //Member(Total)
    //----------------Init Order Book(Buy)-----------------
    //| 200 : M1(8000)->M1(6000)->M2(4000)->M2(2000)      |
    //-----------------------------------------------------
    @Test
    void cancelOrderTest() {
        //----------------Init Order Book(Buy)-----------------
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 8000.0, 0.0, 1.0, Trade.PRICE));
        Order cancelOrder = orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember1Username, "BTC", 200.0, 6000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 4000.0, 0.0, 1.0, Trade.PRICE));
        orderService.buyDesignatedPrice(
                new OrderCommand(OrderType.LIMIT, buyMember2Username, "BTC", 200.0, 2000.0, 0.0, 1.0, Trade.PRICE));
        //-----------------------------------------------------
        orderService.cancelOrder(cancelOrder);
        orderService.sellMarketPrice(
                new OrderCommand(OrderType.MARKET, sellMember1Username, "BTC", 0.0, 10000.0, 0.0, 1.0, Trade.PRICE));

        assertThat(getCoinAndCash(buyMember1)).containsExactly(40.0, 92000.0);
        assertThat(getCoinAndCash(buyMember2)).containsExactly(10.0, 94000.0);

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