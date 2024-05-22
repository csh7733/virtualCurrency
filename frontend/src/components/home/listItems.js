import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ListSubheader from '@mui/material/ListSubheader';
import HomeIcon from '@mui/icons-material/Home';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PeopleIcon from '@mui/icons-material/People';

export const MainListItems = () => {
  const navigate = useNavigate();

  return (
    <React.Fragment>
      <ListSubheader component="div" inset>
        Futures
      </ListSubheader>
      <ListItemButton onClick={() => navigate('/')}>
        <ListItemIcon>
          <HomeIcon />
        </ListItemIcon>
        <ListItemText primary="Home" />
      </ListItemButton>
      <ListItemButton onClick={() => navigate('/assets')}>
        <ListItemIcon>
          <AccountBalanceWalletIcon />
        </ListItemIcon>
        <ListItemText primary="Assets" />
      </ListItemButton>
      <ListItemButton onClick={() => navigate('/orders')}>
        <ListItemIcon>
          <ShoppingCartIcon />
        </ListItemIcon>
        <ListItemText primary="Orders" />
      </ListItemButton>
    </React.Fragment>
  );
};

export const SecondaryListItems = () => {
  const navigate = useNavigate();

  return (
    <React.Fragment>
      <ListSubheader component="div" inset>
        Customer
      </ListSubheader>
      <ListItemButton onClick={() => navigate('/account')}>
        <ListItemIcon>
          <PeopleIcon />
        </ListItemIcon>
        <ListItemText primary="Account" />
      </ListItemButton>
    </React.Fragment>
  );
};
