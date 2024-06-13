import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import { Paper, Grid, Typography, TextField, Button, ButtonGroup, Slider, ThemeProvider, Select, MenuItem, FormControl, InputAdornment, Modal } from '@mui/material';
import { styled } from '@mui/material/styles';
import darkTheme from '../Theme';
import apiClient from '../../apiClient'; // axios 인스턴스 임포트
import { useMarketData } from '../context/MarketData';

const CustomPaper = styled(Paper)(({ theme }) => ({
  padding: '20px',
  color: theme.palette.text.primary,
  backgroundColor: theme.palette.background.paper,
  marginBottom: '20px',
  position: 'relative', // Added to position the account balance
  minHeight: '470px' // Fixed height to prevent resizing issues
}));

const LeverageModal = ({ open, onClose, leverage, setLeverage }) => {
  const marks = [
    { value: 1, label: '1x' },
    { value: 100, label: '100x' },
    { value: 200, label: '200x' },
    { value: 300, label: '300x' },
    { value: 400, label: '400x' },
    { value: 500, label: '500x' },
  ];

  const handleSliderChange = (event, newValue) => {
    setLeverage(newValue);
  };

  return (
    <Modal open={open} onClose={onClose}>
      <Box sx={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 400, bgcolor: 'background.paper', border: '2px solid #000', boxShadow: 24, p: 4 }}>
        <Typography variant="h6" component="h2">
          Adjust Leverage
        </Typography>
        <Typography sx={{ mt: 2 }}>Leverage</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 2 }}>
          <Button onClick={() => setLeverage(leverage > 1 ? leverage - 1 : 1)}>-</Button>
          <Typography variant="h4">{leverage}x</Typography>
          <Button onClick={() => setLeverage(leverage < 500 ? leverage + 1 : 500)}>+</Button>
        </Box>
        <Slider
          value={leverage}
          min={1}
          max={500}
          step={1}
          onChange={handleSliderChange}
          valueLabelDisplay="auto"
          marks={marks}
          sx={{ mt: 4 }}
        />
        <Typography sx={{ mt: 2 }}>Maximum leverage you can choose: 500x</Typography>
        <Typography sx={{ mt: 1 }}>Please note that higher the leverage, the faster it can be liquidated.</Typography>
        <Typography sx={{ mt: 1, color: 'red' }}>
          Selecting higher leverage such as [10x] increases your liquidation risk. Always manage your risk levels.
        </Typography>
        <Button 
          variant="contained" 
          onClick={onClose} 
          sx={{ 
            mt: 2, 
            width: '100%', // Make the button take the full width
            display: 'block', // Center the button horizontally
            mx: 'auto' // Center the button horizontally
          }}
        >
  Confirm
</Button>
      </Box>
    </Modal>
  );
};

