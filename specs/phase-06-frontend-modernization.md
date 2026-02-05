# Phase 6: Frontend Modernization - Specification

## Executive Summary

This document specifies the modernization of the DayTrader frontend from legacy JSF/JSP to a modern React-based Single Page Application (SPA). The new frontend will consume the modernized Quarkus backend services via REST APIs and provide a responsive, accessible user experience.

---

## Phase Objectives

1. **Technology Modernization**: Replace JSF 2.2/XHTML Facelets and JSP with React 18+ and TypeScript
2. **API-First Architecture**: Consume REST APIs from Account, Trading, and Market services
3. **JWT Authentication**: Implement client-side authentication with secure token management
4. **Responsive Design**: Mobile-first responsive UI using Tailwind CSS
5. **Developer Experience**: Modern tooling with Vite, ESLint, Prettier, and TypeScript
6. **Feature Parity**: Maintain all existing functionality from the legacy frontend

---

## 1. Technology Stack

### Core Dependencies

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| **Framework** | React | 18.x | UI component library |
| **Language** | TypeScript | 5.x | Type-safe JavaScript |
| **Build Tool** | Vite | 5.x | Fast development and build |
| **Routing** | React Router | 6.x | Client-side navigation |
| **Server State** | TanStack Query (React Query) | 5.x | Server state management, caching |
| **Client State** | Zustand | 4.x | Lightweight client state |
| **Styling** | Tailwind CSS | 3.x | Utility-first CSS framework |
| **HTTP Client** | Axios | 1.x | HTTP requests with interceptors |
| **Forms** | React Hook Form | 7.x | Form state and validation |
| **Validation** | Zod | 3.x | Schema validation |
| **Icons** | Lucide React | 0.3x | SVG icon library |
| **Charts** | Recharts | 2.x | Market data visualization |
| **Date/Time** | date-fns | 3.x | Date manipulation |

### Development Dependencies

| Tool | Purpose |
|------|---------|
| ESLint | Code linting |
| Prettier | Code formatting |
| Vitest | Unit testing |
| Testing Library | Component testing |
| Playwright | End-to-end testing (optional) |
| MSW (Mock Service Worker) | API mocking for tests |

---

## 2. Project Structure

