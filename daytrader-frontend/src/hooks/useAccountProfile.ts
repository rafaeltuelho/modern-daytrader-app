import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { accountApi } from '../api/account';
import type { AccountProfile, UpdateProfileRequest } from '../types';

export const useAccountProfile = () => {
  return useQuery<AccountProfile>({
    queryKey: ['account', 'profile'],
    queryFn: accountApi.getProfile,
  });
};

export const useUpdateProfile = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => accountApi.updateProfile(data),
    onSuccess: (updatedProfile) => {
      // Update the cached profile data
      queryClient.setQueryData(['account', 'profile'], updatedProfile);
      // Invalidate to refetch and ensure consistency
      queryClient.invalidateQueries({ queryKey: ['account', 'profile'] });
    },
  });
};

