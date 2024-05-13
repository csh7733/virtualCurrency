// MarketDataContext.js
import React, { createContext, useState, useContext } from 'react';

const MarketDataContext = createContext(null);

export const useMarketData = () => useContext(MarketDataContext);

export const MarketDataProvider = ({ children }) => {
  const [marketData, setMarketData] = useState({
    BTC : 0,
    ETH : 0,
    XRP : 0,
    CSH : 0
  });

  const updateMarketData = newData => {
    setMarketData(newData);
  };

  return (
    <MarketDataContext.Provider value={{ marketData, updateMarketData }}>
      {children}
    </MarketDataContext.Provider>
  );
};
