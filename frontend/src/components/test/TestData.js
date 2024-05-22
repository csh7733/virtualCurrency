import { useEffect, useRef, useState } from "react";
import { useChartData } from '../context/ChartData';
import { useMarketData } from '../context/MarketData';

 const TestData = ({ coinName }) =>  {
    const { setChartData} = useChartData();
    const { updateMarketData } = useMarketData();
    
    /**
     * Test
     */
    // 데이터 처리 및 차트 업데이트 함수
    function processMarketData(newData) {
        const { coinName, price, time } = newData;
        updateMarketData(prevData => {
            return {...prevData, [coinName] : price}
        });
        const roundedTime = Math.floor(time / 60) * 60;
      
        setChartData(prevData => {
          const coinData = prevData[coinName] || [];
          const lastCandle = coinData.length > 0 ? coinData[coinData.length - 1] : null;
      
          if (lastCandle && lastCandle.time === roundedTime) {
            // 현재 분의 데이터를 업데이트
            const updatedCandle = {
              ...lastCandle,
              high: Math.max(lastCandle.high, price),
              low: Math.min(lastCandle.low, price),
              close: price
            };
            return { ...prevData, [coinName]: coinData.map(candle => candle.time === roundedTime ? updatedCandle : candle) };
          } else {
            // 새로운 분의 첫 데이터라면 새 캔들 생성
            const newCandle = {
              time: roundedTime,
              open: lastCandle ? lastCandle.close : price,
              high: lastCandle ? lastCandle.close : price,
              low: lastCandle ? lastCandle.close : price,
              close: price
            };
            return { ...prevData, [coinName]: [...coinData, newCandle] };
          }
        });
      }
      
  
  // 시뮬레이션 시작
  const BTCTime = 1575221940;  // 초기 시간 설정
  const ETHTime = 1575216720;  // 초기 시간 설정
  
  // 각 코인별 초기 상태 설정
  let coins = {
    BTC: { currentTime: BTCTime, currentPrice: 7307.35, high: 7319.24, low: 7307.35 },
    ETH: { currentTime: ETHTime, currentPrice: 7329.85, high: 7344.4, low: 7326.15 },
    XRP: { currentTime: ETHTime, currentPrice: 7329.85, high: 7344.4, low: 7326.15 },
    CSH: { currentTime: ETHTime, currentPrice: 7329.85, high: 7344.4, low: 7326.15 }
  }
  
  function simulatePriceUpdate(coinName) {
    let coin = coins[coinName];
    coin.currentTime += 1;  
    let priceChange = (Math.random() * (coin.high - coin.low) - (coin.high - coin.low) / 2) * 0.3;
    coin.currentPrice += priceChange;
  
    // 시뮬레이션된 데이터와 코인 이름을 processMarketData에 전달
    processMarketData({ time: coin.currentTime, price: coin.currentPrice, coinName : coinName });
  
    setTimeout(() => simulatePriceUpdate(coinName), 20);  // 다음 업데이트를 위해 setTimeout 재귀적 호출
  }
  
  useEffect(() => {
    simulatePriceUpdate(coinName);
  }, []);


  return (
    <div></div>
  );
}
export default TestData;
