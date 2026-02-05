import apiClient from './client';
import type {
  CreateOrderRequest,
  OrderResponse,
  HoldingResponse,
  HoldingListResponse,
  PortfolioSummaryResponse,
} from '../types/trading.types';

export const tradingApi = {
  // Orders
  createOrder: async (data: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await apiClient.post<OrderResponse>('/orders', data);
    return response.data;
  },

  getOrders: async (params?: {
    page?: number;
    size?: number;
    status?: string;
    orderType?: string;
    symbol?: string;
    fromDate?: string;
    toDate?: string;
    sort?: string;
  }): Promise<OrderResponse[]> => {
    const response = await apiClient.get<OrderResponse[]>('/orders', { params });
    return response.data;
  },

  getOrder: async (orderId: number): Promise<OrderResponse> => {
    const response = await apiClient.get<OrderResponse>(`/orders/${orderId}`);
    return response.data;
  },

  cancelOrder: async (orderId: number): Promise<OrderResponse> => {
    const response = await apiClient.post<OrderResponse>(`/orders/${orderId}/cancel`);
    return response.data;
  },

  // Holdings
  getHoldings: async (params?: {
    page?: number;
    size?: number;
    symbol?: string;
    sort?: string;
  }): Promise<HoldingListResponse> => {
    const response = await apiClient.get<HoldingListResponse>('/holdings', { params });
    return response.data;
  },

  getHolding: async (holdingId: number): Promise<HoldingResponse> => {
    const response = await apiClient.get<HoldingResponse>(`/holdings/${holdingId}`);
    return response.data;
  },

  // Portfolio
  getPortfolioSummary: async (): Promise<PortfolioSummaryResponse> => {
    const response = await apiClient.get<PortfolioSummaryResponse>('/portfolio/summary');
    return response.data;
  },
};