```
daytrader-frontend/
├── public/
│   ├── favicon.ico
│   └── images/                    # Static images (logos, icons)
├── src/
│   ├── api/                       # API client modules
│   │   ├── client.ts              # Axios instance with interceptors
│   │   ├── account.api.ts         # Account service API calls
│   │   ├── trading.api.ts         # Trading service API calls
│   │   ├── market.api.ts          # Market service API calls
│   │   └── quote.api.ts           # Quote service API calls
│   ├── components/                # Reusable UI components
│   │   ├── ui/                    # Base UI components
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Alert.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Spinner.tsx
│   │   │   └── index.ts
│   │   ├── layout/                # Layout components
│   │   │   ├── Header.tsx
│   │   │   ├── Navigation.tsx
│   │   │   ├── Footer.tsx
│   │   │   ├── AuthLayout.tsx
│   │   │   └── DashboardLayout.tsx
│   │   ├── trading/               # Trading domain components
│   │   │   ├── QuoteCard.tsx
│   │   │   ├── QuoteTable.tsx
│   │   │   ├── HoldingRow.tsx
│   │   │   ├── OrderRow.tsx
│   │   │   ├── OrderForm.tsx
│   │   │   ├── BuyForm.tsx
│   │   │   ├── SellButton.tsx
│   │   │   └── PortfolioSummary.tsx
│   │   ├── market/                # Market domain components
│   │   │   ├── MarketSummaryCard.tsx
│   │   │   ├── TopMoversTable.tsx
│   │   │   ├── GainersLosersPanel.tsx
│   │   │   └── TsiaChart.tsx
│   │   └── account/               # Account domain components
│   │       ├── AccountInfo.tsx
│   │       ├── ProfileForm.tsx
│   │       ├── OrderHistory.tsx
│   │       └── CompletedOrdersAlert.tsx
│   ├── hooks/                     # Custom React hooks
│   │   ├── useAuth.ts             # Authentication hook
│   │   ├── useAccount.ts          # Account queries
│   │   ├── usePortfolio.ts        # Portfolio queries
│   │   ├── useOrders.ts           # Orders queries/mutations
│   │   ├── useQuotes.ts           # Quotes queries
│   │   ├── useMarket.ts           # Market summary queries
│   │   └── useWebSocket.ts        # WebSocket connections
│   ├── pages/                     # Route page components
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── HomePage.tsx           # Dashboard/trade home
│   │   ├── AccountPage.tsx
│   │   ├── PortfolioPage.tsx
│   │   ├── QuotePage.tsx
│   │   ├── MarketPage.tsx
│   │   ├── OrderConfirmPage.tsx
│   │   └── NotFoundPage.tsx
│   ├── stores/                    # Client state stores
│   │   └── authStore.ts           # Auth state (Zustand)
│   ├── types/                     # TypeScript types
│   │   ├── account.types.ts
│   │   ├── trading.types.ts
│   │   ├── market.types.ts
│   │   ├── quote.types.ts
│   │   └── api.types.ts
│   ├── utils/                     # Utility functions
│   │   ├── formatters.ts          # Number/date formatters
│   │   ├── validators.ts          # Form validation schemas
│   │   └── constants.ts           # App constants
│   ├── App.tsx                    # Root component with routing
│   ├── main.tsx                   # Entry point
│   └── index.css                  # Global styles + Tailwind
├── tests/
│   ├── setup.ts                   # Test setup
│   ├── mocks/                     # MSW handlers
│   │   └── handlers.ts
│   └── components/                # Component tests
├── .env.example                   # Environment variables template
├── .env.development               # Dev environment config
├── .eslintrc.cjs                  # ESLint config
├── .prettierrc                    # Prettier config
├── index.html                     # HTML entry point
├── package.json
├── tailwind.config.js
├── tsconfig.json
├── vite.config.ts
└── Dockerfile
```

### Naming Conventions

| Entity | Convention | Example |
|--------|------------|---------|
| Components | PascalCase | `QuoteCard.tsx`, `BuyForm.tsx` |
| Hooks | camelCase with `use` prefix | `useAuth.ts`, `useOrders.ts` |
| API modules | camelCase with `.api` suffix | `account.api.ts` |
| Types | PascalCase with descriptive suffix | `AccountResponse`, `CreateOrderRequest` |
| Stores | camelCase with `Store` suffix | `authStore.ts` |
| Utilities | camelCase | `formatters.ts` |
| Test files | Same as source with `.test` suffix | `QuoteCard.test.tsx` |

---

## 3. Authentication Architecture

### ADR Reference
Per **ADR-001**: Simple JWT authentication without OIDC/Keycloak.

### Token Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Frontend Authentication Flow                      │
└─────────────────────────────────────────────────────────────────────────┘

   ┌──────────────┐                                    ┌──────────────────┐
   │   Browser    │                                    │  Account Service │
   │  (React SPA) │                                    │   (port 8080)    │
   └──────┬───────┘                                    └────────┬─────────┘
          │                                                     │
          │  1. POST /api/auth/login {userId, password}         │
          │ ────────────────────────────────────────────────►   │
          │                                                     │
          │  2. Return {accessToken, refreshToken, expiresIn}   │
          │ ◄────────────────────────────────────────────────   │
          │                                                     │
          │  3. Store tokens (memory + localStorage backup)     │
          │  ─────────────────────────────────                  │
          │                                                     │
          │  4. API calls with Authorization: Bearer <token>    │
          │ ────────────────────────────────────────────────►   │
          │                                                     │
          │  5. On 401: Attempt token refresh or redirect login │
          │ ◄────────────────────────────────────────────────   │
