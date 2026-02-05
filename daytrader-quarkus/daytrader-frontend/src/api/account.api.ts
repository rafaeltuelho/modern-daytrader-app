import apiClient from './client';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  AccountResponse,
  ProfileResponse,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from '../types/account.types';

export const accountApi = {
  // Authentication
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },

  refreshToken: async (refreshToken: string): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/auth/refresh', { refreshToken });
    return response.data;
  },

  // Registration
  register: async (data: RegisterRequest): Promise<AccountResponse> => {
    const response = await apiClient.post<AccountResponse>('/accounts', data);
    return response.data;
  },

  // Account management
  getCurrentAccount: async (): Promise<AccountResponse> => {
    const response = await apiClient.get<AccountResponse>('/accounts/me');
    return response.data;
  },

  getAccount: async (accountId: number): Promise<AccountResponse> => {
    const response = await apiClient.get<AccountResponse>(`/accounts/${accountId}`);
    return response.data;
  },

  // Profile management
  getCurrentProfile: async (): Promise<ProfileResponse> => {
    const response = await apiClient.get<ProfileResponse>('/profiles/me');
    return response.data;
  },

  updateCurrentProfile: async (data: UpdateProfileRequest): Promise<ProfileResponse> => {
    const response = await apiClient.put<ProfileResponse>('/profiles/me', data);
    return response.data;
  },

  changePassword: async (data: ChangePasswordRequest): Promise<void> => {
    await apiClient.put('/profiles/me/password', data);
  },
};

