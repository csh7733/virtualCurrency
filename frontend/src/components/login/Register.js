import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; 
import Copyright from '../Copyright';
import darkTheme from '../Theme';
import logo from '../../assets/logo.png';

const defaultTheme = darkTheme;

export default function Register() {
const navigate = useNavigate(); 
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
        const jwt = response.data;
        if(jwt === "ok") {
            console.log("success!");
            navigate('/login'); // 회원가입 성공 후 로그인 페이지로 리디렉션
          }
    } catch (error) {
        console.error('계정생성 실패:', error);
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
                      input: { color: 'white', backgroundColor: 'rgba(255, 255, 255, 0.1)' }, // 배경색 변경
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
                    input: { color: 'white', backgroundColor: 'rgba(255, 255, 255, 0.1)' }, // 배경색 변경
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
                    input: { color: 'white', backgroundColor: 'rgba(255, 255, 255, 0.1)' }, // 배경색 변경
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
      </Container>
    </ThemeProvider>
  );
}