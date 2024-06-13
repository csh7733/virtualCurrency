// MarketDataContext.js
import React, { createContext, useState, useContext, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const MarketDataContext = createContext(null);

export const useMarketData = () => useContext(MarketDataContext);

export const MarketDataProvider = ({ children }) => {
  const [marketData, setMarketData] = useState({
    BTC: 0,
    ETH: 0,
    XRP: 0,
    CSH: 0
  });

  const updateMarketData = newData => {
    setMarketData(prevData => ({
      ...prevData,
      ...newData
    }));
  };

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);

    stompClient.debug = () => {};
    stompClient.connect({}, () => {
      ['BTC', 'ETH', 'XRP', 'CSH'].forEach(coinName => {
        stompClient.subscribe(`/topic/market-data/${coinName}`, (message) => {
          const newData = JSON.parse(message.body);
          const { price } = newData;
          updateMarketData({ [coinName]: price });
        });
      });
    });

    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  return (
    <MarketDataContext.Provider value={{ marketData, updateMarketData }}>
      {children}
    </MarketDataContext.Provider>
  );
};
