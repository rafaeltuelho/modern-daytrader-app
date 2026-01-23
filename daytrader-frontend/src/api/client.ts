import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '../types';

// Create axios instance with base URL
// In development, Vite proxy will handle /api routes
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token storage key
const TOKEN_KEY = 'daytrader_token';

// Get token from localStorage
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

// Set token in localStorage
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

// Remove token from localStorage
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

// Request interceptor - add JWT token to requests
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    // Handle specific error cases
    if (error.response) {
      const status = error.response.status;
      
      // Unauthorized - clear token and redirect to login
      if (status === 401) {
        removeToken();
        // Redirect to login if not already there
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }
      
      // Create a standardized error response
      const apiError: ApiError = {
        error: error.response.data?.error || 'An error occurred',
        message: error.response.data?.message,
        status,
      };
      
      return Promise.reject(apiError);
    }
    
    // Network error or timeout
    if (error.request) {
      const apiError: ApiError = {
        error: 'Network error',
        message: 'Unable to connect to the server. Please check your connection.',
      };
      return Promise.reject(apiError);
    }
    
    // Other errors
    return Promise.reject({
      error: 'Unknown error',
      message: error.message,
    } as ApiError);
  }
);

export default apiClient;

