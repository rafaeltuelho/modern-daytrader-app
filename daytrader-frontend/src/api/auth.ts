import { apiClient } from './client';
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '../types';

// Backend response format from LoginResponseDTO
interface BackendLoginResponse {
  user: User;
  token: string;
  expiresIn: number;
  tokenType: string;
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    // Backend returns LoginResponseDTO with user, token, expiresIn, tokenType
    const response = await apiClient.post<BackendLoginResponse>('/auth/login', credentials);

    // Store the JWT token for subsequent API calls
    apiClient.setToken(response.token);

    // Store the userID in sessionStorage for session management
    sessionStorage.setItem('auth_user_id', response.user.profileID || credentials.userID);

    // Store token expiration time
    const expiresAt = Date.now() + (response.expiresIn * 1000);
    sessionStorage.setItem('auth_token_expires_at', expiresAt.toString());

    return response;
  },

  logout: async (): Promise<void> => {
    const userID = sessionStorage.getItem('auth_user_id');
    if (userID) {
      try {
        await apiClient.post<void>('/auth/logout', { userID });
      } catch (error) {
        // Ignore logout errors - clear local state anyway
        console.warn('Logout request failed:', error);
      }
    }
    sessionStorage.removeItem('auth_user_id');
    sessionStorage.removeItem('auth_token_expires_at');
    apiClient.setToken(null);
  },

  register: async (data: RegisterRequest): Promise<User> => {
    return apiClient.post<User>('/auth/register', data);
  },

  getCurrentUser: async (): Promise<User> => {
    return apiClient.get<User>('/auth/me');
  },

  isTokenExpired: (): boolean => {
    const expiresAt = sessionStorage.getItem('auth_token_expires_at');
    if (!expiresAt) return true;
    return Date.now() > parseInt(expiresAt, 10);
  },

  hasValidToken: (): boolean => {
    const token = apiClient.getToken();
    return !!token && !authApi.isTokenExpired();
  },
};

