import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '../api/orders';
import type { Order } from '../types';

export const useOrders = () => {
  return useQuery<Order[]>({
    queryKey: ['orders'],
    queryFn: ordersApi.getOrders,
  });
};

export const useOrder = (orderId: number) => {
  return useQuery<Order>({
    queryKey: ['orders', orderId],
    queryFn: () => ordersApi.getOrder(orderId),
    enabled: !!orderId,
  });
};

export const useBuy = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ordersApi.buy,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['portfolio'] });
    },
  });
};

export const useSell = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ordersApi.sell,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['portfolio'] });
    },
  });
};

