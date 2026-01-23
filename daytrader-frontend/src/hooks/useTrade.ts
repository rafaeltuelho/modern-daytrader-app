import { useMutation, useQueryClient } from '@tanstack/react-query';
import { tradeApi } from '../api';
import type { Order, BuyRequest } from '../types';

/**
 * Hook to execute a buy order
 */
export function useBuy() {
  const queryClient = useQueryClient();

  return useMutation<Order, Error, BuyRequest>({
    mutationFn: (request) => tradeApi.buy(request),
    onSuccess: () => {
      // Invalidate related queries after successful trade
      queryClient.invalidateQueries({ queryKey: ['holdings'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['account'] });
    },
  });
}

/**
 * Hook to execute a sell order
 */
export function useSell() {
  const queryClient = useQueryClient();

  return useMutation<Order, Error, number>({
    mutationFn: (holdingId) => tradeApi.sell(holdingId),
    onSuccess: () => {
      // Invalidate related queries after successful trade
      queryClient.invalidateQueries({ queryKey: ['holdings'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['account'] });
    },
  });
}

