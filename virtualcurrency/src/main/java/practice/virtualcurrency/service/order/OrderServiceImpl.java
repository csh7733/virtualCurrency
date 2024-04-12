package practice.virtualcurrency.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private Map<String, TreeMap<Double, List<Order>>> buyOrdersMap = new HashMap<>();
    private Map<String, TreeMap<Double, List<Order>>> sellOrdersMap = new HashMap<>();

    public Order save(Member member, Order order) {
        memberService.addOrder(member,order);
        return orderRepository.save(order);
    }

    @Override
    public void buyDesignatedPrice(Member member,String coinName, Double price, Double cash) {
        checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double quantity;

        Optional<Double> minKey = Optional.ofNullable(sellOrders.isEmpty() ? null : sellOrders.firstKey());

        //If the designated price is greater than the market price
        //Buy at a market price instead of a designated price
        if(minKey.isPresent() && (price >= minKey.get())) {
            cash = buyMarketPrice(member, coinName, price, cash);
            if(cash == 0) return;
        }

        quantity = cash/price;
        List<Order> currentBuyPriceList = buyOrders.computeIfAbsent(price, k -> new LinkedList<>());
        currentBuyPriceList.add(new Order(coinName,price,quantity,State.BUY,member));
    }
    //Behaviour similar to when buying
    @Override
    public void sellDesignatedPrice(Member member,String coinName, Double price, Double cash) {
        checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double quantity;

        Optional<Double> maxKey = Optional.ofNullable(buyOrders.isEmpty() ? null : buyOrders.firstKey());

        if(maxKey.isPresent() && (price <= maxKey.get())){
            cash = sellMarketPrice(member, coinName, price, cash);
            if(cash == 0) return;
        }

        quantity = cash/price;
        List<Order> currentSellPriceList = sellOrders.computeIfAbsent(price, k -> new LinkedList<>());
        currentSellPriceList.add(new Order(coinName,price,quantity,State.SELL,member));
    }

    // This parameter, Price, determines the pricing mode for the purchase:
    // If Price is 0, the purchase is made at the current market price.
    // If Price is not 0, the purchase is made at the designated price.
    @Override
    public Double buyMarketPrice(Member member,String coinName, Double price, Double cash) {
        if(price ==0) checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));


        boolean escape = false;

        while(cash > 0){
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
                    quantity = cash/price;
                    List<Order> currentBuyPriceList = buyOrders.computeIfAbsent(price, k -> new LinkedList<>());
                    currentBuyPriceList.add(new Order(coinName, price, quantity, State.BUY,member));
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
                    if(cash >= currentOrderTotalPrice){
                        trade(member,currentOrder.getMember(),currentOrder.getCoinName(),currentOrder.getPrice(),currentOrder.getQuantity());
                        currentSellPriceList.remove(0);
                        cash -= currentOrderTotalPrice;
                    }else{
                        quantity = cash/currentOrder.getPrice();
                        trade(member,currentOrder.getMember(),currentOrder.getCoinName(),currentOrder.getPrice(),quantity);
                        currentOrder.setQuantity(currentOrder.getQuantity()-quantity);
                        cash = 0.0;
                    }
                    if(cash == 0) break;
                }
                if(currentSellPriceList.isEmpty()) sellOrders.remove(firstEntry.getKey());
            }
            log.info("cash = {}",cash);
            if(escape) break;
        }
        if(price == 0) addRemainCash(member,cash);
        log.info("Buy Market Finsih!");
        return cash;
    }

    //Behaviour similar to when buying
    @Override
    public Double sellMarketPrice(Member member,String coinName, Double price, Double cash) {
        if(price ==0) checkCashAndRemove(member,cash);

        TreeMap<Double, List<Order>> buyOrders = buyOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.reverseOrder()));
        TreeMap<Double, List<Order>> sellOrders = sellOrdersMap.computeIfAbsent(coinName, k -> new TreeMap<>(Comparator.naturalOrder()));
        Double quantity;

        boolean escape = false;

        while(cash > 0){
            Map.Entry<Double, List<Order>> firstEntry = buyOrders.firstEntry();
            Optional<List<Order>> optionalCurrentBuyPriceList = Optional.ofNullable(firstEntry != null ? firstEntry.getValue() : null);

            if(optionalCurrentBuyPriceList.isEmpty()){
                if(price == 0) {
                    break;
                }else{
                    quantity = cash/price;
                    List<Order> currentSellPriceList = sellOrders.computeIfAbsent(price, k -> new LinkedList<>());
                    currentSellPriceList.add(new Order(coinName, price, quantity, State.SELL,member));
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
                    if(cash >= currentOrderTotalPrice){
                        trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),currentOrder.getQuantity());
                        currentBuyPriceList.remove(0);
                        cash -= currentOrderTotalPrice;
                    }else{
                        quantity = cash/currentOrder.getPrice();
                        trade(currentOrder.getMember(),member,currentOrder.getCoinName(),currentOrder.getPrice(),quantity);
                        currentOrder.setQuantity(currentOrder.getQuantity()-quantity);
                        cash = 0.0;
                    }
                    if(cash == 0) break;
                }
                if(currentBuyPriceList.isEmpty()) buyOrders.remove(firstEntry.getKey());
            }
            log.info("cash = {}",cash);
            if(escape) break;
        }
        if(price == 0) addRemainCash(member,cash);
        log.info("Sell Market Finsih!");
        return cash;
    }

    public void trade(Member buyMember, Member sellMember, String coinName, Double price, Double quantity) {
        Order buyMemberOrder = new Order(coinName, price, quantity, State.BUY, buyMember);
        Order sellMemberOrder = new Order(coinName, price, quantity, State.SELL, sellMember);
        save(buyMember,buyMemberOrder);
        save(sellMember,sellMemberOrder);

        memberService.addCoin(buyMember,new Coin(coinName,quantity));
        memberService.subCoin(sellMember,new Coin(coinName,quantity));

        log.info("trade success!");
        log.info("buy order ={}",buyMemberOrder);
        log.info("sell order ={}",sellMemberOrder);
    }

    private void checkCashAndRemove(Member member, Double cash){
        if(member.getWallet().get("KRW") < cash) throw new InsufficientCashException();
        memberService.subCoin(member,new Coin("KRW",cash));
    }

    private void addRemainCash(Member member,Double cash){
        memberService.addCoin(member,new Coin("KRW",cash));
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
