import React, { useState, useEffect } from 'react';
import { Container, Typography, Grid, Paper } from '@mui/material';
import { styled } from '@mui/material/styles';
import apiClient from '../../apiClient'; // axios 인스턴스 임포트

const CustomPaper = styled(Paper)(({ theme }) => ({
  padding: '20px',
  color: theme.palette.text.primary,
  backgroundColor: theme.palette.background.paper,
  marginBottom: '20px',
}));

const Account = () => {
  const [user, setUser] = useState({ username: '', email: '' });

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await apiClient.get('/member/info');
        setUser(response.data);
      } catch (error) {
        console.error('Error fetching user data:', error);
      }
    };

    fetchUserData();
  }, []);

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
