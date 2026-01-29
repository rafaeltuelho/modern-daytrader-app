import { http, HttpResponse } from 'msw';
import type {
  User,
  LoginResponse,
  Quote,
  Holding,
  Order,
  MarketSummary,
  PortfolioSummary,
  AccountProfile,
} from '../../types';

const API_BASE_URL = 'http://localhost:8080/api/v1';

// Mock data
const mockUser: User = {
  accountID: 1,
  profileID: 'testuser',
  loginCount: 5,
  logoutCount: 4,
  lastLogin: '2024-01-15T10:30:00Z',
  creationDate: '2024-01-01T00:00:00Z',
  balance: 10000.0,
  openBalance: 10000.0,
};

const mockAccountProfile: AccountProfile = {
  userID: 'testuser',
  fullName: 'Test User',
  email: 'test@example.com',
  address: '123 Test St',
  creditCard: '****-****-****-1234',
};

const mockQuote: Quote = {
  symbol: 'AAPL',
  companyName: 'Apple Inc.',
  volume: 1000000,
  price: 150.25,
  open: 149.5,
  low: 148.75,
  high: 151.0,
  change: 0.75,
};

const mockHolding: Holding = {
  holdingID: 1,
  quantity: 100,
  purchasePrice: 140.0,
  purchaseDate: new Date('2024-01-01'),
  quoteID: 'AAPL',
  accountID: 1,
  symbol: 'AAPL',
  currentPrice: 150.25,
  marketValue: 15025.0,
  gain: 1025.0,
  gainPercent: 7.32,
};

const mockOrder: Order = {
  orderID: 1,
  orderType: 'buy',
  orderStatus: 'completed',
  openDate: new Date('2024-01-15'),
  completionDate: new Date('2024-01-15'),
  quantity: 100,
  price: 150.25,
  orderFee: 10.0,
  symbol: 'AAPL',
  accountID: 1,
};

const mockMarketSummary: MarketSummary = {
  tsia: 5000.0,
  openTSIA: 4950.0,
  volume: 10000000,
  topGainers: [mockQuote],
  topLosers: [],
  summaryDate: new Date(),
};

const mockPortfolioSummary: PortfolioSummary = {
  accountID: 1,
  balance: 10000.0,
  openBalance: 10000.0,
  holdingsValue: 15025.0,
  totalValue: 25025.0,
  gain: 1025.0,
  gainPercent: 4.27,
  numberOfHoldings: 1,
};

export const handlers = [
  // Auth endpoints
  http.post(`${API_BASE_URL}/auth/login`, async () => {
    const response: LoginResponse = {
      user: mockUser,
      token: 'mock-jwt-token',
      expiresIn: 3600,
      tokenType: 'Bearer',
    };
    return HttpResponse.json(response);
  }),

  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  http.post(`${API_BASE_URL}/auth/register`, () => {
    return new HttpResponse(null, { status: 201 });
  }),

  http.get(`${API_BASE_URL}/auth/me`, () => {
    return HttpResponse.json(mockUser);
  }),

  // Account endpoints
  http.get(`${API_BASE_URL}/account/profile`, () => {
    return HttpResponse.json(mockAccountProfile);
  }),

  http.put(`${API_BASE_URL}/account/profile`, () => {
    return HttpResponse.json(mockAccountProfile);
  }),

  // Portfolio endpoints
  // Note: /portfolio endpoint returns holdings array (used by getHoldings)
  http.get(`${API_BASE_URL}/portfolio`, () => {
    return HttpResponse.json([mockHolding]);
  }),

  http.get(`${API_BASE_URL}/portfolio/summary`, () => {
    return HttpResponse.json(mockPortfolioSummary);
  }),

  http.get(`${API_BASE_URL}/portfolio/holdings`, () => {
    return HttpResponse.json([mockHolding]);
  }),

  http.get(`${API_BASE_URL}/portfolio/:holdingId`, ({ params }) => {
    return HttpResponse.json(mockHolding);
  }),

  // Orders endpoints
  http.get(`${API_BASE_URL}/orders`, () => {
    return HttpResponse.json([mockOrder]);
  }),

  http.post(`${API_BASE_URL}/orders/buy`, () => {
    return HttpResponse.json(mockOrder);
  }),

  http.post(`${API_BASE_URL}/orders/sell`, () => {
    return HttpResponse.json(mockOrder);
  }),

  // Quotes endpoints
  http.get(`${API_BASE_URL}/quotes/:symbol`, ({ params }) => {
    const { symbol } = params;
    return HttpResponse.json({
      ...mockQuote,
      symbol: symbol as string,
    });
  }),

  // Market endpoints
  http.get(`${API_BASE_URL}/market/summary`, () => {
    return HttpResponse.json(mockMarketSummary);
  }),
];

// Export mock data for use in tests
export { mockUser, mockAccountProfile, mockQuote, mockHolding, mockOrder, mockMarketSummary, mockPortfolioSummary };