```



### Token Storage Strategy

**Primary Storage**: In-memory (Zustand store)
- Tokens stored in JavaScript memory during session
- Most secure against XSS (not accessible via document.cookie or localStorage)

**Backup Storage**: localStorage (for page refresh persistence)
- Encrypted token stored for session recovery
- Cleared on explicit logout
- Automatically cleared on token expiration

### Auth Store Implementation (Zustand)

```typescript
// src/stores/authStore.ts
interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  refreshAccessToken: () => Promise<boolean>;
  hydrateFromStorage: () => void;
}
```

### Protected Routes

```typescript
// src/components/auth/ProtectedRoute.tsx
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthStore();
  const location = useLocation();

  if (isLoading) return <LoadingSpinner />;
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return <>{children}</>;
};
```

---

## 4. Page Specifications

### 4.1 Login Page (`/login`)

**Legacy Reference**: `welcome.xhtml`

**Route**: `/login` (public)

**Components**:
- `LoginForm` - Form with userId/password inputs
- `AuthLayout` - Centered card layout

**API Endpoints**:
- `POST /api/auth/login` → Account Service (port 8080)

**Form Fields**:
| Field | Type | Validation | Legacy Mapping |
|-------|------|------------|----------------|
| userId | text | Required, min 1 char | `#{tradeapp.userID}` |
| password | password | Required, min 1 char | `#{tradeapp.password}` |

**User Interactions**:
- Submit form → validate → call login API → store tokens → redirect to `/`
- Show error message on invalid credentials
- Link to registration page

**Responsive Design**:
- Mobile: Full-width card, stacked form
- Desktop: Centered card (max-width 400px)

---

### 4.2 Register Page (`/register`)

**Legacy Reference**: `register.xhtml`

**Route**: `/register` (public)

**Components**:
- `RegistrationForm` - Multi-field registration form
- `AuthLayout` - Centered card layout

**API Endpoints**:
- `POST /api/accounts/register` → Account Service (port 8080)

**Form Fields**:
| Field | Type | Validation | Legacy Mapping |
|-------|------|------------|----------------|
| userId | text | Required, unique | `#{tradeapp.userID}` |
| password | password | Required, 1-12 chars | `#{tradeapp.password}` |
| confirmPassword | password | Must match password | `#{tradeapp.cpassword}` |
| fullName | text | Required | `#{tradeapp.fullname}` |
| address | text | Required | `#{tradeapp.address}` |
| email | email | Required, valid email | `#{tradeapp.email}` |
| creditCard | text | Required, 1-30 chars | `#{tradeapp.ccn}` |
| openBalance | number | Required, positive | `#{tradeapp.openBalance}` |

**User Interactions**:
- Submit form → validate → call register API → redirect to login
- Show validation errors inline
- Link back to login page

---

### 4.3 Home/Dashboard Page (`/`)

**Legacy Reference**: `tradehome.xhtml`

**Route**: `/` (protected)

**Components**:
- `DashboardLayout` - Main app layout with navigation
- `MarketSummaryCard` - TSIA index and volume
- `TopMoversTable` - Recent price changes
- `QuickQuoteLookup` - Symbol search widget
- `CompletedOrdersAlert` - Alert for closed orders

**API Endpoints**:
- `GET /api/market/summary` → Market Service (port 8082)
- `GET /api/orders/closed` → Trading Service (port 8081)

**User Interactions**:
- View market summary at a glance
- Quick quote lookup by symbol
- Navigation to other sections
- Dismiss completed order alerts

---

### 4.4 Account Page (`/account`)

**Legacy Reference**: `account.xhtml`

**Route**: `/account` (protected)

**Components**:
- `AccountInfo` - Read-only account details
- `ProfileForm` - Editable profile fields
- `OrderHistory` - Paginated order history table
- `CompletedOrdersAlert` - Alert for closed orders

