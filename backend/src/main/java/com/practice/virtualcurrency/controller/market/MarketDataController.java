package com.practice.virtualcurrency.controller.market;

import com.practice.virtualcurrency.dto.market.MarketDataDto;
import com.practice.virtualcurrency.service.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/market-data")
public class MarketDataController {
    private final MarketDataService marketDataService;

    @GetMapping("/{coinName}")
    public List<MarketDataDto> getInitialMarketData(@PathVariable String coinName) {
        List<MarketDataDto> initialMarketData = marketDataService.getInitialMarketData(coinName);
        return initialMarketData;
    }
}
