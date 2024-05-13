package practice.virtualcurrency.service.order;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import practice.virtualcurrency.domain.order.Order;

import java.util.List;

@Getter
public class LiquidationEvent extends ApplicationEvent {

    private final List<Order> liquidationOrders;
    public LiquidationEvent(Object source, List<Order> ordersToLiquidate) {
        super(source);
        this.liquidationOrders = ordersToLiquidate;
    }
}
