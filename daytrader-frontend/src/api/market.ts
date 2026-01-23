import apiClient from './client';
import type { Quote, MarketSummary } from '../types';

/**
 * Market API service
 */
export const marketApi = {
  /**
   * Get all stock quotes
   */
  async getQuotes(): Promise<Quote[]> {
    const response = await apiClient.get<Quote[]>('/market/quotes');
    return response.data;
  },

  /**
   * Get a specific quote by symbol
   */
  async getQuote(symbol: string): Promise<Quote> {
    const response = await apiClient.get<Quote>(`/market/quotes/${symbol}`);
    return response.data;
  },

  /**
   * Get market summary
   */
  async getSummary(): Promise<MarketSummary> {
    const response = await apiClient.get<MarketSummary>('/market/summary');
    return response.data;
  },
};

export default marketApi;

