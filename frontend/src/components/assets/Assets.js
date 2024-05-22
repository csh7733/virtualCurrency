import React from 'react';
import { Container, Typography, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, Tab } from '@mui/material';
import { makeStyles } from '@mui/styles';
import { styled } from '@mui/material/styles';
import btcIcon from '../../assets/btc.png';
import ethIcon from '../../assets/eth.png';
import xrpIcon from '../../assets/xrp.png';
import cshIcon from '../../assets/csh.png';
import usdtIcon from '../../assets/usdt.png';
import Positions from '../home/Positions';

const coinIcons = {
    BTC: btcIcon,
    ETH: ethIcon,
    XRP: xrpIcon,
    CSH: cshIcon,
    USDT: usdtIcon
  };
  

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
  table: {
    minWidth: 650,
  },
  tab: {
    '& .Mui-selected': {
      color: theme.palette.primary.main,
    },
  },
  tabContent: {
    padding: theme.spacing(2),
  },
}));

const CustomTableContainer = styled(TableContainer)(({ theme }) => ({
  backgroundColor: theme.palette.background.default,
}));

const assetsData = [
  { name: 'BTC', balance: '*****', pnl: '*****',position: '*****' },
  { name: 'ETH', balance: '*****', pnl: '*****',position: '*****' },
  { name: 'XRP', balance: '*****', pnl: '*****',position: '*****' },
  { name: 'CSH', balance: '*****', pnl: '*****',position: '*****' },
  { name: 'USDT', balance: '*****', pnl: '*****',position: '*****' },
];

const AssetRow = ({ asset }) => (
  <TableRow>
    <TableCell>
      <Grid container alignItems="center">
        <Grid item>
          <img src={coinIcons[asset.name]} alt={asset.name} style={{ height: '24px', width: '24px', marginRight: '8px' }} />
        </Grid>
        <Grid item>
          <Typography variant="body2">{asset.name}</Typography>
        </Grid>
      </Grid>
    </TableCell>
    <TableCell>{asset.balance}</TableCell>
    <TableCell>{asset.pnl}</TableCell>
    <TableCell>{asset.position}</TableCell>
  </TableRow>
);

const Assets = () => {
  const classes = useStyles();
  const [value, setValue] = React.useState(0);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
    <Container>
      <Paper className={classes.root}>
        <Tabs value={value} onChange={handleChange} indicatorColor="primary" textColor="inherit" centered className={classes.tab}>
          <Tab label="Assets" />
          <Tab label="Positions" />
        </Tabs>
        <div className={classes.tabContent}>
          {value === 0 && (
            <CustomTableContainer component={Paper}>
              <Table className={classes.table} aria-label="assets table">
                <TableHead>
                  <TableRow>
                    <TableCell>Asset</TableCell>
                    <TableCell>Wallet Balance</TableCell>
                    <TableCell>Unrealized PNL</TableCell>
                    <TableCell>Position</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {assetsData.map((asset, index) => (
                    <AssetRow key={index} asset={asset} />
                  ))}
                </TableBody>
              </Table>
            </CustomTableContainer>
          )}
          {value === 1 && (
            <Positions />
          )}
        </div>
      </Paper>
    </Container>
  );
};

export default Assets;
