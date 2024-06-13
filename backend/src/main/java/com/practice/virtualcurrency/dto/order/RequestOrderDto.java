package com.practice.virtualcurrency.dto.order;

import com.practice.virtualcurrency.domain.order.OrderType;
import com.practice.virtualcurrency.domain.order.Trade;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestOrderDto {
    private OrderType orderType;
    private Trade trade;
    private String coinName;
    private Double price;
    private Double size;
    private Double leverage;
    private String position;
}