**API Endpoints**:
- `GET /api/accounts/{userId}` → Account Service (port 8080)
- `GET /api/accounts/{userId}/profile` → Account Service (port 8080)
- `PUT /api/accounts/{userId}/profile` → Account Service (port 8080)
- `GET /api/orders` → Trading Service (port 8081)
- `GET /api/orders/closed` → Trading Service (port 8081)

**Account Info Display** (read-only):
| Field | Legacy Mapping |
|-------|----------------|
| Account ID | `#{accountdata.accountID}` |
| User ID | `#{accountdata.profileID}` |
| Account Created | `#{accountdata.creationDate}` |
| Last Login | `#{accountdata.lastLogin}` |
| Total Logins | `#{accountdata.loginCount}` |
| Total Logouts | `#{accountdata.logoutCount}` |
| Cash Balance | `#{accountdata.balance}` |
| Opening Balance | `#{accountdata.openBalance}` |

**Profile Form Fields** (editable):
| Field | Editable | Legacy Mapping |
|-------|----------|----------------|
| User ID | Yes | `#{tradeapp.userID}` |
| Password | Yes | `#{tradeapp.password}` |
| Confirm Password | Yes | `#{tradeapp.cpassword}` |
| Full Name | No | `#{tradeapp.fullname}` |
| Address | No | `#{tradeapp.address}` |
| Email | Yes | `#{tradeapp.email}` |
| Credit Card | Yes | `#{tradeapp.ccn}` |

**Order History Table Columns**:
| Column | Legacy Mapping |
|--------|----------------|
| Order ID | `#{item.orderID}` |
| Status | `#{item.orderStatus}` |
| Creation Date | `#{item.openDate}` |
| Completion Date | `#{item.completionDate}` |
| Txn Fee | `#{item.orderFee}` |
| Type | `#{item.orderType}` |
| Symbol | `#{item.symbol}` |
| Quantity | `#{item.quantity}` |
| Price | `#{item.price}` |
| Total | `#{item.total}` |

---

### 4.5 Portfolio Page (`/portfolio`)

**Legacy Reference**: `portfolio.xhtml`

**Route**: `/portfolio` (protected)

**Components**:
- `PortfolioSummary` - Total value, gain/loss
- `HoldingsTable` - List of holdings with sell buttons
- `CompletedOrdersAlert` - Alert for closed orders

**API Endpoints**:
- `GET /api/portfolio/holdings` → Trading Service (port 8081)
- `GET /api/portfolio/summary` → Trading Service (port 8081)
- `POST /api/orders` (sell) → Trading Service (port 8081)
- `GET /api/orders/closed` → Trading Service (port 8081)

**Holdings Table Columns**:
| Column | Action | Legacy Mapping |
|--------|--------|----------------|
| Holding ID | - | `#{item.holdingID}` |
| Purchase Date | - | `#{item.purchaseDate}` |
| Symbol | Link to quote | `#{item.quoteID}` |
| Quantity | - | `#{item.quantity}` |
| Purchase Price | - | `#{item.purchasePrice}` |
| Current Price | - | `#{item.quote.price}` |
| Gain/Loss | Color coded | `#{item.gainHTML}` |
| Action | Sell button | `#{portfolio.sell}` |

**User Interactions**:
- Click "Sell" button → opens sell confirmation modal
- Confirm sell → POST order → refresh holdings
- Click symbol → navigate to quote page



### 4.6 Quote Page (`/quotes`)

**Legacy Reference**: `quote.xhtml`

**Route**: `/quotes` or `/quotes?symbol=AAPL` (protected)

**Components**:
- `QuoteLookupForm` - Symbol search input
- `QuoteTable` - Display multiple quotes
- `BuyForm` - Buy quantity input per quote
- `CompletedOrdersAlert` - Alert for closed orders

