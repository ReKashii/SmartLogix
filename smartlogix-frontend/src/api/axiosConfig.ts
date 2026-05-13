import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

const DUMMY_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsIm5hbWUiOiIocmVuYXRvIiwiaWF0IjoyMTQ3NDgzNjAwfQ.SFlrZl9kdW1teV90b2tlbl9mb3Jfcy1tYXJ0bG9naXhfMjAyNl9tdXN0X2JlX2xvbmdfZW5vdWdo';

api.interceptors.request.use(
  (config) => {
    config.headers.Authorization = `Bearer ${DUMMY_TOKEN}`;
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
