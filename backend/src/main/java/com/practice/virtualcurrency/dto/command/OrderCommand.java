package com.practice.virtualcurrency.dto.command;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.OrderType;
import com.practice.virtualcurrency.domain.order.Trade;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderCommand {
    private OrderType orderType;
    private String username;
    private String coinName;
    private Double price;
    @Builder.Default
    private Double cash = 0.0;
    @Builder.Default
    private Double quantity = 0.0;
    private Double leverage;
    private Trade trade;
}
