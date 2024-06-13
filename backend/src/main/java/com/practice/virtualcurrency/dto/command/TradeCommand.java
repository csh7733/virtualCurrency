package com.practice.virtualcurrency.dto.command;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCommand {
    private String time;
    private OrderType orderType;
    private String buyMemberUsername;
    private String sellMemberUsername;
    private String coinName;
    private Double price;
    private Double quantity;
    private Double buyMemberLeverage;
    private Double sellMemberLeverage;
}
