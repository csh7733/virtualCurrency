import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { styled, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import Typography from '@mui/material/Typography';
import Chart from './chart/Chart';
import Current from './Current';
import Orders from './Positions';
import darkTheme from '../Theme';
import TradeOptions from './TradeOptions';
import OrderBook from './OrderBook';
import Copyright from '../Copyright';

export default function Dashboard() {
  const [showOrderBook, setShowOrderBook] = useState(false);
  const params = useParams();
  const coinName = params.coinName || 'BTC';
  const [selectedCoin, setSelectedCoin] = useState(coinName);

  const handleChange = (event) => {
    window.location.href = `/${event.target.value}`;
  };

  const handleToggleOrderBook = () => {
    setShowOrderBook((prev) => !prev);
  };

  return (
    <ThemeProvider theme={darkTheme}>
      <Box sx={{ display: 'flex' }}>
        <CssBaseline />
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
          <Grid container spacing={3}>
            {/* Chart */}
            <Grid item xs={12} md={8} lg={9}>
              <Paper
                sx={{
                  p: 2,
                  display: 'flex',
                  flexDirection: 'column',
                  height: 'auto',
                  position: 'relative', // Ensure relative positioning for inner elements
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Select
                    value={selectedCoin}
                    onChange={handleChange}
                    displayEmpty
                    inputProps={{ 'aria-label': 'Without label' }}
                    sx={{
                      '& .MuiOutlinedInput-notchedOutline': {
                        border: 'none', // Remove border
                      },
                      '&:hover .MuiOutlinedInput-notchedOutline': {
                        border: 'none', // Remove border on hover
                      },
                      '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                        border: 'none', // Remove border on focus
                      },
                    }}
                  >
                    <MenuItem value="BTC">BTC</MenuItem>
                    <MenuItem value="ETH">ETH</MenuItem>
                    <MenuItem value="XRP">XRP</MenuItem>
                    <MenuItem value="CSH">CSH</MenuItem>
                  </Select>
                  <Button
                    onClick={handleToggleOrderBook}
                    sx={{
                      color: 'white',
                      border: '1px solid white',
                      height: '30px',
                      backgroundColor: 'transparent',
                      '&:hover': {
                        backgroundColor: 'rgba(255, 255, 255, 0.1)',
                      },
                    }}
                  >
                    Order Book
                  </Button>
                </div>
                <Chart coinName={coinName} />
              </Paper>
            </Grid>
            {/* Current and Trade Options */}
            <Grid item xs={12} md={4} lg={3}>
              <Paper
                sx={{
                  p: 2,
                  display: 'flex',
                  flexDirection: 'column',
                  height: 'auto',
                }}
              >
                <Current coinName={coinName} />
                <TradeOptions coinName={coinName} />
              </Paper>
            </Grid>
            {showOrderBook && (
              <Grid item xs={12}>
                <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column' }}>
                  <OrderBook coinName={coinName} />
                </Paper>
              </Grid>
            )}
            {/* Recent Orders */}
            <Grid item xs={12}>
              <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column' }}>
                <Orders />
              </Paper>
            </Grid>
          </Grid>
          <Copyright sx={{ pt: 4 }} />
        </Container>
      </Box>
    </ThemeProvider>
  );
}
