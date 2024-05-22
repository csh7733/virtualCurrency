import * as React from 'react';
import { useEffect, useState } from 'react';
import { Typography, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import Title from './Title';
import { useMarketData } from '../context/MarketData';

const OrderBook = ({ coinName }) => {
  const { marketData } = useMarketData();
  const price = marketData[coinName];
  const fixedPrice = price ? price.toFixed(2) : 'Loading...';

  const [bids, setBids] = useState([]);
  const [asks, setAsks] = useState([]);
  const [trades, setTrades] = useState([]);

  useEffect(() => {
    // Fetch bids, asks, and trades data from the market data context or an API
    setBids([
      { price: 61747.9, size: 3.540 },
      { price: 61746.9, size: 0.024 },
      { price: 61746.0, size: 0.002 },
      { price: 61745.9, size: 0.080 },
      { price: 61745.8, size: 0.002 },
      { price: 61745.5, size: 0.002 },
      { price: 61744.4, size: 0.016 },
    ]);
    setAsks([
      { price: 61750.2, size: 0.002 },
      { price: 61750.1, size: 0.750 },
      { price: 61750.0, size: 0.161 },
      { price: 61749.9, size: 0.381 },
      { price: 61749.8, size: 0.007 },
      { price: 61749.5, size: 0.002 },
      { price: 61748.0, size: 10.160 },
    ]);
    setTrades([
      { price: 61907.3, amount: 14.804, time: '15:38:55' },
      { price: 61901.9, amount: 1.300, time: '15:38:54' },
      { price: 61905.9, amount: 0.291, time: '15:38:54' },
      { price: 61906.0, amount: 0.793, time: '15:38:53' },
      { price: 61905.9, amount: 0.422, time: '15:38:53' },
      { price: 61906.0, amount: 0.368, time: '15:38:52' },
      { price: 61905.9, amount: 1.031, time: '15:38:52' },
    ]);
  }, [coinName]);

  return (
    <React.Fragment>
      <Title>Order Book - {coinName}</Title>
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <Paper variant="outlined" sx={{ padding: 2, height: '270px', overflow: 'auto' }}>
            <TableContainer>
              <Table size="small" aria-label="asks table">
                <TableHead>
                  <TableRow>
                    <TableCell>Price(USDT)</TableCell>
                    <TableCell>Size({coinName})</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {asks.slice(0, 5).map((ask, index) => (
                    <TableRow key={index}>
                      <TableCell sx={{ color: 'red' }}>{ask.price.toFixed(1)}</TableCell>
                      <TableCell>{ask.size}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper variant="outlined" sx={{ padding: 2, height: '270px', overflow: 'auto' }}>
            <TableContainer>
              <Table size="small" aria-label="bids table">
                <TableHead>
                  <TableRow>
                    <TableCell>Price(USDT)</TableCell>
                    <TableCell>Size({coinName})</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {bids.slice(0, 5).map((bid, index) => (
                    <TableRow key={index}>
                      <TableCell sx={{ color: 'green' }}>{bid.price.toFixed(1)}</TableCell>
                      <TableCell>{bid.size}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper variant="outlined" sx={{ padding: 2, height: '270px', overflow: 'auto' }}>
            <TableContainer sx={{ overflowX: 'hidden' }}>
              <Table size="small" aria-label="trades table">
                <TableHead>
                  <TableRow>
                    <TableCell>Price(USDT)</TableCell>
                    <TableCell>Amount({coinName})</TableCell>
                    <TableCell>Time</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {trades.slice(0, 5).map((trade, index) => (
                    <TableRow key={index}>
                      <TableCell sx={{ color: trade.price > price ? 'green' : 'red' }}>
                        {trade.price.toFixed(1)}
                      </TableCell>
                      <TableCell>{trade.amount.toFixed(3)}</TableCell>
                      <TableCell>{trade.time}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </React.Fragment>
  );
};

export default OrderBook;
