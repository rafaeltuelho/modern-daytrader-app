import apiClient, { setToken, removeToken } from './client';
import type { LoginRequest, LoginResponse, RegisterRequest, Account } from '../types';

/**
 * Authentication API service
 */
export const authApi = {
  /**
   * Login with user credentials
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
    // Store token on successful login
    setToken(response.data.token);
    return response.data;
  },

  /**
   * Register a new user
   */
  async register(data: RegisterRequest): Promise<Account> {
    const response = await apiClient.post<Account>('/auth/register', data);
    return response.data;
  },

  /**
   * Logout current user
   */
  logout(): void {
    removeToken();
  },
};

export default authApi;

