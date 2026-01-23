import { useQuery, useQueryClient } from '@tanstack/react-query';
import { accountApi } from '../api';
import type { Order } from '../types';

/**
 * Hook to fetch current user's order history
 */
export function useOrders() {
  return useQuery<Order[], Error>({
    queryKey: ['orders'],
    queryFn: () => accountApi.getOrders(),
    staleTime: 30 * 1000, // 30 seconds
  });
}

/**
 * Hook to invalidate orders data
 */
export function useInvalidateOrders() {
  const queryClient = useQueryClient();
  
  return () => {
    queryClient.invalidateQueries({ queryKey: ['orders'] });
  };
}

