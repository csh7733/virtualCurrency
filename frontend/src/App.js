// App.js
import React from 'react';
import { Route, Routes, Navigate } from 'react-router-dom';
import Dashboard from './components/home/Dashboard'; 
import Login from './components/login/Login'; 
import Register from './components/login/Register'; 
import TestData from './components/test/TestData'; 
import Layout from './components/Layout';
import Assets from './components/assets/Assets';
import Orders from './components/orders/Orders';
import Account from './components/account/Account';
import { MarketDataProvider } from './components/context/MarketData';
import { ChartDataProvider } from './components/context/ChartData';

function App() {
  return (
    <ChartDataProvider>
      <MarketDataProvider>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="/:coinName" element={<Dashboard />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/assets" element={<Assets />} />
            <Route path="/orders" element={<Orders />} />
            <Route path="/account" element={<Account />} />
          </Route>
        </Routes>
        <TestData coinName={"BTC"}/>
        <TestData coinName={"ETH"}/>
        <TestData coinName={"XRP"}/>
        <TestData coinName={"CSH"}/>
      </MarketDataProvider>
    </ChartDataProvider>
  );
}

export default App;