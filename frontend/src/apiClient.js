import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api',
});

apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  config.headers.Authorization = token ? `Bearer ${token}` : '';
  return config;
});

export default apiClient;
