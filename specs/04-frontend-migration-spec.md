# DayTrader Frontend Migration Specification (React SPA)

## Phase Overview: Frontend Migration (Phase 4)
- **Objectives**: Replace JSF/Servlet-based UI with a modern React + TypeScript SPA consuming Quarkus REST APIs, preserving all functional flows (auth, trading, portfolio, market) while improving UX, performance, and testability.
- **Technical Approach**: Single-page app built with React 18, Vite, React Router, React Query, React Hook Form + Zod, and Material UI (MUI) for component library; integrate with REST and WebSocket/SSE backends.
- **Dependencies**: Backend REST APIs and WebSocket endpoints from Quarkus; authentication (likely JWT-based); CI/CD and hosting environment for static assets; CORS configuration on backend.
- **Acceptance Criteria**: All existing JSF features available in SPA; key flows covered by automated tests; no critical regressions; perceived performance equal or better; accessible and responsive UI.
- **Risks & Mitigations**: Auth integration mismatch (align early with backend contract); inconsistent data models (define shared API types); real-time connection instability (central connection manager, retry policies); SEO changes (ensure relevant entry routes and deep-linking).
- **Implementation Notes**: Migrate feature-by-feature behind a reverse proxy if parallel run is needed; keep JSF and SPA auth models aligned during transition; maintain API compatibility where feasible.

## 1. Current UI Analysis

### 1.1 Existing Screens & Functions
- **TradeAppJSF**: Entry point for login, navigation shell, and high-level dashboard.
- **PortfolioJSF**: Displays current holdings, valuations, and allows buy/sell actions.
- **QuoteJSF**: Quote lookup for single/multiple symbols, with price and basic stats.
- **MarketSummaryJSF**: Market overview with top gainers/losers and key indices.
- **AccountDataJSF**: Account profile, preferences, and basic settings display/edit.
- **OrderDataJSF**: Order history, open orders, and order status.
- **TradeAppServlet/TradeScenarioServlet/TradeConfigServlet**: Handle trading actions, scenarios/load testing, and configuration operations respectively.
- **MarketSummaryWebSocket**: Pushes real-time market summary updates to UI.

### 1.2 Key User Flows
- **Login/Registration**: Anonymous user reaches login; may register; upon success redirected to dashboard/home.
- **Trading**: From portfolio or quote, user initiates buy/sell; order is validated, submitted, and confirmation/status shown.
- **Portfolio Management**: User views holdings, unrealized gains/losses, and can sell or navigate to quotes.
- **Quote Lookup**: Ad-hoc symbol search leading to quote display and trading entry points.
- **Order History**: User reviews historical orders and current open orders; may filter by status/date.
- **Account Management**: User views/edits basic profile data (name, email, contact, preferences).
- **Market Monitoring**: User tracks market summary, top movers, and possibly auto-refresh/real-time data.

### 1.3 Data Displayed per Screen (Conceptual)
- **Login/Register**: Credentials, registration details, error messages, session state.
- **Dashboard/Home**: Welcome banner, key KPIs (portfolio value, P/L, recent orders, market snapshot).
- **Portfolio**: List of holdings, quantities, cost basis, current price, market value, P/L, actions.
- **Quote Lookup**: Symbol, last price, change, volume, basic company info, trade shortcuts.
- **Trading (Buy/Sell)**: Order ticket (symbol, side, qty, price type), fees estimate, validation errors, confirmation.
- **Order History**: Table of orders with id, symbol, side, qty, status, timestamps, execution price.
- **Account Profile**: User identity data, contact info, preferences, maybe risk settings.
- **Market Summary**: Index snapshot, top gainers/losers, last update time, streaming indicators.

## 2. Technology Stack
- **UI Framework**: React 18+ with TypeScript.
- **Build Tooling**: Vite (fast dev server, optimized production builds, env handling via `import.meta.env`).
- **Styling**: Material UI (MUI) as primary component library; custom theme for DayTrader branding. Tailwind may be used selectively for utility-first layout if team prefers, but MUI components and theming are canonical.
- **HTTP Client**: Native `fetch` wrapped by a small utility plus **React Query** for data fetching, caching, and synchronization.
- **State Management**: React Query for server state; React Context + hooks for auth/session and UI preferences (theme, layout). Introduce Redux Toolkit only if future complexity requires it.
- **Routing**: React Router v6 for SPA routing, nested layouts, and protected routes.
- **Forms & Validation**: React Hook Form for form state management; Zod schemas for validation and shared typing between UI and API contracts.
- **Testing**: Vitest + React Testing Library for unit/integration; Playwright (or Cypress) for E2E flows; MSW for API mocking.

## 3. Application Structure

```text
src/
  api/           # API client wrappers, endpoint modules, shared API types
  components/    # Reusable presentational components (tables, forms, charts)
  features/      # Feature modules (domain-centric)
    auth/
    portfolio/
    trading/
    market/
    account/
    orders/
  hooks/         # Cross-cutting custom hooks (useAuth, useWebSocket, etc.)
  layouts/       # AppShell, AuthLayout, public/private layouts
  pages/         # Route-level components composing features/layouts
  store/         # Context providers and any global state helpers
  types/         # Domain and DTO TypeScript types if not colocated with api/
  utils/         # Utility functions (formatting, dates, numbers, logging)
```

