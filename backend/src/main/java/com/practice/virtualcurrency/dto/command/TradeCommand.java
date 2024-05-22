package com.practice.virtualcurrency.dto.command;

import com.practice.virtualcurrency.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCommand {
    private Member buyMember;
    private Member sellMember;
    private String coinName;
    private Double price;
    private Double quantity;
    private Double buyMemberLeverage;
    private Double sellMemberLeverage;
}
