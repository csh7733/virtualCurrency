// src/App.js
import React from 'react';
import { Route, Routes } from 'react-router-dom';
import Dashboard from './components/home/Dashboard'; 
import Login from './components/login/Login'; 
import Register from './components/login/Register';  
import Layout from './components/Layout';
import Assets from './components/assets/Assets';
import Orders from './components/orders/Orders';
import Account from './components/account/Account';
import { MarketDataProvider } from './components/context/MarketData';
import PrivateRoute from './components/PrivateRoute';

function App() {
  return (
    <MarketDataProvider>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="/:coinName" element={<Dashboard />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/assets" element={<PrivateRoute element={<Assets />} />} />
          <Route path="/orders" element={<PrivateRoute element={<Orders />} />} />
          <Route path="/account" element={<PrivateRoute element={<Account />} />} />
        </Route>
      </Routes>
    </MarketDataProvider>
  );
}

export default App;
