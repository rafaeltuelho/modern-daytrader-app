import { useQuery } from '@tanstack/react-query';
import { marketApi } from '../api/market';
import type { MarketSummary } from '../types';

export const useMarketSummary = (refetchInterval?: number) => {
  return useQuery<MarketSummary>({
    queryKey: ['market', 'summary'],
    queryFn: marketApi.getSummary,
    refetchInterval: refetchInterval,
  });
};

