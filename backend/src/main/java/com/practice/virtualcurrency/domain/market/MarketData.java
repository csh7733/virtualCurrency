package com.practice.virtualcurrency.domain.market;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String coinName;
    private double open;
    private double high;
    private double low;
    private double close;
    private long time;

    public MarketData(String coinName, double open, double high, double low, double close, long time) {
        this.coinName = coinName;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.time = time;
    }
}
