import { useQuery } from '@tanstack/react-query';
import { marketApi } from '../api';
import type { Quote, MarketSummary } from '../types';

/**
 * Hook to fetch all stock quotes
 */
export function useQuotes() {
  return useQuery<Quote[], Error>({
    queryKey: ['quotes'],
    queryFn: () => marketApi.getQuotes(),
    staleTime: 30 * 1000, // 30 seconds
  });
}

/**
 * Hook to fetch a single quote by symbol
 */
export function useQuote(symbol: string) {
  return useQuery<Quote, Error>({
    queryKey: ['quote', symbol],
    queryFn: () => marketApi.getQuote(symbol),
    enabled: !!symbol,
    staleTime: 10 * 1000, // 10 seconds
  });
}

/**
 * Hook to fetch market summary
 */
export function useMarketSummary() {
  return useQuery<MarketSummary, Error>({
    queryKey: ['marketSummary'],
    queryFn: () => marketApi.getSummary(),
    staleTime: 60 * 1000, // 1 minute
  });
}

