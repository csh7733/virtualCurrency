package com.practice.virtualcurrency.dto.market;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentMarketDataDto {
    private String coinName;
    private double price;
    private long time;
}
