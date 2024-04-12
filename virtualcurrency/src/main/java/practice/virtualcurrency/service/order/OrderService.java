package practice.virtualcurrency.service.order;

import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;

public interface OrderService {
    public void buyDesignatedPrice(Member member,String coinName,Double price,Double cash);
    public void sellDesignatedPrice(Member member,String coinName,Double price,Double cash);
    public Double buyMarketPrice(Member member,String coinName,Double price,Double cash);
    public Double sellMarketPrice(Member member,String coinName,Double price,Double cash);
    //For Test
    public void printMap(State state, String coinName);
    public void clearOrderBook();

}
