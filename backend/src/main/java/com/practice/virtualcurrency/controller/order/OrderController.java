package com.practice.virtualcurrency.controller.order;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.OrderType;
import com.practice.virtualcurrency.domain.order.Trade;
import com.practice.virtualcurrency.dto.command.OrderCommand;
import com.practice.virtualcurrency.dto.order.RequestOrderDto;
import com.practice.virtualcurrency.service.member.MemberService;
import com.practice.virtualcurrency.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.practice.virtualcurrency.VirtualCurrencyConst.getCurrentMemberUsername;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    @PostMapping()
    public String order(@RequestBody RequestOrderDto requestOrderDto) {
        OrderType orderType = requestOrderDto.getOrderType();
        Trade trade = requestOrderDto.getTrade();
        String username = getCurrentMemberUsername();
        String position = requestOrderDto.getPosition();

        OrderCommand orderCommand = OrderCommand.builder()
                .orderType(orderType)
                .username(username)
                .coinName(requestOrderDto.getCoinName())
                .price(requestOrderDto.getPrice())
                .leverage(requestOrderDto.getLeverage())
                .trade(trade)
                .build();

        if (orderType.equals(OrderType.LIMIT)) {
            if(trade.equals(Trade.PRICE)){
                orderCommand.setCash(requestOrderDto.getSize());
            }else if(trade.equals(Trade.QUANTITY)){
                orderCommand.setQuantity(requestOrderDto.getSize());
            }

            if(position.equals("LONG")) orderService.buyDesignatedPrice(orderCommand);
            else if(position.equals("SHORT")) orderService.sellDesignatedPrice(orderCommand);
        }else if(orderType.equals(OrderType.MARKET)){
            if(trade.equals(Trade.PRICE)){
                orderCommand.setPrice(0.0);
                orderCommand.setCash(requestOrderDto.getSize());
            }

            if(position.equals("LONG")) orderService.buyMarketPrice(orderCommand);
            else if(position.equals("SHORT")) orderService.sellMarketPrice(orderCommand);
        }

        log.info(orderCommand.toString());

        return "ok";
    }
}