- **Feature Modules** encapsulate components, hooks, and tests per domain (e.g., `features/portfolio` has list, details, hooks for fetching holdings).
- **API Layer** centralizes REST/WebSocket integration with typed contracts.
- **Layouts/Pages** map to top-level routes and orchestrate feature components.

## 4. Screen-by-Screen Migration

For each screen, we map current JSF/Servlet artifacts to React routes and feature components.

### 4.1 Login/Register
- **Current**: `TradeAppJSF` (login), registration flow possibly via separate JSF page.
- **New Route(s)**: `/login`, `/register`.
- **React Components**: `pages/LoginPage`, `pages/RegisterPage`, `features/auth/components/LoginForm`, `RegisterForm`.
- **APIs Consumed**:
  - `POST /api/auth/login` (credentials → JWT/refresh token or session id).
  - `POST /api/auth/register`.
  - Optional: `GET /api/auth/me` to fetch current user profile.
- **State Requirements**:
  - Auth context: current user, tokens, auth status, login/logout actions.
  - Error + loading state per mutation.
  - Redirect targets after login (e.g., `from` route).

### 4.2 Dashboard/Home
- **Current**: `TradeAppJSF` main screen post-login.
- **New Route**: `/` (protected), e.g., `DashboardPage`.
- **React Components**: `pages/DashboardPage`, dashboard widgets (portfolio summary, recent orders, market snapshot) under `features/portfolio`, `features/orders`, `features/market`.
- **APIs Consumed**:
  - `GET /api/portfolio/summary`.
  - `GET /api/orders/recent`.
  - `GET /api/market/summary` (initial snapshot; then real-time).
- **State Requirements**:
  - Cached queries per widget with React Query.
  - Lightweight local UI state for widget layout and filters.

### 4.3 Portfolio View
- **Current**: `PortfolioJSF` + data beans.
- **New Route**: `/portfolio` (protected).
- **React Components**: `pages/PortfolioPage`, `features/portfolio/components/PortfolioTable`, `HoldingRow`, `PortfolioStats`.
- **APIs Consumed**:
  - `GET /api/portfolio/holdings`.
  - Possibly `GET /api/portfolio/performance`.
- **State Requirements**:
  - React Query cache of holdings with refetch on trade completion.
  - Local selection/sorting/filter state.
  - Integration with trading feature (open buy/sell ticket pre-filled from holding).

### 4.4 Quote Lookup
- **Current**: `QuoteJSF`.
- **New Route**: `/quotes` with optional symbol query param (e.g., `/quotes?symbol=IBM`).
- **React Components**: `pages/QuotePage`, `features/market/components/QuoteSearchForm`, `QuoteDetailsCard`.
- **APIs Consumed**:
  - `GET /api/market/quote?symbol={symbol}`.
  - Optional batch endpoint `GET /api/market/quotes?symbols=...`.
- **State Requirements**:
  - React Query for quote data keyed by symbol.
  - Form state for symbol lookup via React Hook Form + Zod.
  - Integration with trading ticket ("Buy" / "Sell" from quote view).

### 4.5 Trading (Buy/Sell)
- **Current**: `TradeAppServlet` actions from portfolio/quote screens.
- **New Route(s)**: `/trade/buy`, `/trade/sell` (with query params `symbol`, `quantity`), or modal-based ticket within portfolio/quote pages.
- **React Components**: `features/trading/components/TradeTicket`, `TradeConfirmation`, `OrderSummary`.
- **APIs Consumed**:
  - `POST /api/trades/buy`.
  - `POST /api/trades/sell`.
  - Optional `GET /api/trades/preview` for fees/estimates.
- **State Requirements**:
  - React Hook Form + Zod for ticket validation (symbol, qty, side, type).
  - Mutation state for submission and confirmation.
  - Side-effects: invalidate/refetch portfolio, orders, and quotes upon success.

### 4.6 Order History
- **Current**: `OrderDataJSF`.
- **New Route**: `/orders` (protected).
- **React Components**: `pages/OrdersPage`, `features/orders/components/OrdersTable`, `OrderFilterBar`, `OrderStatusTag`.
- **APIs Consumed**:
  - `GET /api/orders?status=&from=&to=` with pagination/sorting.
- **State Requirements**:
  - React Query paginated query for orders.
  - Local filter and pagination state synchronized with URL query params.
  - Optional WebSocket/SSE updates for status changes.

### 4.7 Account Profile
- **Current**: `AccountDataJSF`.
- **New Route**: `/account` (protected).
- **React Components**: `pages/AccountPage`, `features/account/components/AccountProfileForm`, `PreferenceToggles`.
- **APIs Consumed**:
  - `GET /api/account/profile`.
  - `PUT /api/account/profile`.
  - Possibly `GET/PUT /api/account/preferences`.
- **State Requirements**:
  - React Query for profile and preferences queries.
  - Form state with optimistic UI where appropriate.

