import React, { useState } from 'react';
import Box from '@mui/material/Box';
import { Paper, Grid, Typography, TextField, Button, ButtonGroup, Slider, ThemeProvider } from '@mui/material';
import { styled } from '@mui/material/styles';
import darkTheme from '../Theme';

const CustomPaper = styled(Paper)(({ theme }) => ({
  padding: '20px',
  color: theme.palette.text.primary,
  backgroundColor: theme.palette.background.paper,
  marginBottom: '20px'
}));

const TradeOptions = () => {
  const [orderType, setOrderType] = useState('limit');
  const [leverage, setLeverage] = useState(1);
  const [position, setPosition] = useState('');

  const handleSliderChange = (event, newValue) => {
    setLeverage(newValue);
  };

  const handlePositionChange = (position) => {
    setPosition(position);
  };

  return (
    <ThemeProvider theme={darkTheme}>
      <CustomPaper>
        <Typography variant="h6" sx={{ color: 'grey.500' }}>Trade</Typography>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <ButtonGroup fullWidth aria-label="outlined button group">
              <Button onClick={() => setOrderType('limit')} variant={orderType === 'limit' ? "contained" : "outlined"}>Limit</Button>
              <Button onClick={() => setOrderType('market')} variant={orderType === 'market' ? "contained" : "outlined"}>Market</Button>
            </ButtonGroup>
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Price"
              variant="outlined"
              defaultValue="7313.13"
              InputLabelProps={{
                shrink: true,
              }}
            />
          </Grid>
          <Grid item xs={6}>
            <TextField
              fullWidth
              label="Size"
              variant="outlined"
              defaultValue="22.05"
              InputLabelProps={{
                shrink: true,
              }}
            />
          </Grid>
          <Grid item xs={12}>
            <Typography id="input-slider" gutterBottom>
              Leverage
            </Typography>
            <Slider
              value={leverage}
              min={1}
              max={100}
              step={1}
              onChange={handleSliderChange}
              valueLabelDisplay="auto"
              aria-labelledby="input-slider"
            />
            <Typography>Selected Leverage: {leverage}x</Typography>
          </Grid>
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
            <Typography>Max : 0.00BTC</Typography>
            <Typography>COST : 0.00USDT</Typography>
          </Grid>
          <Grid item xs={12}/>
        </Grid>
      </CustomPaper>
    </ThemeProvider>
  );
};

export default TradeOptions;
