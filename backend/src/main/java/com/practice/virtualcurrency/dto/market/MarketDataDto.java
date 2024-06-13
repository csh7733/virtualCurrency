package com.practice.virtualcurrency.dto.market;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MarketDataDto {
    private String coinName;
    private double open;
    private double high;
    private double low;
    private double close;
    private long time;
}