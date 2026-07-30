import axios from "axios";

const API_BASE_URL =
  "https://medical-camp-project-production.up.railway.app/api/v1";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    // Do not attach token to public endpoints like login
    if (config.url && config.url.includes('/auth/login')) {
      delete config.headers.Authorization;
      return config;
    }

    const token = localStorage.getItem('mediq_token');
    if (token && typeof token === 'string' && token.split('.').length === 3) {
      config.headers.Authorization = `Bearer ${token}`;
    } else if (token) {
      // Clear corrupt token
      localStorage.removeItem('mediq_token');
      localStorage.removeItem('mediq_user');
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    let errorMessage = 'An error occurred during authentication';
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('mediq_token');
        localStorage.removeItem('mediq_user');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }
      errorMessage = error.response.data?.message || error.response.data?.error || `Server Error (${error.response.status})`;
    } else if (error.message) {
      errorMessage = error.message;
    }
    return Promise.reject({ message: errorMessage });
  }
);

export default api;