**API Endpoints**:
- `GET /api/quotes/{symbol}` → Market Service (port 8082)
- `GET /api/quotes?symbols=SYM1,SYM2` → Market Service (port 8082)
- `POST /api/orders` (buy) → Trading Service (port 8081)
- `GET /api/orders/closed` → Trading Service (port 8081)

**Quote Table Columns**:
| Column | Legacy Mapping |
|--------|----------------|
| Symbol | `#{item.symbol}` |
| Company | `#{item.companyName}` |
| Volume | `#{item.volume}` |
| Range | `#{item.range}` |
| Open Price | `#{item.open}` |
| Current Price | `#{item.price}` |
| Gain/Loss | `#{item.gainHTML}`, `#{item.gainPercentHTML}` |
| Trade (Buy) | Buy button + quantity input |

**User Interactions**:
- Enter symbol(s) → search → display quotes
- Enter quantity → click "Buy" → confirm order
- View gain/loss with color indicators (green/red)

---

### 4.7 Market Summary Page (`/market`)

**Legacy Reference**: `marketSummary.xhtml`

**Route**: `/market` (protected)

**Components**:
- `MarketSummaryCard` - TSIA index, trading volume
- `RecentChangesTable` - Recent price changes (5 stocks)
- `TopGainersTable` - Top 5 gainers
- `TopLosersTable` - Top 5 losers
- `CompletedOrdersAlert` - Alert for closed orders

**API Endpoints**:
- `GET /api/market/summary` → Market Service (port 8082)
- `GET /api/market/gainers?limit=5` → Market Service (port 8082)
- `GET /api/market/losers?limit=5` → Market Service (port 8082)
- `GET /api/orders/closed` → Trading Service (port 8081)

**Market Summary Display**:
| Metric | Legacy Element ID |
|--------|------------------|
| TSIA Index | `#tsia` |
| Trading Volume | `#volume` |
| Market Date | `#date` |

**Top Movers Tables** (Gainers/Losers):
| Column | Legacy Pattern |
|--------|----------------|
| Symbol | `#gainer1_stock`, `#loser1_stock` |
| Price | `#gainer1_price`, `#loser1_price` |
| Change | `#gainer1_change`, `#loser1_change` |

**Real-time Updates**:
- Legacy used WebSocket for live updates
- Modern: Use TanStack Query with polling (refetchInterval: 5000ms)
- Optional: WebSocket integration for true real-time

---

### 4.8 Order Confirmation Page (`/orders/:orderId`)

**Route**: `/orders/:orderId` (protected)

**Components**:
- `OrderConfirmation` - Order details card
- `BackToPortfolio` - Navigation button

**API Endpoints**:
- `GET /api/orders/{orderId}` → Trading Service (port 8081)

**Order Details Display**:
| Field | Description |
|-------|-------------|
| Order ID | Unique order identifier |
| Order Type | BUY or SELL |
| Symbol | Stock symbol |
| Quantity | Number of shares |
| Price | Execution price |
| Total | Total transaction amount |
| Fee | Transaction fee |
| Status | Order status |
| Created | Order creation timestamp |

---

## 5. Component Library

### 5.1 Base UI Components

#### Button
```typescript
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'danger' | 'ghost';
  size: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  disabled?: boolean;
  children: React.ReactNode;
  onClick?: () => void;
}
```

#### Input
```typescript
interface InputProps {
  type: 'text' | 'password' | 'email' | 'number';
  label?: string;
  error?: string;
  placeholder?: string;
  // React Hook Form compatible
}
```

#### Table
```typescript
interface TableProps<T> {
  columns: ColumnDef<T>[];
  data: T[];
  isLoading?: boolean;
  emptyMessage?: string;
  onRowClick?: (row: T) => void;
}
```

#### Alert
```typescript
interface AlertProps {
  variant: 'info' | 'success' | 'warning' | 'error';
  title?: string;
  children: React.ReactNode;
  onDismiss?: () => void;
}
```

### 5.2 Domain Components

