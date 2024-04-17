package practice.virtualcurrency.service.order;

import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;
import practice.virtualcurrency.domain.order.Trade;

public interface OrderService {
    public Order buyDesignatedPrice(Member member, String coinName, Double price, Double cash, Double quantity, Double leverage, Trade trade);
    public Order sellDesignatedPrice(Member member,String coinName,Double price,Double cash,Double quantity,Double leverage,Trade trade);
    public Double buyMarketPrice(Member member,String coinName,Double price,Double cash,Double quantity,Double leverage,Trade trade);
    public Double sellMarketPrice(Member member,String coinName,Double price,Double cash,Double quantity,Double leverage,Trade trade);

    public boolean cancelOrder(Order order);
    //For Test
    public void printMap(State state, String coinName);
    public void clearOrderBook();

}
