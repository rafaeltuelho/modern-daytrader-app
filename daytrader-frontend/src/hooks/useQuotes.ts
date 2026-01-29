import { useQuery } from '@tanstack/react-query';
import { quotesApi } from '../api/quotes';
import type { Quote } from '../types';

export const useQuotes = () => {
  return useQuery<Quote[]>({
    queryKey: ['quotes'],
    queryFn: quotesApi.getAllQuotes,
  });
};

export const useQuote = (symbol: string) => {
  return useQuery<Quote>({
    queryKey: ['quotes', symbol],
    queryFn: () => quotesApi.getQuote(symbol),
    enabled: !!symbol,
  });
};