#### CompletedOrdersAlert
- Displays when user has closed orders to acknowledge
- Fetches from `GET /api/orders/closed`
- Shown on Account, Portfolio, Quote, and Market pages (per legacy behavior)

#### MarketSummaryCard
- TSIA index with up/down arrow indicator
- Trading volume
- Color-coded gain/loss

#### QuoteCard
- Individual stock quote display
- Symbol, company, price, change
- Buy button integration

---

## 6. API Integration Layer

### 6.1 Axios Client Configuration

```typescript
// src/api/client.ts
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add JWT token
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshed = await useAuthStore.getState().refreshAccessToken();
      if (refreshed) {
        return apiClient.request(error.config);
      }
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 6.2 Service-Specific API Modules

| Module | Base URL | Service |
|--------|----------|---------|
| `account.api.ts` | `/api/auth`, `/api/accounts` | Account Service (8080) |
| `trading.api.ts` | `/api/orders`, `/api/portfolio` | Trading Service (8081) |
| `quote.api.ts` | `/api/quotes` | Market Service (8082) |
| `market.api.ts` | `/api/market` | Market Service (8082) |

### 6.3 TanStack Query Hooks Pattern

```typescript
// src/hooks/usePortfolio.ts
export const useHoldings = () => {
  return useQuery({
    queryKey: ['holdings'],
    queryFn: () => tradingApi.getHoldings(),
    staleTime: 30000, // 30 seconds
  });
};

