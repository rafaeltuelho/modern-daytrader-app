// Market-related types
export type MarketStatus = 'PRE_MARKET' | 'OPEN' | 'CLOSED' | 'AFTER_HOURS';

export interface QuoteSummary {
  symbol: string;
  companyName: string;
  price: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  priceChange: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MarketSummaryResponse {
  tsia: number;
  openTsia: number;
  volume: number;
  gainPercent: number;
  summaryDate: string;
  marketStatus: MarketStatus;
  topGainersCount: number;
  topLosersCount: number;
  topGainers?: QuoteSummary[];
  topLosers?: QuoteSummary[];
}

export interface MarketStatusResponse {
  status: MarketStatus;
  currentTime: string;
  marketOpen: string;
  marketClose: string;
  timezone: string;
  tradingDay: string;
  isHoliday: boolean;
}

export interface TopMoversResponse {
  movers: QuoteSummary[];
  count: number;
  asOf: string;
}

export interface VolumeStatsResponse {
  totalVolume: number;
  advancingVolume: number;
  decliningVolume: number;
  unchangedVolume: number;
  asOf: string;
}

export interface QuoteResponse {
  symbol: string;
  companyName: string;
  price: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  volume: number;
  priceChange: number;
  createdAt?: string;
  updatedAt?: string;
}

