// Core domain types for DayTrader API

// User type matches backend AccountDTO response
export interface User {
  accountID: number;
  profileID: string;
  loginCount: number;
  logoutCount: number;
  lastLogin?: string;
  creationDate?: string;
  balance: number;
  openBalance: number;
}

export interface AccountProfile {
  userID: string;
  password?: string;
  fullName: string;
  address?: string;
  email: string;
  creditCard?: string;
}

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

export interface Holding {
  holdingID: number;
  quantity: number;
  purchasePrice: number;
  purchaseDate: Date;
  quoteID: string;
  accountID: number;
  symbol?: string;
  currentPrice?: number;
  marketValue?: number;
  gain?: number;
  gainPercent?: number;
}

export interface Order {
  orderID: number;
  orderType: 'buy' | 'sell';
  orderStatus: 'open' | 'processing' | 'completed' | 'cancelled' | 'closed';
  openDate: Date;
  completionDate?: Date;
  quantity: number;
  price: number;
  orderFee: number;
  symbol: string;
  accountID: number;
  holdingID?: number;
}

export interface MarketSummary {
  tsia: number;
  openTSIA: number;
  volume: number;
  topGainers: Quote[];
  topLosers: Quote[];
  summaryDate: Date;
}

export interface PortfolioSummary {
  accountID: number;
  balance: number;
  openBalance: number;
  holdingsValue: number;
  totalValue: number;
  gain: number;
  gainPercent: number;
  numberOfHoldings: number;
}

// API Request/Response types
export interface LoginRequest {
  userID: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  token: string;
  expiresIn: number;
  tokenType: string;
}

export interface RegisterRequest {
  userID: string;
  password: string;
  fullName: string;
  email: string;
  address?: string;
  creditCard?: string;
}

export interface BuyRequest {
  symbol: string;
  quantity: number;
}

export interface SellRequest {
  holdingID: number;
}

export interface UpdateProfileRequest {
  fullName?: string;
  address?: string;
  email?: string;
  creditCard?: string;
}

// API Error types
export interface ApiError {
  message: string;
  code?: string;
  status?: number;
  details?: Record<string, unknown>;
}

// Auth context types
export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthContextType extends AuthState {
  login: (userID: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
}