export const useSellHolding = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (holdingId: number) => tradingApi.sellHolding(holdingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['holdings'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
```



### 6.4 Error Handling

```typescript
// Standard API error response type
interface ApiError {
  status: number;
  code: string;
  message: string;
  details?: Record<string, string[]>;
}

// Error handling in components
const { error, isError } = useQuery(...);
if (isError) {
  return <Alert variant="error">{error.message}</Alert>;
}
```

### 6.5 Loading States

- Use TanStack Query's `isLoading` and `isFetching` states
- Show `Spinner` component for full-page loading
- Show skeleton loaders for partial content loading
- Disable form submissions during mutation pending states

---

## 7. Development Environment

### 7.1 Environment Variables

```bash
# .env.example
VITE_ACCOUNT_SERVICE_URL=http://localhost:8080
VITE_TRADING_SERVICE_URL=http://localhost:8081
VITE_MARKET_SERVICE_URL=http://localhost:8082
VITE_API_TIMEOUT=10000
```

```bash
# .env.development
VITE_ACCOUNT_SERVICE_URL=http://localhost:8080
VITE_TRADING_SERVICE_URL=http://localhost:8081
VITE_MARKET_SERVICE_URL=http://localhost:8082
```

### 7.2 Vite Proxy Configuration

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/accounts': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/portfolio': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/quotes': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/market': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
});
```

### 7.3 Docker Configuration

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 7.4 Docker Compose Integration

```yaml
# Add to docker/docker-compose.yml
daytrader-frontend:
  build:
    context: ../daytrader-frontend
    dockerfile: Dockerfile
  ports:
    - "3000:80"
  depends_on:
    - daytrader-account-service
    - daytrader-trading-service
    - daytrader-market-service
```

---

## 8. Testing Strategy

### 8.1 Unit Tests (Vitest)

**Coverage Target**: 80% for utilities, hooks, and stores

| Test Type | Location | Tools |
|-----------|----------|-------|
| Utility functions | `tests/utils/` | Vitest |
| Form validators | `tests/utils/` | Vitest, Zod |
| Store actions | `tests/stores/` | Vitest, Zustand |

### 8.2 Component Tests (Testing Library)

**Coverage Target**: 70% for components

```typescript
// Example: QuoteCard.test.tsx
import { render, screen } from '@testing-library/react';
import { QuoteCard } from './QuoteCard';

describe('QuoteCard', () => {
  it('displays stock symbol and price', () => {
    render(<QuoteCard symbol="AAPL" price={150.00} change={2.50} />);
    expect(screen.getByText('AAPL')).toBeInTheDocument();
    expect(screen.getByText('$150.00')).toBeInTheDocument();
  });

  it('shows green color for positive change', () => {
    render(<QuoteCard symbol="AAPL" price={150.00} change={2.50} />);
    expect(screen.getByTestId('change-indicator')).toHaveClass('text-green-600');
  });
});
```

### 8.3 API Mocking (MSW)

```typescript
// tests/mocks/handlers.ts
import { rest } from 'msw';

export const handlers = [
  rest.post('/api/auth/login', (req, res, ctx) => {
    return res(
      ctx.json({
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token',
        expiresIn: 3600,
      })
    );
  }),

  rest.get('/api/portfolio/holdings', (req, res, ctx) => {
    return res(
      ctx.json([
        { holdingId: 1, symbol: 'AAPL', quantity: 100, purchasePrice: 145.00 },
      ])
    );
  }),
];
```

### 8.4 E2E Tests (Playwright - Optional)

```typescript
// e2e/login.spec.ts
test('user can login and view portfolio', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[name="userId"]', 'uid:0');
  await page.fill('[name="password"]', 'xxx');
  await page.click('button[type="submit"]');

  await expect(page).toHaveURL('/');
  await page.click('text=Portfolio');
  await expect(page.locator('h1')).toContainText('Portfolio');
});
```

---

## 9. Acceptance Criteria

### 9.1 Functional Requirements

| ID | Requirement | Acceptance Criteria |
|----|-------------|---------------------|
| FR-01 | User Login | User can login with valid credentials and receive JWT |
| FR-02 | User Registration | New user can register with all required fields |
| FR-03 | View Portfolio | Authenticated user can view their holdings |
| FR-04 | Buy Stock | User can purchase stock with quantity input |
| FR-05 | Sell Stock | User can sell holdings from portfolio |
| FR-06 | View Quotes | User can search and view stock quotes |
| FR-07 | Market Summary | User can view TSIA, gainers, and losers |
| FR-08 | Account Profile | User can view and update profile |
| FR-09 | Order History | User can view past orders |
| FR-10 | Completed Orders | User sees alerts for closed orders |



### 9.2 Non-Functional Requirements

| ID | Requirement | Acceptance Criteria |
|----|-------------|---------------------|
| NFR-01 | Responsive Design | UI works on mobile (320px) to desktop (1920px) |
| NFR-02 | Performance | Initial load < 3 seconds on 3G connection |
| NFR-03 | Accessibility | WCAG 2.1 Level AA compliance |
| NFR-04 | Browser Support | Chrome, Firefox, Safari, Edge (latest 2 versions) |
| NFR-05 | Error Handling | User-friendly error messages for all API failures |
| NFR-06 | Session Management | Graceful handling of token expiration |

---

## 10. Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **CORS issues** | High | Medium | Configure backend CORS headers; use Vite proxy in dev |
| **JWT token exposure** | High | Low | Store in memory, not localStorage; HTTPS only in prod |
| **API contract changes** | Medium | Medium | Use TypeScript types; integration tests; OpenAPI specs |
| **State management complexity** | Medium | Low | Use TanStack Query for server state; minimal Zustand |
| **Browser compatibility** | Low | Low | Use Vite polyfills; test across browsers |
| **Performance on large datasets** | Medium | Medium | Implement pagination; virtualized tables if needed |
| **Team React experience** | Medium | Varies | Provide training; code reviews; pair programming |

---

## 11. Security Requirements

### 11.1 Authentication Security

| Requirement | Implementation |
|-------------|----------------|
| Secure token storage | In-memory primary; encrypted localStorage backup |
| Token expiration | Tokens expire after 1 hour (per ADR-001) |
| Logout | Clear all tokens from memory and storage |
| Session timeout | Redirect to login on 401 response |
| HTTPS | Required in production |

### 11.2 XSS Prevention

- React's default JSX escaping
- Avoid `dangerouslySetInnerHTML`
- Content Security Policy headers (via nginx)
- Input sanitization for user-provided content

### 11.3 CSRF Protection

- JWT tokens in Authorization header (not cookies)
- SameSite cookie policy if using cookies
- No automatic credential submission

### 11.4 Input Validation

- Client-side validation with Zod schemas
- Server-side validation as source of truth
- Display server validation errors to user

---

## 12. Implementation Notes

### 12.1 Agent Assignment

| Agent | Responsibilities |
|-------|------------------|
| **frontend-engineer** | Implement React components, pages, routing |
| **frontend-engineer** | Implement API integration, state management |
| **verifier** | Verify implementation matches this spec |
| **qa-engineer** | Write and execute test cases |

### 12.2 Implementation Order

1. **Week 1**: Project setup, auth flow, login/register pages
2. **Week 2**: Dashboard layout, navigation, market summary
3. **Week 3**: Portfolio page, holdings table, sell functionality
4. **Week 4**: Quote page, buy functionality, order confirmation
5. **Week 5**: Account page, profile management, order history
6. **Week 6**: Testing, bug fixes, polish

### 12.3 Definition of Done

- [ ] Component implemented with TypeScript
- [ ] Component tests written and passing
- [ ] Responsive design verified (mobile + desktop)
- [ ] API integration tested against backend
- [ ] Error states handled gracefully
- [ ] Loading states implemented
- [ ] Code reviewed and approved
- [ ] Accessibility checked

### 12.4 Legacy Feature Parity Checklist

| Legacy Feature | Status | Notes |
|----------------|--------|-------|
| Login/Logout | Planned | JWT-based auth |
| User Registration | Planned | Same fields as legacy |
| Trade Home | Planned | Dashboard with market summary |
| Account Info | Planned | Read-only account details |
| Profile Update | Planned | Editable profile form |
| Order History | Planned | Paginated table |
| Portfolio View | Planned | Holdings with current prices |
| Buy Stock | Planned | Quote page with buy form |
| Sell Stock | Planned | Portfolio page with sell buttons |
| Quote Lookup | Planned | Symbol search |
| Market Summary | Planned | TSIA, gainers, losers |
| Completed Orders Alert | Planned | Dismissable alert banner |
| WebSocket Updates | Deferred | Use polling initially; WebSocket optional |

---

## 13. Dependencies on Other Phases

| Phase | Dependency | Status |
|-------|------------|--------|
| Phase 1 | Account Service API | Complete |
| Phase 2 | Trading Service API | Complete |
| Phase 3 | Market Service API | Complete |
| ADR-001 | JWT Authentication | Accepted |
| ADR-002 | In-Memory Messaging | Accepted |

---

## 14. Open Questions

1. **WebSocket Support**: Should real-time market updates use WebSocket or is polling sufficient?
   - **Current Decision**: Start with TanStack Query polling (5-second intervals); WebSocket as optional enhancement

2. **Charts**: Should market data include historical charts?
   - **Current Decision**: Defer to future phase; focus on feature parity first

3. **Mobile App**: Is a React Native mobile app needed?
   - **Current Decision**: Out of scope; focus on responsive web app

---

## References

- [ADR-001: Simple JWT Authentication](./adr/ADR-001-simple-jwt-authentication.md)
- [ADR-002: In-Memory Messaging](./adr/ADR-002-in-memory-messaging.md)
- [Account API Specification](./api-spec-account.md)
- [Trading API Specification](./api-spec-trading.md)
- [Quote API Specification](./api-spec-quote.md)
- [Market API Specification](./api-spec-market.md)
- Legacy Frontend: `daytrader-ee7-web/src/main/webapp/`

---

**Document Status**: Draft
**Last Updated**: 2026-02-04
**Author**: Software Architect Agent
**Next Review**: Prior to frontend implementation kickoff
