import React, { useState } from 'react';
import Avatar from '@mui/material/Avatar';
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
import Copyright from '../Copyright';
import darkTheme from '../Theme';
import logo from '../../assets/logo.png';
import Modal from '@mui/material/Modal';

const defaultTheme = darkTheme;

export default function Register() {
  const navigate = useNavigate(); 
  const [error, setError] = useState(''); 
  const [open, setOpen] = useState(false); // Modal 상태 관리

  const handleSubmit = async e => {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const { userName, email, password } = Object.fromEntries(data.entries());
    try {
        const response = await axios.post('/api/register', {
            userName,
            email,
            password
        });
        if(response.status === 200) {
            setOpen(true); 
        }
    } catch (error) {
        if (error.response && error.response.status === 409) {
          setError('Email already exists');
        } else {
          setError('Failed to create account');
        }
    }
  };

  const handleClose = () => {
    setOpen(false);
    navigate('/login');
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
            REGISTER
          </Typography>
          <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
            <Grid container spacing={2}>
                <Grid item xs={12}>
                <TextField
                    autoComplete="name"
                    name="userName"
                    required
                    fullWidth
                    id="userName"
                    label="User Name" 
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
                </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="email"
                  label="Email Address"
                  name="email"
                  autoComplete="email"
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
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  name="password"
                  label="Password"
                  type="password"
                  id="password"
                  autoComplete="new-password"
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
              </Grid>
            </Grid>
            {error && (
              <Typography variant="body2" color="error" align="center" sx={{ mt: 2 }}>
                {error}
              </Typography>
            )}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
            >
              Register
            </Button>
            <Grid container justifyContent="flex-end">
              <Grid item>
                <Link href="login" color="text.secondary" variant="body2">
                  Already have an account? Log in
                </Link>
              </Grid>
            </Grid>
          </Box>
        </Box>
        <Copyright sx={{ mt: 5 }} />
        <Modal
          open={open}
          onClose={handleClose}
          aria-labelledby="modal-title"
          aria-describedby="modal-description"
        >
          <Box sx={{
            position: 'absolute',
            top: '50%',
            left: '57%',
            transform: 'translate(-50%, -50%)',
            width: 400,
            bgcolor: 'background.paper',
            border: '2px solid #000',
            boxShadow: 24,
            p: 4,
          }}>
            <Typography id="modal-title" variant="h6" component="h2">
              Account Created Successfully
            </Typography>
            <Typography id="modal-description" sx={{ mt: 2 }}>
              Your account has been created successfully. Click the button below to log in.
            </Typography>
            <Button onClick={handleClose} fullWidth variant="contained" sx={{ mt: 2 }}>
              Go to Login
            </Button>
          </Box>
        </Modal>
      </Container>
    </ThemeProvider>
  );
}
