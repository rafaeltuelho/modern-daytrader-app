import type { PagedResponse } from './api.types';

// Trading-related types
export type OrderType = 'buy' | 'sell';
export type OrderStatus = 'open' | 'processing' | 'closed' | 'completed' | 'cancelled';

export interface CreateOrderRequest {
  orderType: OrderType;
  symbol?: string;
  quantity?: number;
  holdingId?: number;
}

export interface OrderResponse {
  id: number;
  orderType: OrderType;
  orderStatus: OrderStatus;
  symbol: string;
  quantity: number;
  price: number;
  orderFee: number;
  totalValue: number;
  openDate: string;
  completionDate?: string;
  holdingId?: number;
  accountId: number;
}

export type OrderListResponse = PagedResponse<OrderResponse>;

export interface HoldingResponse {
  id: number;
  symbol: string;
  companyName: string;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;
  purchaseValue: number;
  currentPrice: number;
  currentValue: number;
  gain: number;
  gainPercent: number;
}

export interface HoldingListResponse extends PagedResponse<HoldingResponse> {
  totalValue: number;
  totalGain: number;
}

export interface PortfolioSummaryResponse {
  accountId: number;
  cashBalance: number;
  holdingsValue: number;
  totalValue: number;
  totalGain: number;
  totalGainPercent: number;
  holdingsCount: number;
  recentOrders: OrderResponse[];
  topHoldings: HoldingResponse[];
}

