import { apiClient } from './client';
import type { Holding, PortfolioSummary } from '../types';

export const portfolioApi = {
  getHoldings: async (): Promise<Holding[]> => {
    return apiClient.get<Holding[]>('/portfolio');
  },

  getHolding: async (holdingId: number): Promise<Holding> => {
    return apiClient.get<Holding>(`/portfolio/${holdingId}`);
  },

  getSummary: async (): Promise<PortfolioSummary> => {
    return apiClient.get<PortfolioSummary>('/portfolio/summary');
  },
};