const TradeOptions = ({ coinName }) => {
  const [orderType, setOrderType] = useState('limit');
  const [sizeType, setSizeType] = useState('USDT');
  const [size, setSize] = useState('0');
  const [price, setPrice] = useState('7313.13'); // Example price
  const [accountBalance, setAccountBalance] = useState(0); // Initial state for account balance
  const [leverage, setLeverage] = useState(1); // Example leverage
  const { marketData} = useMarketData();
  const [isLeverageModalOpen, setIsLeverageModalOpen] = useState(false);

  const fetchAccountBalance = async (setAccountBalance) => {
    try {
      const response = await apiClient.get('/member/wallet');
      const data = response.data;
      setAccountBalance(data.USDT || 0); // Set USDT balance from response data
    } catch (error) {
      console.error('Error fetching account balance:', error);
    }
  };
  
  // 2. useEffect 내에서 fetchAccountBalance 호출
  useEffect(() => {
    fetchAccountBalance(setAccountBalance);
  }, []);

  const handleSizeChange = (event, newValue) => {
    if (typeof newValue === 'number') {
      const roundedValue = newValue.toFixed(2);
      setSize(roundedValue);
    } else {
      const inputValue = event.target.value;
      if (/^\d*\.?\d*$/.test(inputValue)) {
        setSize(inputValue);
      }
    }
  };

  const handleSizeTypeChange = (event) => {
    if (orderType === 'market') {
      setSizeType('USDT');
    } else {
      setSizeType(event.target.value);
    }
    setSize('0');;
  };

  const handlePositionChange = async (position) => {
    const tradeType = sizeType === 'USDT' ? 'price' : 'quantity';
    const finalPrice = orderType === 'market' ? marketData[coinName] : parseFloat(price); // Use market data price if order type is market
    let adjustedSize = parseFloat(size);
    const accountBalanceLeverage = accountBalance * leverage;
  
    if (sizeType === 'USDT') {
      const total = adjustedSize;
      if (total > accountBalanceLeverage) {
        adjustedSize = accountBalance;
      } else {
        adjustedSize = (total / leverage).toFixed(2);
      }
    } else {
      const total = adjustedSize * finalPrice;
      if (total > accountBalanceLeverage) {
        adjustedSize = (accountBalanceLeverage / finalPrice).toFixed(2);
      }
    }
  
    const orderData = {
      orderType: orderType,
      coinName,
      price: finalPrice.toFixed(2), // Use the final price
      size: adjustedSize,
      leverage,
      trade: tradeType,
      position : position.toUpperCase(),
    };
    console.log('Order:', orderData);
    try {
      const response = await apiClient.post('/order', orderData);
      console.log('Order response:', response.data);
      // Handle the response if needed
    } catch (error) {
      console.error('Error placing order:', error);
    }
    fetchAccountBalance(setAccountBalance);
  };

  const handlePriceChange = (event) => {
    const inputValue = event.target.value;
    if (/^\d*\.?\d*$/.test(inputValue)) {
      setPrice(inputValue);
    }
  };

  const calculateAmount = (percentage) => {
    return ((accountBalance * leverage) * (percentage / 100)).toFixed(2);
  };

  const calculateTotal = () => {
      let sizeValue = parseFloat(size);
      let priceValue = parseFloat(price);
      if(orderType === 'market') {
          priceValue = marketData[coinName];
          if(sizeType !== 'USDT') setSizeType('USDT');
      }

      if (isNaN(sizeValue)) {
        sizeValue = 0;
    }

    if (sizeType !== 'USDT') {
      const total = sizeValue * priceValue;
      return total > (accountBalance * leverage) ? (accountBalance * leverage).toFixed(2) : total.toFixed(2);
    } else {
      return sizeValue > (accountBalance * leverage) ? (accountBalance * leverage).toFixed(2) : sizeValue.toFixed(2);
    }
  };

  const calculateCost = () => {
    const total = calculateTotal();
    return (total / leverage).toFixed(2);
  };

  const marks = [
    { value: 0, label: '0%' },
    { value: 25, label: '25%' },
    { value: 50, label: '50%' },
    { value: 75, label: '75%' },
    { value: 100, label: '100%' },
  ];

  const sizePercentage = (parseFloat(size) / (accountBalance * leverage)) * 100;

  return (
    <ThemeProvider theme={darkTheme}>
      <CustomPaper>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Button
           onClick={() => setIsLeverageModalOpen(true)}
           sx={{ border: '1px solid white', color: 'white' }}>
            {leverage}x
          </Button>
          <Typography variant="caption" sx={{ color: 'grey.500' }}>
            {`My USDT: ${accountBalance.toFixed(2)}`}
          </Typography>
        </Box>
        <Typography variant="h6" sx={{ color: 'grey.500', mt: 2 }}>Trade</Typography>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <ButtonGroup fullWidth aria-label="outlined button group">
              <Button onClick={() => setOrderType('limit')} variant={orderType === 'limit' ? "contained" : "outlined"}>Limit</Button>
              <Button onClick={() => setOrderType('market')} variant={orderType === 'market' ? "contained" : "outlined"}>Market</Button>
            </ButtonGroup>
          </Grid>
          {orderType === 'limit' && (
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Price"
                variant="outlined"
                value={price === '0' ? '' : price}
                onChange={handlePriceChange}
                InputLabelProps={{
                  shrink: true,
                }}
              />
            </Grid>
          )}
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Size"
              variant="outlined"
              value={size === '0' ? '' : size}
              InputLabelProps={{
                shrink: true,
              }}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <FormControl variant="standard">
                      {orderType === 'market' ? (
                        <Typography variant="subtitle1">USDT</Typography>
                      ) : (
                        <Select
                          value={sizeType}
                          onChange={handleSizeTypeChange}
                          disableUnderline
                        >
                          <MenuItem value="USDT">USDT</MenuItem>
                          <MenuItem value={coinName}>{coinName}</MenuItem>
                        </Select>
                      )}
                    </FormControl>
                  </InputAdornment>
                ),
              }}
              onChange={handleSizeChange}
            />
          </Grid>
          {sizeType === 'USDT' && (
            <Grid item xs={12}>
              <Slider
                value={isNaN(sizePercentage) ? 0 : sizePercentage}
                min={0}
                max={100}
                step={1}
                onChange={(event, newValue) => handleSizeChange(null, (newValue / 100) * (accountBalance * leverage))}
                valueLabelDisplay="auto"
                valueLabelFormat={(value) => `${value.toFixed(2)}%`}
                marks={marks}
                aria-labelledby="input-slider"
              />
            </Grid>
          )}
          <Grid item xs={6}>
            <Button
              variant="contained"
              onClick={() => handlePositionChange('long')}
              sx={{ backgroundColor: '#4caf50', '&:hover': { backgroundColor: '#388e3c' } }}
              fullWidth>
              Long
            </Button>
          </Grid>
          <Grid item xs={6}>
            <Button
              variant="contained"
              onClick={() => handlePositionChange('short')}
              sx={{ backgroundColor: '#f44336', '&:hover': { backgroundColor: '#d32f2f' } }}
              fullWidth>
              Short
            </Button>
          </Grid>
          <Grid item xs={12}>
            <Typography>{`Cost ${calculateCost()} USDT`}</Typography>
          </Grid>
          <Grid item xs={12}/>
        </Grid>
      </CustomPaper>
      <LeverageModal
        open={isLeverageModalOpen}
        onClose={() => setIsLeverageModalOpen(false)}
        leverage={leverage}
        setLeverage={setLeverage}
      />
    </ThemeProvider>
  );
};

export default TradeOptions;
