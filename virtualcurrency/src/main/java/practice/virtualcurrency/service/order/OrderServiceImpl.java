package practice.virtualcurrency.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.domain.coin.Coin;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;
import practice.virtualcurrency.domain.order.Trade;
import practice.virtualcurrency.exception.InsufficientCashException;
import practice.virtualcurrency.repository.order.OrderRepository;
import practice.virtualcurrency.service.member.MemberService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final LiquidationManager liquidationManager;
    private Map<String, TreeMap<Double, List<Order>>> buyOrdersMap = new HashMap<>();
    private Map<String, TreeMap<Double, List<Order>>> sellOrdersMap = new HashMap<>();

    // This parameter, Trade, determines the trading mode for the purchase:
    // If trade is Quantity, the purchase is made by quantity
    // If trade is price, the purchase is made at by price
    @Override
    public Order buyDesignatedPrice(Member member,String coinName, Double price,
                                    Double cash, Double quantity,Double leverage, Trade trade) {
        if(trade == Trade.QUANTITY) {
            cash = quantity*price/leverage;
        }
        takeCash(member,coinName,cash,State.BUY);


        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));

        Optional<Double> minKey = Optional.ofNullable(sellOrders.isEmpty() ? null : sellOrders.firstKey());

        //If the designated price is greater than the market price
        //Buy at a market price instead of a designated price
        if(minKey.isPresent() && (price >= minKey.get())) {
            if(trade == Trade.PRCIE) {
                cash = buyMarketPrice(member, coinName, price, cash,quantity,leverage,trade);
                // If cash is 0,stop to buy
                if (cash == 0) return new Order();
            }else if(trade == Trade.QUANTITY){
                quantity = buyMarketPrice(member, coinName, price, cash,quantity,leverage,trade);
                // If quantity is 0,stop to buy
                if (quantity == 0) return new Order();
            }
        }

        if(trade == Trade.PRCIE) {
            Double totalCash = cash * leverage;
            quantity = totalCash / price;
        }

        return addToOrderBook(member, coinName, price, buyOrders, quantity,State.BUY,leverage);
    }

    //Behaviour similar to when buying
    @Override
    public Order sellDesignatedPrice(Member member,String coinName, Double price,
                                     Double cash,Double quantity,Double leverage, Trade trade) {
        if(trade == Trade.QUANTITY) {
            cash = quantity*price/leverage;
        }
        takeCash(member,coinName,cash,State.SELL);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));

        Optional<Double> maxKey = Optional.ofNullable(buyOrders.isEmpty() ? null : buyOrders.firstKey());

        if(maxKey.isPresent() && (price <= maxKey.get())){
            if(trade == Trade.PRCIE) {
                cash = sellMarketPrice(member, coinName, price, cash,quantity,leverage,trade);
                // If cash is 0,stop to buy
                if (cash == 0) return new Order();
            }else if(trade == Trade.QUANTITY){
                quantity = sellMarketPrice(member, coinName, price, cash,quantity,leverage,trade);
                // If quantity is 0,stop to buy
                if (quantity == 0) return new Order();
            }
        }

        if(trade == Trade.PRCIE) {
            Double totalCash = cash * leverage;
            quantity = totalCash / price;
        }
        return addToOrderBook(member,coinName,price,sellOrders,quantity,State.SELL,leverage);
    }

    // This parameter, Price, determines the pricing mode for the purchase:
    // If Price is 0, the purchase is made at the current market price.
    // If Price is not 0, the purchase is made at the designated price.
    @Override
    public Double buyMarketPrice(Member member,String coinName, Double price,
                                 Double cash,Double quantity,Double leverage, Trade trade) {
        if(trade == Trade.PRCIE && price ==0) takeCash(member,coinName,cash,State.BUY);
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double totalCash = cash*leverage;

        boolean isRemain = true;
        boolean escape = false;

        //if totalCash > 0 (When trade method is Price) || quantity > 0 (When trade method is quantity)
        //isRemain is true
        while(isRemain){
            Map.Entry<Double, List<Order>> firstEntry = sellOrders.firstEntry();
            Optional<List<Order>> optionalCurrentSellPriceList = Optional.ofNullable(firstEntry != null ? firstEntry.getValue() : null);

            //If there is no coin for sale,there are two cases
            //In the case of purchase at the market price from the beginning(parameter Price value is 0),it will no longer buy it
            //In the case of purchase at the designated price from the beginning(parameter Price value is not 0),it will buy it at a designated price
            if(optionalCurrentSellPriceList.isEmpty()){
                escape = true;
            }else{
                List<Order> currentSellPriceList = optionalCurrentSellPriceList.get();
                while(!currentSellPriceList.isEmpty()){
                    Order currentOrder = currentSellPriceList.get(0);
                    Double currentOrderPrice = currentOrder.getPrice();
                    //If the price is smaller than the currentOrderPrice,
                    //it means that the remaining quantity must be purchased at the designated price.
                    //Set the escape flag to true and exit the market method and return to the designated price method
                    if(price < currentOrderPrice) {
                        escape = true;
                        break;
                    }

                    Double currentOrderTotalPrice = currentOrder.getQuantity() * currentOrder.getPrice();
                    if(trade == Trade.PRCIE) {
                        if (totalCash >= currentOrderTotalPrice) {
                            trade(member, currentOrder.getMember(), currentOrder.getCoinName(), currentOrder.getPrice(),
                                    currentOrder.getQuantity(), leverage, currentOrder.getLeverage());
                            currentSellPriceList.remove(0);
                            totalCash -= currentOrderTotalPrice;
                        } else {
                            quantity = totalCash / currentOrder.getPrice();
                            trade(member, currentOrder.getMember(), currentOrder.getCoinName(), currentOrder.getPrice(),
                                    quantity, leverage, currentOrder.getLeverage());
                            currentOrder.setQuantity(currentOrder.getQuantity() - quantity);
                            totalCash = 0.0;
                        }
                        if(totalCash == 0) break;
                    }else{
                        if (quantity >= currentOrder.getQuantity()) {
                            trade(member, currentOrder.getMember(), currentOrder.getCoinName(), currentOrder.getPrice(),
                                    currentOrder.getQuantity(), leverage, currentOrder.getLeverage());
                            currentSellPriceList.remove(0);
                            quantity -= currentOrder.getQuantity();
                        } else {
                            trade(member, currentOrder.getMember(), currentOrder.getCoinName(), currentOrder.getPrice(),
                                    quantity, leverage, currentOrder.getLeverage());
                            currentOrder.setQuantity(currentOrder.getQuantity() - quantity);
                            quantity = 0.0;
                        }
                        if(quantity == 0) break;
                    }
                }
                if(currentSellPriceList.isEmpty()) sellOrders.remove(firstEntry.getKey());
                if((trade == Trade.PRCIE && totalCash == 0) || (trade == Trade.QUANTITY && quantity == 0)) isRemain = false;
            }
            if(escape) break;
        }
        if(trade == Trade.PRCIE){
            cash = totalCash/leverage;
            if(price == 0) addCash(member,cash);
            return cash;
        }else{
            return quantity;
        }
    }

    //Behaviour similar to when buying
    @Override
    public Double sellMarketPrice(Member member,String coinName, Double price,
                                  Double cash,Double quantity,Double leverage, Trade trade) {
        if(trade == Trade.PRCIE && price ==0) takeCash(member,coinName,cash,State.SELL);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        Double totalCash = cash*leverage;

        boolean isRemain = true;
        boolean escape = false;

        while(isRemain){
            Map.Entry<Double, List<Order>> firstEntry = buyOrders.firstEntry();
            Optional<List<Order>> optionalCurrentBuyPriceList = Optional.ofNullable(firstEntry != null ? firstEntry.getValue() : null);

            if(optionalCurrentBuyPriceList.isEmpty()){
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
                    if(trade == Trade.PRCIE) {
                        if (totalCash >= currentOrderTotalPrice) {
                            trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),
                                    currentOrder.getQuantity(),currentOrder.getLeverage(),leverage);
                            currentBuyPriceList.remove(0);
                            totalCash -= currentOrderTotalPrice;
                        } else {
                            quantity = totalCash/currentOrder.getPrice();
                            trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),
                                    quantity,currentOrder.getLeverage(),leverage);
                            currentOrder.setQuantity(currentOrder.getQuantity()-quantity);
                            totalCash = 0.0;
                        }
                        if(totalCash == 0) break;
                    }else{
                        if (quantity >= currentOrder.getQuantity()) {
                            trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),
                                    currentOrder.getQuantity(),currentOrder.getLeverage(),leverage);
                            currentBuyPriceList.remove(0);
                            quantity -= currentOrder.getQuantity();
                        } else {
                            trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),
                                    quantity,currentOrder.getLeverage(),leverage);
                            currentOrder.setQuantity(currentOrder.getQuantity()-quantity);
                            quantity = 0.0;
                        }
                        if(quantity == 0) break;
                    }
                }
                if(currentBuyPriceList.isEmpty()) buyOrders.remove(firstEntry.getKey());
                if((trade == Trade.PRCIE && totalCash == 0) || (trade == Trade.QUANTITY && quantity == 0)) isRemain = false;
            }
            if(escape) break;
        }
        if(trade == Trade.PRCIE){
            cash = totalCash/leverage;
            if(price == 0) addCash(member,cash);
            return cash;
        }else{
            return quantity;
        }
    }

    @Override
    public boolean cancelOrder(Order order) {
        Map<String, TreeMap<Double, List<Order>>> ordersMap = order.getState() == State.BUY ? buyOrdersMap : sellOrdersMap;
        TreeMap<Double, List<Order>> priceMap = ordersMap.get(order.getCoinName());
        List<Order> currentPriceList = priceMap.get(order.getPrice());
        addCash(order.getMember(),order.getPrice()*order.getQuantity());
        return currentPriceList.remove(order);

    }
    @EventListener
    public void onLiquidationEvent(LiquidationEvent event) {
        List<Order> orders = event.getLiquidationOrders();
        for (Order order : orders) {
            //log.info("liquidation order = {}!",order.toString());
            removeOrder(order.getMember(),order.getCoinName());
            memberService.resetCoin(order.getMember(),order.getCoinName());
            /**
             * ToDo Trade Market price by Opposite Postion
             */
        }
    }

    public void trade(Member buyMember, Member sellMember, String coinName, Double price, Double quantity,
                      Double buyMemberLeverage,Double sellMemberLeverage) {
        Order buyMemberOrder = new Order(coinName, price, quantity, State.BUY, buyMember,buyMemberLeverage);
        Order sellMemberOrder = new Order(coinName, price, quantity, State.SELL, sellMember,sellMemberLeverage);

        addOrderAndSetLiquidation(buyMember, sellMember, coinName, price, buyMemberOrder, sellMemberOrder);

        //If the member's position is different from the neworder's position
        //That means handing over what member originally have to the opposite position
        if(hasDifferentPosition(buyMember,coinName,State.BUY)) {
            Double currentPositionPrice = getOrderWithCoinName(buyMember, coinName).getPrice();
            Double priceOffset = currentPositionPrice - price;
            priceOffset = priceOffset > 0 ? currentPositionPrice + priceOffset : priceOffset;
            addCash(buyMember,priceOffset*quantity);
        }
        if(hasDifferentPosition(sellMember,coinName,State.SELL)) {
            Double currentPositionPrice = getOrderWithCoinName(sellMember, coinName).getPrice();
            Double priceOffset = price-currentPositionPrice;
            priceOffset = priceOffset > 0 ? currentPositionPrice + priceOffset : priceOffset;
            addCash(sellMember,priceOffset*quantity);
        }

        memberService.addCoin(buyMember,new Coin(coinName,quantity));
        memberService.subCoin(sellMember,new Coin(coinName,quantity));


        log.info("trade success!");
        log.info("buy order ={}",buyMemberOrder);
        log.info("sell order ={}",sellMemberOrder);
    }

    private void addOrderAndSetLiquidation(Member buyMember, Member sellMember, String coinName, Double price, Order buyMemberOrder, Order sellMemberOrder) {
        Order buyMemberprevOrder = getOrderWithCoinName(buyMember, coinName);
        Order sellMemberprevOrder = getOrderWithCoinName(sellMember, coinName);

        if(!buyMemberprevOrder.getPrice().equals(0.0)) liquidationManager.deleteLiquidationOrder(buyMemberprevOrder);
        if(!sellMemberprevOrder.getPrice().equals(0.0)) liquidationManager.deleteLiquidationOrder(sellMemberprevOrder);

        Order buyMemberNewOrder = addOrder(buyMember, buyMemberOrder);
        Order sellMemberNewOrder = addOrder(sellMember, sellMemberOrder);

        liquidationManager.addToLiquidationOrdersMap(buyMemberNewOrder);
        liquidationManager.addToLiquidationOrdersMap(sellMemberNewOrder);

        liquidationManager.setPrice(coinName, price);
    }

    public Order addOrder(Member member,Order newOrder) {
        String coinName = newOrder.getCoinName();
        Order prevOrder = getOrderWithCoinName(member, coinName);
        State state;
        Double prevLeverageCash,prevCash,newLeverageCash,newCash,totalLeverageCash,
                totalCash,quantity,price,leverage;

        //If the member's position is different from the neworder's position
        //Turn the newOrder values into negative numbers for calculation
        //Result is always positive(Because original position values > newOrder values)
        if(hasDifferentPosition(member,coinName,newOrder.getState())){
            state = newOrder.getState().equals(State.BUY) ? State.SELL : State.BUY;
            quantity = prevOrder.getQuantity() - newOrder.getQuantity();
            price = prevOrder.getPrice();
            leverage = prevOrder.getLeverage();
        }else{
            state = newOrder.getState();
            prevLeverageCash = prevOrder.getPrice()*prevOrder.getQuantity();
            prevCash = prevLeverageCash/prevOrder.getLeverage();
            newLeverageCash = newOrder.getPrice()* newOrder.getQuantity();
            newCash = newLeverageCash/newOrder.getLeverage();
            totalLeverageCash = prevLeverageCash + newLeverageCash;
            totalCash = prevCash + newCash;
            quantity = prevOrder.getQuantity() + newOrder.getQuantity();
            price = totalLeverageCash/quantity;
            leverage = totalLeverageCash/totalCash;
        }
        prevOrder.setMember(member);
        prevOrder.setPrice(price);
        prevOrder.setQuantity(quantity);
        prevOrder.setState(state);
        prevOrder.setLeverage(leverage);
        return prevOrder;
    }

    private Order getOrderWithCoinName(Member member, String coinName) {
        List<Order> orders = member.getOrders();
        for (Order order : orders) {
            if (order.getCoinName().equals(coinName)) {
                return order;
            }
        }

        Order newOrder = new Order(coinName);
        orders.add(newOrder);
        orderRepository.save(newOrder);
        return newOrder;
    }

    public void removeOrder(Member member,String coinName) {
        List<Order> orders = member.getOrders();
        for(Order order : orders){
            if(order.getCoinName().equals(coinName)){
                order.setLeverage(1.0);
                order.setQuantity(0.0);
                order.setPrice(0.0);
            }
        }
    }

    private void takeCash(Member member,String coinName,Double cash,State state){
        //If the member's position is different from the neworder's position
        //Don't take out the money and return
        if (hasDifferentPosition(member, coinName, state)) return;

        if(member.getWallet().get("KRW") < cash) throw new InsufficientCashException();
        memberService.subCoin(member,new Coin("KRW",cash));
    }

    private boolean hasDifferentPosition(Member member, String coinName, State state) {
        if(memberService.getCoin(member, coinName) > 0 && state.equals(State.SELL)) {
            log.info("isDifferent! coin ={},state ={}",memberService.getCoin(member,coinName),state);
            return true;
        }
        if(memberService.getCoin(member, coinName) < 0 && state.equals(State.BUY)) {
            log.info("isDifferent! coin ={},state ={}",memberService.getCoin(member,coinName),state);
            return true;
        }
        return false;
    }

    private void addCash(Member member, Double cash){
        memberService.addCoin(member,new Coin("KRW",cash));
    }

    private Order addToOrderBook(Member member, String coinName, Double price, TreeMap<Double, List<Order>> Orders,
                                 Double quantity, State state, Double leverage) {
        List<Order> currentPriceList = Orders.computeIfAbsent(price, k -> new LinkedList<>());
        Order order = new Order(coinName, price, quantity,state, member,leverage);
        currentPriceList.add(order);
        return order;
    }

    //For Test
    public void printMap(State state, String coinName){
        TreeMap<Double, List<Order>> printMap;

        if(state == State.BUY){
            printMap = buyOrdersMap.get(coinName);
        }else{
            printMap = sellOrdersMap.get(coinName);
        }

        for (Map.Entry<Double, List<Order>> entry : printMap.entrySet()) {
            Double price = entry.getKey();
            List<Order> ordersAtPrice = entry.getValue();
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

    public void clearOrderBook(){
        buyOrdersMap.clear();
        sellOrdersMap.clear();
        liquidationManager.clear();
    }
}
