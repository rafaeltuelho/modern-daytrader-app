import { apiClient } from './client';
import type { MarketSummary } from '../types';

export const marketApi = {
  getSummary: async (): Promise<MarketSummary> => {
    return apiClient.get<MarketSummary>('/market/summary');
  },
};

