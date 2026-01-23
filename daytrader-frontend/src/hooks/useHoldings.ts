import { useQuery, useQueryClient } from '@tanstack/react-query';
import { accountApi } from '../api';
import type { Holding } from '../types';

/**
 * Hook to fetch current user's portfolio holdings
 */
export function useHoldings() {
  return useQuery<Holding[], Error>({
    queryKey: ['holdings'],
    queryFn: () => accountApi.getPortfolio(),
    staleTime: 30 * 1000, // 30 seconds
  });
}

/**
 * Hook to invalidate holdings data
 */
export function useInvalidateHoldings() {
  const queryClient = useQueryClient();
  
  return () => {
    queryClient.invalidateQueries({ queryKey: ['holdings'] });
  };
}

