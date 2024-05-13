import React from 'react';
import { Typography, Link } from '@mui/material';

function Copyright() {
  return (
    <Typography variant="body2" color="text.secondary" align="center">
      {'CSH © '}
      <Link color="inherit" href="https://github.com/csh7733/virtualCurrency">
        VirtualCurrency
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

export default Copyright;
