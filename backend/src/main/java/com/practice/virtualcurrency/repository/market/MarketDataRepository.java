package com.practice.virtualcurrency.repository.market;

import com.practice.virtualcurrency.domain.market.MarketData;
import com.practice.virtualcurrency.dto.market.MarketDataDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarketDataRepository extends JpaRepository<MarketData, Long> {
    List<MarketData> findTop100ByCoinNameOrderByTimeDesc(String coinName);

    @Modifying
    @Query(value = "DELETE FROM MARKET_DATA m WHERE m.id IN (SELECT id FROM MARKET_DATA ORDER BY time ASC LIMIT 200)", nativeQuery = true)
    void deleteOldestEntries();
}
