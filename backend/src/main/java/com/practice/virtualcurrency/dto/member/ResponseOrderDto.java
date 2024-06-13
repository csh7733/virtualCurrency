package com.practice.virtualcurrency.dto.member;

import com.practice.virtualcurrency.domain.order.OrderType;
import com.practice.virtualcurrency.domain.order.State;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseOrderDto {
    private Long id;
    private String coinName;
    private String time;
    private OrderType orderType;
    private Double price;
    private Double quantity;
    private State state;
}
