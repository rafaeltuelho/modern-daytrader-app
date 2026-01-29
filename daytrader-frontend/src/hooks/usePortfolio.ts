import { useQuery } from '@tanstack/react-query';
import { portfolioApi } from '../api/portfolio';
import type { Holding, PortfolioSummary } from '../types';

export const usePortfolio = () => {
  return useQuery<Holding[]>({
    queryKey: ['portfolio', 'holdings'],
    queryFn: portfolioApi.getHoldings,
  });
};

export const usePortfolioSummary = () => {
  return useQuery<PortfolioSummary>({
    queryKey: ['portfolio', 'summary'],
    queryFn: portfolioApi.getSummary,
  });
};

export const useHolding = (holdingId: number) => {
  return useQuery<Holding>({
    queryKey: ['portfolio', 'holding', holdingId],
    queryFn: () => portfolioApi.getHolding(holdingId),
    enabled: !!holdingId,
  });
};

