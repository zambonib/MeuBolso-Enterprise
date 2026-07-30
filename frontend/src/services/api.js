import axios from 'axios';

// Base API Configuration
const API_BASE_URL = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_BASE_URL)
  ? import.meta.env.VITE_API_BASE_URL
  : 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request Interceptor: Attach JWT Bearer Token if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle Global 401 Logout and 400 Validation Error formatting
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      return Promise.reject({
        message: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
        isNetworkError: true,
      });
    }

    const { status, data } = error.response;

    if (status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      }

      return Promise.reject({
        status: 401,
        message: 'Sessão expirada ou não autorizada. Faça login novamente.',
      });
    }

    if (status === 400) {
      const fieldErrors = data?.errors || {};
      const errorMessage = data?.message || 'Dados inválidos. Verifique os campos informados.';

      return Promise.reject({
        status: 400,
        message: errorMessage,
        fieldErrors,
        isValidationError: Object.keys(fieldErrors).length > 0,
      });
    }

    if (status === 404) {
      return Promise.reject({
        status: 404,
        message: data?.message || 'Recurso não encontrado.',
      });
    }

    return Promise.reject({
      status,
      message: data?.message || 'Ocorreu um erro inesperado no servidor.',
    });
  }
);

export default api;
