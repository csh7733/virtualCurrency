import React, { useState, useEffect } from 'react';
import { Container, Typography, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, Tab } from '@mui/material';
import { styled } from '@mui/material/styles';
import axios from 'axios';
import apiClient from '../../apiClient';

const CustomTableContainer = styled(TableContainer)(({ theme }) => ({
  backgroundColor: theme.palette.background.default,
}));

const OrderRow = ({ order }) => (
  <TableRow>
    <TableCell>{order.time}</TableCell>
    <TableCell>{order.symbol}</TableCell>
    <TableCell>{order.orderType}</TableCell>
    <TableCell sx={{ color: order.position === 'Long' ? 'green' : 'red' }}>{order.position}</TableCell>
    <TableCell>{order.size}</TableCell>
    <TableCell>{order.price}</TableCell>
    <TableCell>{order.total}</TableCell>
  </TableRow>
);

const Orders = () => {
  const [value, setValue] = useState(0);
  const [ordersData, setOrdersData] = useState([]);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await apiClient.get('member/orders');
        const orders = response.data.map(order => ({
          time: order.time,
          symbol: order.coinName,
          orderType: order.orderType,
          position: order.state === 'BUY' ? 'Long' : 'Short',
          size: `${order.quantity.toFixed(2)}. ${order.coinName}`,
          price: order.price.toFixed(2),
          total: (order.price * order.quantity).toFixed(2),
        }));
        setOrdersData(orders);
      } catch (error) {
        console.error('Failed to fetch orders:', error);
      }
    };

    fetchOrders();
  }, []);

  return (
    <Container>
      <Paper sx={{ flexGrow: 1, backgroundColor: 'background.paper' }}>
        <Tabs value={value} onChange={handleChange} indicatorColor="primary" textColor="inherit" centered>
          <Tab label="Order History" />
        </Tabs>
        <CustomTableContainer component={Paper} sx={{ mt: 2 }}>
          <Table aria-label="orders table">
            <TableHead>
              <TableRow>
                <TableCell>Time</TableCell>
                <TableCell>Symbol</TableCell>
                <TableCell>Order Type</TableCell>
                <TableCell>Position</TableCell>
                <TableCell>Size</TableCell>
                <TableCell>Price</TableCell>
                <TableCell>Total</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ordersData.map((order, index) => (
                <OrderRow key={index} order={order} />
              ))}
            </TableBody>
          </Table>
        </CustomTableContainer>
      </Paper>
    </Container>
  );
};

export default Orders;
