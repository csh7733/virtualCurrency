package com.practice.virtualcurrency.service.order;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Order;
import com.practice.virtualcurrency.domain.order.OrderBook;
import com.practice.virtualcurrency.domain.order.State;
import com.practice.virtualcurrency.domain.order.Trade;
import com.practice.virtualcurrency.dto.command.OrderCommand;
import com.practice.virtualcurrency.dto.command.TradeCommand;
import com.practice.virtualcurrency.exception.InsufficientCashException;
import com.practice.virtualcurrency.repository.order.OrderRepository;
import com.practice.virtualcurrency.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final OrderBook orderBook;

    @Override
    public Order buyDesignatedPrice(OrderCommand orderCommand) {
        Member member = orderCommand.getMember();
        String coinName = orderCommand.getCoinName();
        Double price = orderCommand.getPrice();
        Double cash = orderCommand.getCash();
        Double quantity = orderCommand.getQuantity();
        Double leverage = orderCommand.getLeverage();
        Trade trade = orderCommand.getTrade();

        Optional<Double> minSellPrice = orderBook.getMinSellPrice(coinName);

        if(trade== Trade.QUANTITY) {
            cash = quantity*price/leverage;
            orderCommand.setCash(cash);
            takeCash(member,cash);

            // If the designated price is greater than the market price
            // Buy at a market price instead of a designated price
            if (minSellPrice.isPresent() && (price >= minSellPrice.get())) {
                quantity = buyMarketPrice(orderCommand);
                // If quantity is 0, stop to buy
                if (quantity == 0) return new Order();

            }

            Order order = Order.builder()
                    .member(member)
                    .coinName(coinName)
                    .price(price)
                    .quantity(quantity)
                    .leverage(leverage)
                    .state(State.BUY)
                    .build();

            orderBook.addBuyOrder(coinName,price,order);

            return order;
        }
        else{ // trade == Trade.PRICE
            takeCash(member,cash);

            // If the designated price is greater than the market price
            // Buy at a market price instead of a designated price
            if (minSellPrice.isPresent() && (price >= minSellPrice.get())) {
                cash = buyMarketPrice(orderCommand);
                // If cash is 0,stop to buy
                if (cash == 0) return new Order();
            }

            Double totalCash = cash * leverage;
            quantity = totalCash / price;

            Order order = Order.builder()
                    .member(member)
                    .coinName(coinName)
                    .price(price)
                    .quantity(quantity)
                    .leverage(leverage)
                    .state(State.BUY)
                    .build();

            orderBook.addBuyOrder(coinName,price,order);

            return order;
        }
    }

    //Behaviour similar to when buying
    @Override
    public Order sellDesignatedPrice(OrderCommand orderCommand) {
        Member member = orderCommand.getMember();
        String coinName = orderCommand.getCoinName();
        Double price = orderCommand.getPrice();
        Double cash = orderCommand.getCash();
        Double quantity = orderCommand.getQuantity();
        Double leverage = orderCommand.getLeverage();
        Trade trade = orderCommand.getTrade();

        Optional<Double> maxBuyPrice = orderBook.getMaxBuyPrice(coinName);

        if(trade == Trade.QUANTITY) {
            cash = quantity*price/leverage;
            orderCommand.setCash(cash);
            takeCash(member,cash);
            if (maxBuyPrice.isPresent() && (price <= maxBuyPrice.get())) {
                quantity = sellMarketPrice(orderCommand);
                // If quantity is 0, stop to sell
                if (quantity == 0) return new Order();
            }

            Order order = Order.builder()
                    .member(member)
                    .coinName(coinName)
                    .price(price)
                    .quantity(quantity)
                    .leverage(leverage)
                    .state(State.SELL)
                    .build();

            orderBook.addSellOrder(coinName, price, order);

            return order;
        } else { // trade == Trade.PRICE
            takeCash(member,cash);

            if (maxBuyPrice.isPresent() && (price <= maxBuyPrice.get())) {
                cash = sellMarketPrice(orderCommand);
                // If cash is 0, stop to sell
                if (cash == 0) return new Order();
            }

            Double totalCash = cash * leverage;
            quantity = totalCash / price;

            Order order = Order.builder()
                    .member(member)
                    .coinName(coinName)
                    .price(price)
                    .quantity(quantity)
                    .leverage(leverage)
                    .state(State.SELL)
                    .build();

            orderBook.addSellOrder(coinName, price, order);

            return order;
        }
    }

    // This parameter, Price, determines the pricing mode for the purchase:
    // If Price is 0, the purchase is made at the current market price.
    // If Price is not 0, the purchase is made at the designated price.
    @Override
    public Double buyMarketPrice(OrderCommand orderCommand) {
        Member member = orderCommand.getMember();
        String coinName = orderCommand.getCoinName();
        Double price = orderCommand.getPrice();
        Double cash = orderCommand.getCash();
        Double quantity = orderCommand.getQuantity();
        Double leverage = orderCommand.getLeverage();
        Trade trade = orderCommand.getTrade();

        if(trade == Trade.QUANTITY){

            boolean isRemain = true;
            boolean escape = false;

            //if quantity > 0 , isRemain is true
            while(isRemain) {

                Optional<List<Order>> optionalCurrentSellPriceList = orderBook.getFirstSellOrders(coinName);

                if(optionalCurrentSellPriceList.isEmpty()) {
                    escape = true;
                }else{
                    List<Order> currentSellPriceList = optionalCurrentSellPriceList.get();
                    while(!currentSellPriceList.isEmpty()) {
                        Order currentOrder = currentSellPriceList.get(0);
                        Double currentOrderPrice = currentOrder.getPrice();
                        //If the price is smaller than the currentOrderPrice,
                        //it means that the remaining quantity must be purchased at the designated price.
                        //Set the escape flag to true and exit the market method and return to the designated price method
                        if(price != 0.0 && price < currentOrderPrice) {
                            escape = true;
                            break;
                        }

                        if (quantity >= currentOrder.getQuantity()) {
                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(member)
                                    .sellMember(currentOrder.getMember())
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(currentOrder.getQuantity())
                                    .buyMemberLeverage(leverage)
                                    .sellMemberLeverage(currentOrder.getLeverage())
                                    .build();

                            trade(tradeCommand);
                            currentSellPriceList.remove(0);
                            quantity -= currentOrder.getQuantity();
                        } else {
                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(member)
                                    .sellMember(currentOrder.getMember())
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(quantity)
                                    .buyMemberLeverage(leverage)
                                    .sellMemberLeverage(currentOrder.getLeverage())
                                    .build();

                            trade(tradeCommand);
                            currentOrder.setQuantity(currentOrder.getQuantity() - quantity);
                            quantity = 0.0;
                            break;
                        }
                    }
                    if(currentSellPriceList.isEmpty()) orderBook.removeFirstSellOrders(coinName);
                    if(quantity == 0.0) isRemain = false;
                }
                if(escape) break;
            }
            return quantity;
        }else{
            if(price == 0) takeCash(member,cash);

            Double totalCash = cash*leverage;

            boolean isRemain = true;
            boolean escape = false;

            //if totalCash > 0 , isRemain is true
            while(isRemain) {

                Optional<List<Order>> optionalCurrentSellPriceList = orderBook.getFirstSellOrders(coinName);

                if(optionalCurrentSellPriceList.isEmpty()) {
                    escape = true;
                }else{
                    List<Order> currentSellPriceList = optionalCurrentSellPriceList.get();
                    while(!currentSellPriceList.isEmpty()){
                        Order currentOrder = currentSellPriceList.get(0);
                        Double currentOrderPrice = currentOrder.getPrice();
                        //If the price is smaller than the currentOrderPrice,
                        //it means that the remaining quantity must be purchased at the designated price.
                        //Set the escape flag to true and exit the market method and return to the designated price method
                        if(price != 0.0 && price < currentOrderPrice) {
                            escape = true;
                            break;
                        }

                        Double currentOrderTotalPrice = currentOrder.getQuantity() * currentOrder.getPrice();

                        if (totalCash >= currentOrderTotalPrice) {
                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(member)
                                    .sellMember(currentOrder.getMember())
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(currentOrder.getQuantity())
                                    .buyMemberLeverage(leverage)
                                    .sellMemberLeverage(currentOrder.getLeverage())
                                    .build();

                            trade(tradeCommand);
                            currentSellPriceList.remove(0);
                            totalCash -= currentOrderTotalPrice;
                        } else {
                            quantity = totalCash / currentOrder.getPrice();

                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(member)
                                    .sellMember(currentOrder.getMember())
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(quantity)
                                    .buyMemberLeverage(leverage)
                                    .sellMemberLeverage(currentOrder.getLeverage())
                                    .build();

                            trade(tradeCommand);
                            currentOrder.setQuantity(currentOrder.getQuantity() - quantity);
                            totalCash = 0.0;
                            break;
                        }
                    }
                    if(currentSellPriceList.isEmpty()) orderBook.removeFirstSellOrders(coinName);
                    if(totalCash == 0) isRemain = false;
                }
                if(escape) break;
            }
            cash = totalCash/leverage;
            if(price == 0) addCash(member,cash);
            return cash;
        }
    }

    @Override
    public Double sellMarketPrice(OrderCommand orderCommand) {
        Member member = orderCommand.getMember();
        String coinName = orderCommand.getCoinName();
        Double price = orderCommand.getPrice();
        Double cash = orderCommand.getCash();
        Double quantity = orderCommand.getQuantity();
        Double leverage = orderCommand.getLeverage();
        Trade trade = orderCommand.getTrade();

        if(trade == Trade.QUANTITY){

            boolean isRemain = true;
            boolean escape = false;

            //if quantity > 0 , isRemain is true
            while(isRemain) {

                Optional<List<Order>> optionalCurrentBuyPriceList = orderBook.getFirstBuyOrders(coinName);

                if(optionalCurrentBuyPriceList.isEmpty()) {
                    escape = true;
                }else{
                    List<Order> currentBuyPriceList = optionalCurrentBuyPriceList.get();
                    while(!currentBuyPriceList.isEmpty()) {
                        Order currentOrder = currentBuyPriceList.get(0);
                        Double currentOrderPrice = currentOrder.getPrice();

                        if(price > currentOrderPrice) {
                            escape = true;
                            break;
                        }

                        if (quantity >= currentOrder.getQuantity()) {
                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(currentOrder.getMember())
                                    .sellMember(member)
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(currentOrder.getQuantity())
                                    .buyMemberLeverage(currentOrder.getLeverage())
                                    .sellMemberLeverage(leverage)
                                    .build();

                            trade(tradeCommand);
                            currentBuyPriceList.remove(0);
                            quantity -= currentOrder.getQuantity();
                        } else {
                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(currentOrder.getMember())
                                    .sellMember(member)
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(quantity)
                                    .buyMemberLeverage(currentOrder.getLeverage())
                                    .sellMemberLeverage(leverage)
                                    .build();

                            trade(tradeCommand);
                            currentOrder.setQuantity(currentOrder.getQuantity() - quantity);
                            quantity = 0.0;
                            break;
                        }
                    }
                    if(currentBuyPriceList.isEmpty()) orderBook.removeFirstBuyOrders(coinName);
                    if(quantity == 0.0) isRemain = false;
                }
                if(escape) break;
            }
            return quantity;
        }else{
            if(price == 0) takeCash(member,cash);

            Double totalCash = cash*leverage;

            boolean isRemain = true;
            boolean escape = false;

            //if totalCash > 0 , isRemain is true
            while(isRemain) {

                Optional<List<Order>> optionalCurrentBuyPriceList = orderBook.getFirstBuyOrders(coinName);

                if(optionalCurrentBuyPriceList.isEmpty()) {
                    escape = true;
                }else{
                    List<Order> currentBuyPriceList = optionalCurrentBuyPriceList.get();
                    while(!currentBuyPriceList.isEmpty()){
                        Order currentOrder = currentBuyPriceList.get(0);
                        Double currentOrderPrice = currentOrder.getPrice();

                        if(price > currentOrderPrice) {
                            escape = true;
                            break;
                        }

                        Double currentOrderTotalPrice = currentOrder.getQuantity() * currentOrder.getPrice();

                        if (totalCash >= currentOrderTotalPrice) {
                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(currentOrder.getMember())
                                    .sellMember(member)
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(currentOrder.getQuantity())
                                    .buyMemberLeverage(currentOrder.getLeverage())
                                    .sellMemberLeverage(leverage)
                                    .build();

                            trade(tradeCommand);
                            currentBuyPriceList.remove(0);
                            totalCash -= currentOrderTotalPrice;
                        } else {
                            quantity = totalCash / currentOrder.getPrice();

                            TradeCommand tradeCommand = TradeCommand.builder()
                                    .buyMember(currentOrder.getMember())
                                    .sellMember(member)
                                    .coinName(coinName)
                                    .price(currentOrder.getPrice())
                                    .quantity(quantity)
                                    .buyMemberLeverage(currentOrder.getLeverage())
                                    .sellMemberLeverage(leverage)
                                    .build();

                            trade(tradeCommand);
                            currentOrder.setQuantity(currentOrder.getQuantity() - quantity);
                            totalCash = 0.0;
                            break;
                        }
                    }
                    if(currentBuyPriceList.isEmpty()) orderBook.removeFirstBuyOrders(coinName);
                    if(totalCash == 0) isRemain = false;
                }
                if(escape) break;
            }
            cash = totalCash/leverage;
            if(price == 0) addCash(member,cash);
            return cash;
        }
    }

    @Override
    public boolean cancelOrder(Order order) {
        String coinName = order.getCoinName();
        Double price = order.getPrice();
        Member member = order.getMember();
        Double quantity = order.getQuantity();
        Double leverage = order.getLeverage();

        Optional<List<Order>> optionalOrders = order.getState() == State.BUY ?
                orderBook.getBuyOrders(coinName,price) : orderBook.getSellOrders(coinName,price);
        if(optionalOrders.isPresent()){
            List<Order> orders = optionalOrders.get();
            boolean removed = orders.remove(order);
            if (removed) {
                addCash(member, price * quantity / leverage);
                if (orders.isEmpty()) {
                    if(order.getState() == State.BUY) orderBook.removeBuyOrdersAtPrice(coinName,price);
                    else orderBook.removeSellOrdersAtPrice(coinName,price);
                }
                return true;
            }
        }
        return false;
    }

    private void takeCash(Member member,Double cash){

        if(memberService.getCoin(member,"USDT") < cash) throw new InsufficientCashException();

        memberService.subCoin(member,"USDT",cash);
    }

    private void addCash(Member member, Double cash){
        memberService.addCoin(member,"USDT",cash);
    }

    private void trade(TradeCommand tradeCommand) {
        Member buyMember = tradeCommand.getBuyMember();
        Member sellMember = tradeCommand.getSellMember();
        String coinName = tradeCommand.getCoinName();
        Double price = tradeCommand.getPrice();
        Double quantity = tradeCommand.getQuantity();
        Double buyMemberLeverage = tradeCommand.getBuyMemberLeverage();
        Double sellMemberLeverage = tradeCommand.getSellMemberLeverage();

        Order buyMemberOrder = Order.builder()
                .member(buyMember)
                .coinName(coinName)
                .price(price)
                .quantity(quantity)
                .state(State.BUY)
                .leverage(buyMemberLeverage)
                .build();

        Order sellMemberOrder = Order.builder()
                .member(sellMember)
                .coinName(coinName)
                .price(price)
                .quantity(quantity)
                .state(State.SELL)
                .leverage(sellMemberLeverage)
                .build();

        memberService.addCoin(buyMember,coinName,quantity);
        memberService.subCoin(sellMember,coinName,quantity);

        log.info("trade success!");
        log.info("buy order ={}",buyMemberOrder);
        log.info("sell order ={}",sellMemberOrder);
    }

}
