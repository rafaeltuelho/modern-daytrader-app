/**
 * TypeScript type definitions for DayTrader application.
 * These match the backend Quarkus DTOs and entities.
 */

// Account Profile
export interface AccountProfile {
  userID: string;
  fullName: string;
  address?: string;
  email?: string;
  creditCard?: string;
}

// Account
export interface Account {
  id: number;
  loginCount: number;
  logoutCount: number;
  lastLogin: string;
  creationDate: string;
  balance: number;
  openBalance: number;
  profile?: AccountProfile;
}

// Quote
export interface Quote {
  symbol: string;
  companyName: string;
  volume: number;
  price: number;
  open: number;
  low: number;
  high: number;
  change: number;
}

// Holding
export interface Holding {
  id: number;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;
  quote?: Quote;
}

// Order
export interface Order {
  id: number;
  orderType: 'buy' | 'sell';
  orderStatus: 'open' | 'processing' | 'completed' | 'closed' | 'cancelled';
  openDate: string;
  completionDate?: string;
  quantity: number;
  price: number;
  orderFee: number;
  quote?: Quote;
}

// Market Summary
export interface MarketSummary {
  tsia: number;
  openTsia: number;
  volume: number;
  topGainers: Quote[];
  topLosers: Quote[];
  summaryDate: string;
}

// Auth DTOs
export interface LoginRequest {
  userID: string;
  password: string;
}

export interface LoginResponse {
  userID: string;
  token: string;
  tokenType: string;
  expiresIn: number;
}

export interface RegisterRequest {
  userID: string;
  password: string;
  fullName: string;
  address?: string;
  email?: string;
  creditCard?: string;
  openBalance: number;
}

// Trade DTOs
export interface BuyRequest {
  symbol: string;
  quantity: number;
}

export interface SellRequest {
  holdingId: number;
}

// API Error Response
export interface ApiError {
  error: string;
  message?: string;
  status?: number;
}

