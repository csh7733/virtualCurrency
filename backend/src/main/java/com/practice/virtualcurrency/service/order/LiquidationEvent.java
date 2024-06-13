package com.practice.virtualcurrency.service.order;

import com.practice.virtualcurrency.domain.order.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class LiquidationEvent extends ApplicationEvent {

    private final List<Order> liquidationOrders;
    public LiquidationEvent(Object source, List<Order> liquidationOrders) {
        super(source);
        this.liquidationOrders = liquidationOrders;
    }
}
