package com.practice.virtualcurrency.domain.order;

import com.practice.virtualcurrency.domain.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
@Slf4j
public class OrderBook {
    private Map<String, ConcurrentSkipListMap<Double, List<Order>>> buyOrdersMap = new ConcurrentHashMap<>();
    private Map<String, ConcurrentSkipListMap<Double, List<Order>>> sellOrdersMap = new ConcurrentHashMap<>();

    public void addBuyOrder(String coinName, Double price, Order order) {
        buyOrdersMap.computeIfAbsent(coinName, k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(-price, k -> new LinkedList<>()) // Negative price for descending order
                .add(order);
    }

    public void addSellOrder(String coinName, Double price, Order order) {
        sellOrdersMap.computeIfAbsent(coinName, k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(price, k -> new LinkedList<>())
                .add(order);
    }

    public Optional<List<Order>> getBuyOrders(String coinName, Double price) {
        ConcurrentSkipListMap<Double, List<Order>> buyOrders = buyOrdersMap.get(coinName);
        if (buyOrders == null || buyOrders.isEmpty()) {
            return Optional.empty();
        }
        List<Order> orders = buyOrders.get(-price);
        return orders != null ? Optional.of(orders) : Optional.empty();
    }

    public Optional<List<Order>> getSellOrders(String coinName, Double price) {
        ConcurrentSkipListMap<Double, List<Order>> sellOrders = sellOrdersMap.get(coinName);
        if (sellOrders == null || sellOrders.isEmpty()) {
            return Optional.empty();
        }
        List<Order> orders = sellOrders.get(price);
        return orders != null ? Optional.of(orders) : Optional.empty();
    }

    public Optional<Double> getMaxBuyPrice(String coinName) {
        ConcurrentSkipListMap<Double, List<Order>> buyOrders = buyOrdersMap.get(coinName);
        if (buyOrders == null || buyOrders.isEmpty()) {
            return Optional.empty();
        }
        Double firstKey = buyOrders.firstKey();
        return Optional.ofNullable(firstKey).map(key -> -key);
    }


    public Optional<Double> getMinSellPrice(String coinName) {
        ConcurrentSkipListMap<Double, List<Order>> sellOrders = sellOrdersMap.get(coinName);
        if (sellOrders == null || sellOrders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sellOrders.firstKey());
    }

    public Optional<List<Order>> getFirstBuyOrders(String coinName) {
        ConcurrentSkipListMap<Double, List<Order>> buyOrders = buyOrdersMap.get(coinName);
        if (buyOrders == null || buyOrders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(buyOrders.firstEntry().getValue());
    }

    public Optional<List<Order>> getFirstSellOrders(String coinName) {
        ConcurrentSkipListMap<Double, List<Order>> sellOrders = sellOrdersMap.get(coinName);
        if (sellOrders == null || sellOrders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sellOrders.firstEntry().getValue());
    }

    public void removeFirstBuyOrders(String coinName) {
        ConcurrentSkipListMap<Double, List<Order>> buyOrders = buyOrdersMap.get(coinName);
        if (buyOrders != null && !buyOrders.isEmpty()) {
            buyOrders.pollFirstEntry();
        }
    }

    public void removeBuyOrdersAtPrice(String coinName, Double price) {
        ConcurrentSkipListMap<Double, List<Order>> buyOrders = buyOrdersMap.get(coinName);
        if (buyOrders != null && !buyOrders.isEmpty()) {
            buyOrders.remove(-price);
        }
    }

    public void removeFirstSellOrders(String coinName) {
        ConcurrentSkipListMap<Double, List<Order>> sellOrders = sellOrdersMap.get(coinName);
        if (sellOrders != null && !sellOrders.isEmpty()) {
            sellOrders.pollFirstEntry();
        }
    }

    public void removeSellOrdersAtPrice(String coinName, Double price) {
        ConcurrentSkipListMap<Double, List<Order>> sellOrders = sellOrdersMap.get(coinName);
        if (sellOrders != null && !sellOrders.isEmpty()) {
            sellOrders.remove(price);
        }
    }

    public void clear() {
        buyOrdersMap.clear();
        sellOrdersMap.clear();
    }

    public void printMap(State state, String coinName){
        ConcurrentSkipListMap<Double, List<Order>> printMap;

        if(state == State.BUY){
            printMap = buyOrdersMap.get(coinName);
        }else{
            printMap = sellOrdersMap.get(coinName);
        }

        if(printMap == null) return;

        for (Map.Entry<Double, List<Order>> entry : printMap.entrySet()) {
            Double price = entry.getKey();
            List<Order> ordersAtPrice = entry.getValue();
            if(price < 0) price = -price;
            log.info("가격: " + price + ", 주문 개수: " + ordersAtPrice.size());

            for (Order order : ordersAtPrice) {
                String orderCoinName = order.getCoinName();
                Double orderPrice = order.getPrice();
                Double orderQuantity = order.getQuantity();
                State orderState = order.getState();
                String orderUsername = order.getMember().getUsername();
                log.info("username = {}, coinName = {}, price = {}, quantitiy = {}, state = {}, total = {}",
                        orderUsername,orderCoinName,orderPrice,orderQuantity,orderState,orderPrice*orderQuantity);
            }
        }
    }
}
