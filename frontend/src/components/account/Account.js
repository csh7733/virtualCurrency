import React from 'react';
import { Container, Typography, Grid, Paper } from '@mui/material';
import { styled } from '@mui/material/styles';

const CustomPaper = styled(Paper)(({ theme }) => ({
  padding: '20px',
  color: theme.palette.text.primary,
  backgroundColor: theme.palette.background.paper,
  marginBottom: '20px',
}));

const user = {
  username: 'JohnDoe',
  email: 'john.doe@example.com',
  // 사용자 정보를 더 추가할 수 있습니다.
};

const Account = () => {
  return (
    <Container>
      <CustomPaper>
        <Typography variant="h4" gutterBottom>
          Account Information
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Typography variant="h6">Username</Typography>
            <Typography variant="body1">{user.username}</Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="h6">Email</Typography>
            <Typography variant="body1">{user.email}</Typography>
          </Grid>
        </Grid>
      </CustomPaper>
    </Container>
  );
};

export default Account;
