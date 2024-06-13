import React, { useEffect, useState } from 'react';
import { Container, Typography, Grid, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, Tab } from '@mui/material';
import { makeStyles } from '@mui/styles';
import { styled } from '@mui/material/styles';
import btcIcon from '../../assets/btc.png';
import ethIcon from '../../assets/eth.png';
import xrpIcon from '../../assets/xrp.png';
import cshIcon from '../../assets/csh.png';
import usdtIcon from '../../assets/usdt.png';
import Positions from '../home/Positions';
import apiClient from '../../apiClient';

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
  </TableRow>
);

const Assets = () => {
  const classes = useStyles();
  const [value, setValue] = useState(0);
  const [assetsData, setAssetsData] = useState([]);
  const coinOrder = ['BTC', 'ETH', 'XRP', 'CSH', 'USDT'];

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await apiClient.get('/member/wallet');
        const data = response.data;

        // JSON 객체를 배열로 변환
        const assetsArray = Object.keys(data).map(key => ({
          name: key,
          balance: data[key] + " " + key
        }))
        .sort((a, b) => coinOrder.indexOf(a.name) - coinOrder.indexOf(b.name));

        setAssetsData(assetsArray);
      } catch (error) {
        console.error('Error fetching assets data:', error);
      }
    };

    fetchData();
  }, []);

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
