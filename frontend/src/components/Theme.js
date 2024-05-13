import { createTheme } from '@mui/material/styles';

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#ffa726', // 주황색 유지
    },
    secondary: {
      main: '#f57c00', // 강조색을 조금 더 진하게 변경
    },
    background: {
      default: '#121212', // 기본 배경색
      paper: '#121212', // 요소 배경색
    },
    text: {
      primary: '#ffffff', // 주요 텍스트 색상
      secondary: '#e0e0e0', // 부차적인 텍스트 색상
    }
  }
});

export default darkTheme;
