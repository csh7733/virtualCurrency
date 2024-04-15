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

    @Override
    public Order buyDesignatedPrice(Member member,String coinName, Double price, Double cash, Double leverage) {
        checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double quantity;

        Optional<Double> minKey = Optional.ofNullable(sellOrders.isEmpty() ? null : sellOrders.firstKey());

        //If the designated price is greater than the market price
        //Buy at a market price instead of a designated price
        if(minKey.isPresent() && (price >= minKey.get())) {
            cash = buyMarketPrice(member, coinName, price, cash,leverage);
            // If cash is 0,stop to buy
            if(cash == 0) return new Order();
        }
        Double totalCash = cash*leverage;

        quantity = totalCash/price;
        return addToOrderBook(member, coinName, price, buyOrders, quantity,State.BUY,leverage);
    }

    //Behaviour similar to when buying
    @Override
    public Order sellDesignatedPrice(Member member,String coinName, Double price, Double cash,Double leverage) {
        checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double quantity;

        Optional<Double> maxKey = Optional.ofNullable(buyOrders.isEmpty() ? null : buyOrders.firstKey());

        if(maxKey.isPresent() && (price <= maxKey.get())){
            cash = sellMarketPrice(member, coinName, price, cash,leverage);
            if(cash == 0) return new Order();
        }

        Double totalCash = cash*leverage;
        quantity = totalCash/price;
        return addToOrderBook(member,coinName,price,sellOrders,quantity,State.SELL,leverage);
    }

    // This parameter, Price, determines the pricing mode for the purchase:
    // If Price is 0, the purchase is made at the current market price.
    // If Price is not 0, the purchase is made at the designated price.
    @Override
    public Double buyMarketPrice(Member member,String coinName, Double price, Double cash,Double leverage) {
        if(price ==0) checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double totalCash = cash*leverage;

        boolean escape = false;

        while(totalCash > 0){
            Map.Entry<Double, List<Order>> firstEntry = sellOrders.firstEntry();
            Optional<List<Order>> optionalCurrentSellPriceList = Optional.ofNullable(firstEntry != null ? firstEntry.getValue() : null);
            Double quantity;

            //If there is no coin for sale,there are two cases
            //In the case of purchase at the market price from the beginning(parameter Price value is 0),it will no longer buy it
            //In the case of purchase at the designated price from the beginning(parameter Price value is not 0),it will buy it at a designated price
            if(optionalCurrentSellPriceList.isEmpty()){
                if(price == 0) {
                    break;
                }else{
                    quantity = totalCash/price;
                    addToOrderBook(member, coinName, price, buyOrders, quantity,State.BUY,leverage);
                }
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
                    if(totalCash >= currentOrderTotalPrice){
                        trade(member,currentOrder.getMember(),currentOrder.getCoinName(),currentOrder.getPrice(),
                                currentOrder.getQuantity(),leverage,currentOrder.getLeverage());
                        currentSellPriceList.remove(0);
                        totalCash -= currentOrderTotalPrice;
                    }else{
                        quantity = totalCash/currentOrder.getPrice();
                        trade(member,currentOrder.getMember(),currentOrder.getCoinName(),currentOrder.getPrice(),
                                quantity,leverage,currentOrder.getLeverage());
                        currentOrder.setQuantity(currentOrder.getQuantity()-quantity);
                        totalCash = 0.0;
                    }
                    if(totalCash == 0) break;
                }
                if(currentSellPriceList.isEmpty()) sellOrders.remove(firstEntry.getKey());
            }
            //log.info("totalCash = {}",totalCash);
            if(escape) break;
        }
        cash = totalCash/leverage;
        if(price == 0) addCash(member,cash);
        //log.info("Buy Market Finsih!");
        return cash;
    }

    //Behaviour similar to when buying
    @Override
    public Double sellMarketPrice(Member member,String coinName, Double price, Double cash,Double leverage) {
        if(price ==0) checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double totalCash = cash*leverage;

        boolean escape = false;

        while(totalCash > 0){
            Map.Entry<Double, List<Order>> firstEntry = buyOrders.firstEntry();
            Optional<List<Order>> optionalCurrentBuyPriceList = Optional.ofNullable(firstEntry != null ? firstEntry.getValue() : null);
            Double quantity;

            if(optionalCurrentBuyPriceList.isEmpty()){
                if(price == 0) {
                    break;
                }else{
                    quantity = totalCash/price;
                    addToOrderBook(member,coinName,price,sellOrders,quantity,State.SELL,leverage);
                }
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
                    if(totalCash >= currentOrderTotalPrice){
                        trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),
                                currentOrder.getQuantity(),currentOrder.getLeverage(),leverage);
                        currentBuyPriceList.remove(0);
                        totalCash -= currentOrderTotalPrice;
                    }else{
                        quantity = totalCash/currentOrder.getPrice();
                        trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),
                                quantity,currentOrder.getLeverage(),leverage);
                        currentOrder.setQuantity(currentOrder.getQuantity()-quantity);
                        totalCash = 0.0;
                    }
                    if(totalCash == 0) break;
                }
                if(currentBuyPriceList.isEmpty()) buyOrders.remove(firstEntry.getKey());
            }
            //log.info("totalCash = {}",totalCash);
            if(escape) break;
        }
        cash = totalCash/leverage;
        if(price == 0) addCash(member,cash);
        //log.info("Sell Market Finsih!");
        return cash;
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
            /**
             * ToDo Trade Market price by Opposite Postion(Wallet)
             */
        }
    }

    public void trade(Member buyMember, Member sellMember, String coinName, Double price, Double quantity,
                      Double buyMemberLeverage,Double sellMemberLeverage) {
        Order buyMemberOrder = new Order(coinName, price, quantity, State.BUY, buyMember,buyMemberLeverage);
        Order sellMemberOrder = new Order(coinName, price, quantity, State.SELL, sellMember,sellMemberLeverage);

        addOrderAndSetLiquidation(buyMember, sellMember, coinName, price, buyMemberOrder, sellMemberOrder);

        memberService.addCoin(buyMember,new Coin(coinName,quantity));
        memberService.subCoin(sellMember,new Coin(coinName,quantity));


        //log.info("trade success!");
        //log.info("buy order ={}",buyMemberOrder);
        //log.info("sell order ={}",sellMemberOrder);
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

        Double prevLeverageCash = prevOrder.getPrice()*prevOrder.getQuantity();
        Double prevCash = prevLeverageCash/prevOrder.getLeverage();
        Double newLeverageCash = newOrder.getPrice()* newOrder.getQuantity();
        Double newCash = newLeverageCash/newOrder.getLeverage();
        Double totalLeverageCash = prevLeverageCash + newLeverageCash;
        Double totalCash = prevCash + newCash;
        Double quantity = prevOrder.getQuantity() + newOrder.getQuantity();
        Double price = totalLeverageCash/quantity;
        Double leverage = totalLeverageCash/totalCash;

        prevOrder.setMember(member);
        prevOrder.setPrice(price);
        prevOrder.setQuantity(quantity);
        prevOrder.setState(newOrder.getState());
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

    private void checkCashAndRemove(Member member, Double cash){
        if(member.getWallet().get("KRW") < cash) throw new InsufficientCashException();
        memberService.subCoin(member,new Coin("KRW",cash));
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
    }
}
