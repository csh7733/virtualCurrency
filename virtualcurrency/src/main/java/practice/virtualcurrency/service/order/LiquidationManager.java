package practice.virtualcurrency.service.order;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import practice.virtualcurrency.VirtualCurrencyConst;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EnableScheduling
@Component
@Slf4j
public class LiquidationManager {
    private Map<String, AtomicDouble> coinPriceMap = new ConcurrentHashMap<>();
    private Map<String,Double> coinPrevPriceMap = new HashMap<>();
    private Map<String, TreeMap<Double, List<Order>>> liquidationOrdersMap = new HashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public LiquidationManager(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
        initMap();
    }

    private void initMap() {
        for (String coinName : VirtualCurrencyConst.WALLET_ELEMENTS) {
            coinPriceMap.put(coinName,new AtomicDouble(0.0));
            coinPrevPriceMap.put(coinName,0.0);
        }
    }

    public Double getPrice(String coinName) {
        return coinPriceMap.get(coinName).get();
    }

    public void setPrice(String coinName, Double newPrice) {
        coinPriceMap.put(coinName, new AtomicDouble(newPrice));
    }

    @Scheduled(fixedRate = 100)
    public void manageLiquidation() {
        for (String coinName : VirtualCurrencyConst.WALLET_ELEMENTS) {
            Double newPrice = getPrice(coinName);
            Double prevPrice = coinPrevPriceMap.get(coinName);
            if(!newPrice.equals(prevPrice)){
                //log.info("liquidation start! prev = {},new = {}",prevPrice,newPrice);
                coinPrevPriceMap.put(coinName,newPrice);
                if(!prevPrice.equals(0.0)) liquidate(coinName,prevPrice,newPrice);
            }
        }
    }
    public void liquidate(String coinName,Double prevCoinPrice,Double currentCoinPrice) {
        Double lowerBoundPrice = prevCoinPrice < currentCoinPrice ? prevCoinPrice : currentCoinPrice;
        Double upperBoundPrice = prevCoinPrice < currentCoinPrice ? currentCoinPrice : prevCoinPrice;

        TreeMap<Double, List<Order>> allLiquidationOrders =
                liquidationOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));

        Map<Double, List<Order>> subMap = allLiquidationOrders.subMap(lowerBoundPrice, true, upperBoundPrice, true);

        List<Order> liquidationOrdersToProcess = new ArrayList<>();
        for (Double price : new ArrayList<>(subMap.keySet())) {
            List<Order> liquidationOrders = allLiquidationOrders.remove(price);
            liquidationOrdersToProcess.addAll(liquidationOrders);
        }

        LiquidationEvent event = new LiquidationEvent(this, liquidationOrdersToProcess);
        eventPublisher.publishEvent(event);
    }

    public void addToLiquidationOrdersMap(Order newOrder){
        Double liquidationPrice = getLiquidationPrice(newOrder);
        //log.info("add liquidation price : order {} : {}",liquidationPrice,newOrder);
        TreeMap<Double, List<Order>> allLiquidationOrders =
                liquidationOrdersMap.computeIfAbsent(newOrder.getCoinName(), k -> new TreeMap<>(Comparator.naturalOrder()));
        List<Order> liquidationOrders = allLiquidationOrders.computeIfAbsent(liquidationPrice, k -> new LinkedList<>());
        liquidationOrders.add(newOrder);
    }

    public void deleteLiquidationOrder(Order deleteOrder){
        Double liquidationPrice = getLiquidationPrice(deleteOrder);
        //log.info("delete liquidation price : order {} : {}",liquidationPrice,deleteOrder);
        TreeMap<Double, List<Order>> allLiquidationOrders =
                liquidationOrdersMap.computeIfAbsent(deleteOrder.getCoinName(), k -> new TreeMap<>(Comparator.naturalOrder()));
        List<Order> liquidationOrders = allLiquidationOrders.computeIfAbsent(liquidationPrice, k -> new LinkedList<>());
        liquidationOrders.remove(deleteOrder);
    }

    public Double getLiquidationPrice(Order order) {
        Double price = order.getPrice();
        Double offset = price/order.getLeverage();
        return order.getState() == State.BUY ? price - offset : price + offset;
    }

}