### 4.8 Market Summary
- **Current**: `MarketSummaryJSF` + `MarketSummaryWebSocket`.
- **New Route**: `/market` (protected), plus widgets embedded on dashboard.
- **React Components**: `pages/MarketSummaryPage`, `features/market/components/MarketSummaryGrid`, `TopMoversTable`, `MarketTicker`.
- **APIs Consumed**:
  - `GET /api/market/summary` (initial load).
  - WebSocket/SSE endpoint, e.g., `ws(s)://.../ws/market-summary` or `/api/market/summary/stream`.
- **State Requirements**:
  - React Query for initial snapshot.
  - Real-time subscription state integrated into market and dashboard components.
  - Visual indicators for connection status and last update time.

## 5. API Integration
- **API Client Design**: Central `api/client.ts` exposing typed `request` helpers and feature-specific modules (e.g., `api/auth`, `api/portfolio`).
- **TypeScript Types**: DTOs for user, holding, quote, order, summary defined in `api/types` or colocated with endpoint modules; keep aligned with backend via shared schema where feasible.
- **Authentication Handling**: Attach JWT access token to `Authorization` header in API client; manage refresh tokens via secure HTTP-only cookies or dedicated refresh endpoint.
- **Error Handling**: Normalize errors (network, validation, server) to a common structure; surface via toast notifications and inline form messages.
- **Loading/Error States**: Standard patterns using React Query status flags; skeleton loaders for tables/cards; centralized error boundary for fatal failures.
- **Retries & Caching**: Configure React Query retry/backoff for idempotent GETs; define cache times per resource type (e.g., quotes short-lived, histories longer).

## 6. Real-time Features
- **Connection Management**: Implement `useWebSocket`/`useMarketStream` hooks under `hooks/` to manage a single shared WebSocket/SSE connection per user session.
- **Market Data Updates**: Stream market summary and top movers; update React Query caches via `queryClient.setQueryData` or local state when messages arrive.
- **Order Status Updates**: Optionally subscribe to order status events; update open orders in `orders` queries and show toast notifications.
- **Reconnection Handling**: Exponential backoff reconnection strategy, max attempts, and explicit UI indication when live data is stale.
- **Graceful Degradation**: Fallback to periodic polling when WebSocket/SSE is unavailable or fails consistently.

## 7. Authentication & Security
- **Token Storage**: Prefer in-memory storage for access tokens with refresh token in secure HTTP-only cookie; avoid long-lived tokens in `localStorage`.
- **Refresh Handling**: Transparent token refresh in API client; queue requests during refresh to avoid token race conditions.
- **Protected Routes**: React Router v6 protected route wrapper using `useAuth` hook; redirects unauthenticated users to `/login` with `from` state.
- **Session Management**: Auto-logout on refresh/token failure; idle timeout and optional warning modal.
- **Security Headers & CSRF**: Rely on backend CSRF protections for state-changing endpoints; ensure CORS and headers are configured correctly for SPA origin.

## 8. Build & Deployment
- **Vite Configuration**: Base config with React plugin, TypeScript path aliases, dev proxy to backend (e.g., `/api` → Quarkus host) to avoid CORS in dev.
- **Environment Configuration**: Use `VITE_API_BASE_URL`, `VITE_WS_BASE_URL`, and feature flags for enabling real-time features.
- **Production Build**: `vite build` producing static assets; integrate into CI pipeline with linting, testing, and bundle analysis as needed.
- **Static Hosting**: Serve built assets via Nginx, CDN, or Quarkus static resources; configure SPA fallback (`index.html`) for all app routes.
- **CORS Considerations**: Backend must allow SPA origin, credentials (if cookies used), and necessary headers/methods; keep environments (dev/stage/prod) configurable.

## 9. Testing Strategy
- **Component Testing**: Use Vitest + React Testing Library for isolated components (forms, tables, widgets) with state and interaction checks.
- **Integration Testing**: Test feature flows within React (e.g., submit trade ticket updates portfolio) using MSW to mock API responses and WebSocket messages where appropriate.
- **E2E Testing**: Playwright or Cypress tests for critical flows: login, place trade, view portfolio, view orders, and market summary.
- **Mock API Patterns**: Centralized MSW handlers mirroring `api/` modules; reusable fixtures for typical domain objects (holdings, quotes, orders).

## 10. UI/UX Improvements
- **Responsive Design**: Mobile-first layout with breakpoints suitable for dashboards and tables; horizontal scrolling or stacked cards for small screens.
- **Consistency & Theming**: Single MUI theme (colors, typography, spacing) reflecting DayTrader brand; shared design tokens for spacing and elevation.
- **Accessibility (a11y)**: Semantic HTML for tables/forms, ARIA labels for interactive controls, focus management for modals and dialogs, contrast-compliant palette.
- **Dark Mode Support**: Theme toggle persisted in user preferences; MUI theme variants for light/dark with minimal overrides.
- **Usability Enhancements**: Clear error messaging, optimistic updates where safe (e.g., profile edits), and progress indicators for long-running operations.

