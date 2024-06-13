package com.practice.virtualcurrency.service.market;

import com.practice.virtualcurrency.domain.market.MarketData;
import com.practice.virtualcurrency.dto.market.MarketDataDto;
import com.practice.virtualcurrency.repository.market.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MarketDataService {

    private final MarketDataRepository marketDataRepository;

    public List<MarketDataDto> getInitialMarketData(String coinName) {
        List<MarketData> marketDataList = marketDataRepository.findTop100ByCoinNameOrderByTimeDesc(coinName);
        return marketDataList.stream()
                .sorted(Comparator.comparingLong(MarketData::getTime))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void saveMarketData(MarketData marketData) {
        Long count = marketDataRepository.count();
        if (count >= 1000) {
            marketDataRepository.deleteOldestEntries();
        }
        marketDataRepository.save(marketData);
    }

    private MarketDataDto convertToDto(MarketData marketData) {
        return new MarketDataDto(marketData.getCoinName(), marketData.getOpen(), marketData.getHigh(), marketData.getLow(), marketData.getClose(), marketData.getTime());
    }
}
