import { apiClient } from './client';
import type { AccountProfile, UpdateProfileRequest } from '../types';

export const accountApi = {
  getProfile: async (): Promise<AccountProfile> => {
    return apiClient.get<AccountProfile>('/accounts/profile');
  },

  updateProfile: async (data: UpdateProfileRequest): Promise<AccountProfile> => {
    return apiClient.put<AccountProfile>('/accounts/profile', data);
  },
};

