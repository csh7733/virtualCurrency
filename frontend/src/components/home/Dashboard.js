import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { styled, createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import MuiDrawer from '@mui/material/Drawer';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import MuiAppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import Badge from '@mui/material/Badge';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Link from '@mui/material/Link';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { mainListItems, secondaryListItems } from './listItems';
import Chart from './chart/Chart';
import Current from './Current';
import Orders from './Orders';
import darkTheme from '../Theme';
import Logo from '../../assets/logo.png'; 
import Title from '../../assets/title.png'; 
import Copyright from '../Copyright';
import TradeOptions from './TradeOptions';


const drawerWidth = 240;

const AppBar = styled(MuiAppBar, {
  shouldForwardProp: (prop) => prop !== 'open',
})(({ theme, open }) => ({
  zIndex: theme.zIndex.drawer + 1,
  transition: theme.transitions.create(['width', 'margin'], {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  ...(open && {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  }),
}));

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
  ({ theme, open }) => ({
    '& .MuiDrawer-paper': {
      position: 'relative',
      whiteSpace: 'nowrap',
      width: drawerWidth,
      transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen,
      }),
      boxSizing: 'border-box',
      ...(!open && {
        overflowX: 'hidden',
        transition: theme.transitions.create('width', {
          easing: theme.transitions.easing.sharp,
          duration: theme.transitions.duration.leavingScreen,
        }),
        width: theme.spacing(7),
        [theme.breakpoints.up('sm')]: {
          width: theme.spacing(9),
        },
      }),
    },
  }),
);

// TODO remove, this demo shouldn't need to reset the theme.
const defaultTheme = darkTheme;

export default function Dashboard() {
  const [open, setOpen] = React.useState(true);
  const params = useParams();
  const coinName = params.coinName || 'BTC';
  const [selectedCoin, setSelectedCoin] = useState(coinName);
  const toggleDrawer = () => {
    setOpen(!open);
  };

  const handleChange = (event) => {
    window.location.href = `/${event.target.value}`; 
  };

  const flag = true;
  return (
    <ThemeProvider theme={defaultTheme}>
      <Box sx={{ display: 'flex' }}>
        <CssBaseline />
        <AppBar position="absolute" open={open}>
          <Toolbar
            sx={{
              pr: '24px', // keep right padding when drawer closed
            }}
          >
            <IconButton
              edge="start"
              color="inherit"
              aria-label="open drawer"
              onClick={toggleDrawer}
              sx={{
                marginRight: '36px',
                ...(open && { display: 'none' }),
              }}
            >
             <MenuIcon />
            </IconButton>
            <IconButton edge="start" color="inherit" aria-label="menu" sx={{ mr: 2 }}>
              <Link to="/">
                <img src={Logo} alt="Logo" style={{ height: '50px', width: '50px' }} /> 
                <img src={Title} alt="최성현 암호화폐 거래소" style={{ height: '40px', marginLeft: '10px'}} />
              </Link>
            </IconButton>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }} />
            <Button href="login" color="inherit" sx={{ mr: 2 }}>Log In</Button>
            <Button href="register" color="inherit" sx={{ mr: 2 }}>Register</Button>
            {flag && (
                <IconButton color="inherit">
                    <Badge badgeContent={4} color="secondary">
                    <NotificationsIcon />
                    </Badge>
                </IconButton>
            )}
          </Toolbar>
        </AppBar>
        <Drawer variant="permanent" open={open}>
          <Toolbar
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'flex-end',
              px: [1],
            }}
          >
            <IconButton onClick={toggleDrawer}>
              <ChevronLeftIcon />
            </IconButton>
          </Toolbar>
          <Divider />
          <List component="nav">
            {mainListItems}
            <Divider sx={{ my: 1 }} />
            {secondaryListItems}
          </List>
        </Drawer>
        <Box
          component="main"
          sx={{
            backgroundColor: (theme) =>
              theme.palette.mode === 'light'
                ? theme.palette.grey[100]
                : theme.palette.grey[900],
            flexGrow: 1,
            height: '100vh',
            overflow: 'auto',
          }}
        >
          <Toolbar />
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
                  }}
                >
                  <div>
                    <Select
                      value={selectedCoin} 
                      onChange={handleChange}
                      displayEmpty
                      inputProps={{ 'aria-label': 'Without label' }}
                      sx={{
                        '& .MuiOutlinedInput-notchedOutline': {
                          border: 'none'  // 테두리 없애기
                        },
                        '&:hover .MuiOutlinedInput-notchedOutline': {
                          border: 'none'  // 호버 상태에서도 테두리 없애기
                        },
                        '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                          border: 'none'  // 포커스 상태에서도 테두리 없애기
                        }
                      }}
                    >
                      <MenuItem value="BTC">BTC</MenuItem>
                      <MenuItem value="ETH">ETH</MenuItem>
                      <MenuItem value="XRP">XRP</MenuItem>
                      <MenuItem value="CSH">CSH</MenuItem>
                    </Select>
                    <Chart coinName={coinName} /> 
                  </div>
                </Paper>
              </Grid>
              {/* Recent Current */}
              <Grid item xs={12} md={4} lg={3}>
                <Paper
                  sx={{
                    p: 2,
                    display: 'flex',
                    flexDirection: 'column',
                    height: 'auto',
                  }}
                >
                  <Current coinName={coinName}/>
                  <TradeOptions />
                </Paper>
              </Grid>
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
      </Box>
    </ThemeProvider>
  );
}