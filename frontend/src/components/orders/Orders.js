import React, { useState } from 'react';
import { Container, Typography, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, Tab } from '@mui/material';
import { styled } from '@mui/material/styles';

const CustomTableContainer = styled(TableContainer)(({ theme }) => ({
  backgroundColor: theme.palette.background.default,
}));

const ordersData = [
  { time: '2024-04-14 07:24:18', symbol: 'BTC', orderType: 'Liquidation', position: 'Long', size: '3.82542 BTC', price: '5.9681378', total: '2,054.8299 USDT'},
  { time: '2024-04-14 06:48:53', symbol: 'CSH', orderType: 'Market', position: 'Short', size: '1,997.51332 CSH', price: '5.80167464', total: '1,997.5166 USDT'},
  // Add more orders as needed
];

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

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

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
