import apiClient from './client';
import type { Account, Holding, Order } from '../types';

/**
 * Account API service
 */
export const accountApi = {
  /**
   * Get current user's account information
   */
  async getAccount(): Promise<Account> {
    const response = await apiClient.get<Account>('/account');
    return response.data;
  },

  /**
   * Get current user's portfolio (holdings)
   */
  async getPortfolio(): Promise<Holding[]> {
    const response = await apiClient.get<Holding[]>('/trade/holdings');
    return response.data;
  },

  /**
   * Get current user's order history
   */
  async getOrders(): Promise<Order[]> {
    const response = await apiClient.get<Order[]>('/trade/orders');
    return response.data;
  },
};

export default accountApi;
