import apiClient from './client';
import type { BuyRequest, Order } from '../types';

/**
 * Trade API service
 */
export const tradeApi = {
  /**
   * Buy stock
   */
  async buy(request: BuyRequest): Promise<Order> {
    const response = await apiClient.post<Order>('/trade/buy', request);
    return response.data;
  },

  /**
   * Sell a holding
   */
  async sell(holdingId: number): Promise<Order> {
    const response = await apiClient.post<Order>(`/trade/sell/${holdingId}`);
    return response.data;
  },
};

export default tradeApi;

