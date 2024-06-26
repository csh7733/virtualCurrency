package com.practice.virtualcurrency.service.order;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Order;
import com.practice.virtualcurrency.domain.order.State;
import com.practice.virtualcurrency.dto.command.OrderCommand;

public interface OrderService {
    Order buyDesignatedPrice(OrderCommand orderCommand);
    Order sellDesignatedPrice(OrderCommand orderCommand);
    Double buyMarketPrice(OrderCommand orderCommand);
    Double sellMarketPrice(OrderCommand orderCommand);
    boolean cancelOrder(Order order);
    //for test
    void test(Member meber1,String username);
    Member test2();
}
