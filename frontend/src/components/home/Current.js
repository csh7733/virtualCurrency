import * as React from 'react';
import { Typography, Grid } from '@mui/material';
import Title from './Title';
import { useMarketData } from '../context/MarketData';
import btcIcon from '../../assets/btc.png';
import ethIcon from '../../assets/eth.png';
import xrpIcon from '../../assets/xrp.png';
import cshIcon from '../../assets/csh.png';

const coinIcons = {
  BTC: btcIcon,
  ETH: ethIcon,
  XRP: xrpIcon,
  CSH: cshIcon,
};

const Current = ({ coinName }) => {
  const { marketData } = useMarketData();
  const price = marketData[coinName];
  const fixedPrice = price ? price.toFixed(2) : 'Loading...';

  return (
    <React.Fragment>
      <Title>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <img src={coinIcons[coinName]} alt={coinName} style={{ height: '30px', marginRight: '10px' }} />
          {coinName}
        </div>
      </Title>
      <Grid container spacing={1}>
        <Grid item xs={12}>
          <Typography component="p" variant="h4">
            ${fixedPrice}
          </Typography>
        </Grid>
        <Grid item xs={12}>
          <Typography color="text.secondary" sx={{ flex: 1 }}>
            price(USDT)
          </Typography>
        </Grid>
      </Grid>
    </React.Fragment>
  );
};

export default Current;
