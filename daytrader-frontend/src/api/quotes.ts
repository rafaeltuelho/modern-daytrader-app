import { apiClient } from './client';
import type { Quote } from '../types';

export const quotesApi = {
  getAllQuotes: async (): Promise<Quote[]> => {
    return apiClient.get<Quote[]>('/quotes');
  },

  getQuote: async (symbol: string): Promise<Quote> => {
    return apiClient.get<Quote>(`/quotes/${symbol}`);
  },

  createQuote: async (data: { symbol: string; companyName: string; price: number }): Promise<Quote> => {
    return apiClient.post<Quote>('/quotes', data);
  },

  updateQuotePrice: async (symbol: string, price: number): Promise<Quote> => {
    return apiClient.put<Quote>(`/quotes/${symbol}/price`, { price });
  },
};

