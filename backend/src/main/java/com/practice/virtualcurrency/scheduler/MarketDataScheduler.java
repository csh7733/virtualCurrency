package com.practice.virtualcurrency.scheduler;

import com.practice.virtualcurrency.domain.market.MarketData;
import com.practice.virtualcurrency.dto.market.CurrentMarketDataDto;
import com.practice.virtualcurrency.service.market.MarketDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class MarketDataScheduler {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private MarketDataService marketDataService;
    private final Map<String, CoinData> coins = new HashMap<>();

    public MarketDataScheduler() {
        long initialTime = System.currentTimeMillis() / 1000;
        coins.put("BTC", new CoinData(initialTime, 7296.62, 7296.62, 7313.76, 7293.05, 7296.62));
        coins.put("ETH", new CoinData(initialTime, 7296.62, 7296.62, 7313.76, 7293.05, 7296.62));
        coins.put("XRP", new CoinData(initialTime, 7296.62, 7296.62, 7313.76, 7293.05, 7296.62));
        coins.put("CSH", new CoinData(initialTime, 7296.62, 7296.62, 7313.76, 7293.05, 7296.62));
    }

    @Scheduled(fixedRate = 20)
    public void sendMarketData() {
        for (String coinName : coins.keySet()) {
            simulatePriceUpdate(coinName);
        }
    }

    private void simulatePriceUpdate(String coinName) {
        CoinData coin = coins.get(coinName);
        coin.setCurrentTime(coin.getCurrentTime() + 1);
        long roundedTime = (coin.getCurrentTime() / 60) * 60;

        double priceChange = ThreadLocalRandom.current().nextDouble(-0.01, 0.01) * coin.getCurrentPrice();
        double newPrice = coin.getCurrentPrice() + priceChange;

        if (coin.getLastCandleTime() != 0 && coin.getLastCandleTime() == roundedTime) {
            coin.setHigh(Math.max(coin.getHigh(), newPrice));
            coin.setLow(Math.min(coin.getLow(), newPrice));
            coin.setClose(newPrice);
        } else {
            MarketData marketData = new MarketData(
                    coinName,
                    coin.getOpen(),
                    coin.getHigh(),
                    coin.getLow(),
                    coin.getClose(),
                    coin.getLastCandleTime()
            );
            marketDataService.saveMarketData(marketData);
            Double price = coin.getLastCandleTime() != 0 ? coin.getClose() : newPrice;
            coin.setOpen(price);
            coin.setHigh(price);
            coin.setLow(price);
            coin.setClose(newPrice);
            coin.setLastCandleTime(roundedTime);
        }

        coin.setCurrentPrice(newPrice);
        coin.setCurrentTime(coin.getCurrentTime());

        CurrentMarketDataDto marketDataDto = new CurrentMarketDataDto(coinName, newPrice, coin.getCurrentTime());
        template.convertAndSend("/topic/market-data/" + coinName, marketDataDto);
    }

    private static class CoinData {
        private long currentTime;
        private double currentPrice;
        private double open;
        private double high;
        private double low;
        private double close;
        private long lastCandleTime;

        public CoinData(long currentTime, double currentPrice, double open, double high, double low, double close) {
            this.currentTime = currentTime;
            this.currentPrice = currentPrice;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.lastCandleTime = 0L;
        }

        // Getters and setters
        public long getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
        }

        public double getOpen() {
            return open;
        }

        public void setOpen(double open) {
            this.open = open;
        }

        public double getHigh() {
            return high;
        }

        public void setHigh(double high) {
            this.high = high;
        }

        public double getLow() {
            return low;
        }

        public void setLow(double low) {
            this.low = low;
        }

        public double getClose() {
            return close;
        }

        public void setClose(double close) {
            this.close = close;
        }

        public long getLastCandleTime() {
            return lastCandleTime;
        }

        public void setLastCandleTime(long lastCandleTime) {
            this.lastCandleTime = lastCandleTime;
        }
    }

}
