import * as React from 'react';
import { Typography, Grid } from '@mui/material';
import Title from './Title';
import { useMarketData } from '../context/MarketData';

function preventDefault(event) {
  event.preventDefault();
}

{/* <div>
<Link color="primary" href="#" onClick={preventDefault}>
  View balance
</Link>
</div> */}

const Current = ({ coinName }) =>{ 
  const { marketData } = useMarketData();
  const price = marketData[coinName];
  const fixedPrice = price ? price.toFixed(2) : 'Loading...';
    return (
      <React.Fragment>
        <Title>{coinName}</Title>
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
}

export default Current;