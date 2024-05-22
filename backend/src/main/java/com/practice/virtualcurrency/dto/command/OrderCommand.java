package com.practice.virtualcurrency.dto.command;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Trade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCommand {
    private Member member;
    private String coinName;
    private Double price;
    private Double cash;
    private Double quantity;
    private Double leverage;
    private Trade trade;
}
