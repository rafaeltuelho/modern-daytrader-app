import apiClient from './client';
import type {
  MarketSummaryResponse,
  MarketStatusResponse,
  VolumeStatsResponse,
  QuoteResponse,
  QuoteSummary,
} from '../types/market.types';

export const marketApi = {
  // Market summary
  getMarketSummary: async (): Promise<MarketSummaryResponse> => {
    const response = await apiClient.get<MarketSummaryResponse>('/market/summary');
    return response.data;
  },

  getMarketStatus: async (): Promise<MarketStatusResponse> => {
    const response = await apiClient.get<MarketStatusResponse>('/market/status');
    return response.data;
  },

  // Top movers - API returns a direct array of QuoteSummary
  getTopGainers: async (limit: number = 10): Promise<QuoteSummary[]> => {
    const response = await apiClient.get<QuoteSummary[]>('/market/gainers', {
      params: { limit },
    });
    return response.data;
  },

  getTopLosers: async (limit: number = 10): Promise<QuoteSummary[]> => {
    const response = await apiClient.get<QuoteSummary[]>('/market/losers', {
      params: { limit },
    });
    return response.data;
  },

  // Volume stats
  getMarketVolume: async (): Promise<VolumeStatsResponse> => {
    const response = await apiClient.get<VolumeStatsResponse>('/market/volume');
    return response.data;
  },

  // Quotes
  getQuote: async (symbol: string): Promise<QuoteResponse> => {
    const response = await apiClient.get<QuoteResponse>(`/quotes/${symbol}`);
    return response.data;
  },

  getQuotes: async (symbols: string[]): Promise<QuoteResponse[]> => {
    const response = await apiClient.get<QuoteResponse[]>('/quotes', {
      params: { symbols: symbols.join(',') },
    });
    return response.data;
  },

  // Get all quotes (for quotes page with client-side filtering)
  getAllQuotes: async (): Promise<QuoteResponse[]> => {
    const response = await apiClient.get<QuoteResponse[]>('/quotes');
    return response.data;
  },
};

