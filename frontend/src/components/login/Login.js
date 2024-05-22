import React, { useState } from 'react';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; 
import {jwtDecode} from 'jwt-decode';
import Copyright from '../Copyright';
import darkTheme from '../Theme';
import logo from '../../assets/logo.png';
import apiClient from '../../apiClient';

const defaultTheme = darkTheme;

export default function Login() {
  const navigate = useNavigate(); 
  const [error, setError] = useState(''); 
  const handleSubmit = async e => {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const { email, password } = Object.fromEntries(data.entries());
    try {
        const response = await axios.post('/api/login', {
            email,
            password
      });
      const jwt = response.data;
      localStorage.setItem('token', jwt);
      navigate('/');
    } catch (error) {
      if (error.response) {
        if (error.response.status === 401) {
          setError('Invalid email or password');
        } else {
          setError('An unexpected error occurred');
        }
      } else {
        setError('Network error');
      }
    }
  };

  return (
    <ThemeProvider theme={defaultTheme}>
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Link href="/">
            <img src={logo} alt="Logo" style={{ width: 100, height: 100 }} />
          </Link>
          <Typography component="h1" variant="h5">
            LOG IN
          </Typography>
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email Address"
              name="email"
              autoComplete="email"
              autoFocus
              sx={{ 
                input: { color: 'white', backgroundColor: 'rgba(255, 255, 255, 0.1)' },
                '& label': { color: 'gray' },
                '& label.Mui-focused': { color: 'white' },
                '& .MuiOutlinedInput-root': {
                  '& fieldset': { borderColor: 'gray' },
                  '&:hover fieldset': { borderColor: 'white' },
                  '&.Mui-focused fieldset': { borderColor: 'white' },
                },
              }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              sx={{ 
                input: { color: 'white', backgroundColor: 'rgba(255, 255, 255, 0.1)' },
                '& label': { color: 'gray' },
                '& label.Mui-focused': { color: 'white' },
                '& .MuiOutlinedInput-root': {
                  '& fieldset': { borderColor: 'gray' },
                  '&:hover fieldset': { borderColor: 'white' },
                  '&.Mui-focused fieldset': { borderColor: 'white' },
                },
              }}
            />
            {error && (
              <Typography variant="body2" color="error" align="center" sx={{ mt: 2 }}>
                {error}
              </Typography>
            )}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2, bgcolor: 'secondary.main', '&:hover': { bgcolor: 'secondary.dark' } }}
            >
              Log In
            </Button>
            <Grid container>
              <Grid item>
                <Link href="register" color="text.secondary" variant="body2">
                  {"Don't have an account? Register"}
                </Link>
              </Grid>
            </Grid>
          </Box>
        </Box>
        <Copyright sx={{ mt: 8, mb: 4 }} />
      </Container>
    </ThemeProvider>
  );
}
