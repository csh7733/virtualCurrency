import React, { useEffect, useRef, useState } from "react";
import { createChart, CrosshairMode } from "lightweight-charts";
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useMarketData } from '../../context/MarketData';
import apiClient from '../../../apiClient'; // apiClient가 필요한 부분을 추가하세요.

const Chart = ({ coinName }) => {
  const chartContainerRef = useRef();
  const candleSeriesRef = useRef();
  const chart = useRef();
  const resizeObserver = useRef();
  const { marketData, updateMarketData } = useMarketData();
  const [localChartData, setLocalChartData] = useState({
    BTC: [],
    ETH: [],
    XRP: [],
    CSH: []
  });

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const response = await apiClient.get(`/market-data/${coinName}`);
        const initialData = response.data.map(item => ({
          time: item.time,
          open: item.open,
          high: item.high,
          low: item.low,
          close: item.close,
        }));
        setLocalChartData(prevData => ({
          ...prevData,
          [coinName]: initialData
        }));

        candleSeriesRef.current.setData(initialData); // 차트에 초기 데이터 설정
      } catch (error) {
        console.error("Failed to fetch initial data:", error);
      }
    };

    fetchInitialData();

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
    candleSeriesRef.current = candleSeries;

    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

    stompClient.connect({}, () => {
      stompClient.subscribe(`/topic/market-data/${coinName}`, (message) => {
        const newData = JSON.parse(message.body);
        const { price, time } = newData;
        const roundedTime = Math.floor(time / 60) * 60; // 1분 간격으로 반올림

        setLocalChartData(prevData => {
          const coinData = prevData[coinName] || [];
          const lastCandle = coinData.length > 0 ? coinData[coinData.length - 1] : null;

          if (lastCandle && lastCandle.time === roundedTime) {
            const updatedCandle = {
              ...lastCandle,
              high: Math.max(lastCandle.high, price),
              low: Math.min(lastCandle.low, price),
              close: price
            };
            candleSeries.update(updatedCandle); // 차트에 업데이트 반영
            return {
              ...prevData,
              [coinName]: coinData.map(candle =>
                candle.time === roundedTime ? updatedCandle : candle
              )
            };
          } else {
            const newCandle = {
              time: roundedTime,
              open: lastCandle ? lastCandle.close : price,
              high: price,
              low: price,
              close: price
            };
            candleSeries.update(newCandle); // 새 캔들 차트에 추가
            return {
              ...prevData,
              [coinName]: [...coinData, newCandle]
            };
          }
        });
      });
    });

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
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, [coinName]);

  return (
    <div className="chart-container">
      <div ref={chartContainerRef} />
    </div>
  );
}

export default Chart;
