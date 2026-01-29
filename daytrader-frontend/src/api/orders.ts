import { apiClient } from './client';
import type { Order, BuyRequest, SellRequest } from '../types';

export const ordersApi = {
  getOrders: async (): Promise<Order[]> => {
    return apiClient.get<Order[]>('/orders');
  },

  getOrder: async (orderId: number): Promise<Order> => {
    return apiClient.get<Order>(`/orders/${orderId}`);
  },

  buy: async (data: BuyRequest): Promise<Order> => {
    return apiClient.post<Order>('/orders/buy', data);
  },

  sell: async (data: SellRequest): Promise<Order> => {
    return apiClient.post<Order>('/orders/sell', data);
  },
};

