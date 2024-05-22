import { createChart, CrosshairMode } from "lightweight-charts";
import { useEffect, useRef, useState } from "react";
import { priceData as initialPriceData } from "./priceData";
import { useChartData } from '../../context/ChartData';
import { useMarketData } from '../../context/MarketData';

const Chart = ({ coinName }) => {
  const chartContainerRef = useRef();
  const chartInstanceRef = useRef();
  const candleSeriesRef = useRef();
  const chart = useRef();
  const resizeObserver = useRef();
  const socket = useRef(null);
  const { chartData, setChartData} = useChartData();
  const { marketData, updateMarketData } = useMarketData();

  useEffect(() => {
    chart.current = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: 550,
      layout: {
        backgroundColor: "#121212",
        textColor: "rgba(255, 255, 255, 0.9)" 
      },
      grid: {
        vertLines: {
          color: "#334158"
        },
        horzLines: {
          color: "#334158"
        }
      },
      crosshair: {
        mode: CrosshairMode.Normal
      },
      priceScale: {
        borderColor: "#485c7b"
      },
      timeScale: {
        borderColor: "#485c7b",
        timeVisible: true,
        secondsVisible: false
      }
    });

    const candleSeries = chart.current.addCandlestickSeries({
      upColor: "#4bffb5",
      downColor: "#ff4976",
      borderDownColor: "#ff4976",
      borderUpColor: "#4bffb5",
      wickDownColor: "#ff4976",
      wickUpColor: "#4bffb5" 
    });
    candleSeries.setData(chartData[coinName]);
    candleSeriesRef.current = candleSeries; 

    socket.current = new WebSocket('wss://your-websocket-url//{coinName}');
    socket.current.onmessage = (event) => {
      const newData = JSON.parse(event.data);
      updateMarketData(newData);
      const { coinName, price, time } = newData;
      const roundedTime = Math.floor(time / 60) * 60; // 1분 간격으로 반올림
    
      setChartData(prevData => {
        const coinData = prevData[coinName] || [];
        const lastCandle = coinData.length > 0 ? coinData[coinData.length - 1] : null;
    
        if (lastCandle && lastCandle.time === roundedTime) {
          // 현재 분의 데이터를 업데이트
          const updatedCandle = {
            ...lastCandle,
            high: Math.max(lastCandle.high, price),
            low: Math.min(lastCandle.low, price),
            close: price  // 가장 최근 가격을 close로 업데이트
          };
          candleSeries.update(updatedCandle);  // 차트에 업데이트 반영
          return {
            ...prevData,
            [coinName]: coinData.map(candle => 
              candle.time === roundedTime ? updatedCandle : candle
            )
          };
        } else {
          // 새로운 분의 첫 데이터라면 새 캔들 생성
          const newCandle = {
            time: roundedTime,
            open: lastCandle ? lastCandle.close : price,
            high: lastCandle ? lastCandle.close : price,
            low: lastCandle ? lastCandle.close : price,
            close: price
          };
          candleSeries.update(newCandle);  // 새 캔들 차트에 추가
          return {
            ...prevData,
            [coinName]: [...coinData, newCandle]
          };
        }
      });
    };

    resizeObserver.current = new ResizeObserver(entries => {
      const { width, height } = entries[0].contentRect;
      requestAnimationFrame(() => {
        if (chart.current) {
          chart.current.applyOptions({ width, height });
          chart.current.timeScale().fitContent();
        }
      });
    });
    resizeObserver.current.observe(chartContainerRef.current);

    return () => {
      if (resizeObserver.current) {
        resizeObserver.current.disconnect();
      }
      if (chart.current) {
        chart.current.remove();
        chart.current = null;
      }
      if (socket.current) {
        socket.current.close();
      }
    };
  }, []);

  useEffect(() => {
    const candleSeries = candleSeriesRef.current;
    if (candleSeries) {
      candleSeries.update(chartData[coinName][chartData[coinName].length-1]); // 저장된 캔들 시리즈 참조를 사용하여 데이터 업데이트
    }
  }, [chartData, coinName]);

  return (
    <div className="chart-container">
        <div ref={chartContainerRef} />
    </div>
);
}

export default Chart;