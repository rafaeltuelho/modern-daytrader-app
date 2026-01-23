import { useQuery, useQueryClient } from '@tanstack/react-query';
import { accountApi } from '../api';
import type { Account } from '../types';

/**
 * Hook to fetch current user's account information
 */
export function useAccount() {
  return useQuery<Account, Error>({
    queryKey: ['account'],
    queryFn: () => accountApi.getAccount(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook to invalidate account data (call after trades, etc.)
 */
export function useInvalidateAccount() {
  const queryClient = useQueryClient();
  
  return () => {
    queryClient.invalidateQueries({ queryKey: ['account'] });
  };
}

