# DayTrader Modernization - Original Specification

> **Note**: This is a copy of the original specification that initiated the DayTrader modernization project from Java EE7/WebSphere to Quarkus + React.

---

## Overview

Modernize the IBM DayTrader7 legacy Java EE7 stock trading application to a modern architecture with:

- **Frontend**: React with TypeScript (modern SPA)
- **Backend**: Quarkus.io (cloud-native Java)
- **Database**: PostgreSQL (production-ready, replaces Derby)

---

## Legacy Architecture Analysis

### Original Technology Stack

| Layer | Technology | Location |
|-------|------------|----------|
| **Web** | JSP/JSF 2.2 + Servlets | `daytrader-ee7-web/` |
| **Business** | EJB3 Stateless Beans | `daytrader-ee7-ejb/` |
| **Data** | JPA 2.1 Entities | 5 core entities |
| **Server** | WebSphere Liberty | `daytrader-ee7/` |
| **Database** | Derby/DB2 | JDBC DataSource |

### Domain Model (5 Entities)

1. **AccountDataBean** - User accounts with balance and profile
2. **AccountProfileDataBean** - User profile with credentials
3. **HoldingDataBean** - Stock holdings owned by accounts
4. **OrderDataBean** - Buy/sell orders with status tracking
5. **QuoteDataBean** - Stock quotes with price and volume

### TradeServices Interface (Core Business Operations)

| Category | Methods |
|----------|---------|
| Authentication | `login()`, `logout()`, `register()` |
| Account | `getAccountData()`, `getAccountProfileData()`, `updateAccountProfile()` |
| Trading | `buy()`, `sell()`, `getOrders()`, `getClosedOrders()`, `getHoldings()`, `getHolding()` |
| Market | `getQuote()`, `getAllQuotes()`, `getMarketSummary()`, `createQuote()`, `updateQuotePriceVolume()` |
| Admin | `resetTrade()` |

### Legacy UI Pages (JSP/JSF)

- Login/Registration: `welcome.jsp`, `register.jsp`
- Dashboard: `tradehome.jsp`, `marketSummary.jsp`
- Portfolio: `portfolio.jsp`, `order.jsp`
- Quotes: `quote.jsp`, `displayQuote.jsp`
- Account: `account.jsp`, `config.jsp`

---

## Target Architecture

### New Technology Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 18 + TypeScript + Vite + TailwindCSS |
| **Backend** | Quarkus 3.x with RESTEasy Reactive |
| **Data** | Hibernate ORM with Panache |
| **Database** | PostgreSQL |
| **Security** | SmallRye JWT (JSON Web Tokens) |
| **API Docs** | OpenAPI/Swagger |

---

## Modernization Phases

### Phase 1: Foundation
- Create Quarkus 3.x project structure with required extensions
- Migrate 5 JPA entities to Panache pattern with jakarta.persistence

### Phase 2: Core Services
- Implement CDI service layer (AuthService, AccountService, TradeService, MarketService)
- Create REST API endpoints with OpenAPI documentation

### Phase 3: React Frontend
- Initialize Vite + React + TypeScript project
- Build reusable UI components
- Implement pages with React Query for data fetching

### Phase 4: Integration
- Configure CORS for frontend-backend communication
- Implement JWT authentication flow
- Set up Flyway database migrations with PostgreSQL

---

## Success Criteria

1. ✅ All 5 entities migrated with Panache finder methods
2. ✅ All trading operations functional via REST API
3. ✅ React frontend with all 8 pages working
4. ✅ JWT authentication securing all protected endpoints
5. ✅ Database migrations with sample data
6. ✅ Project compiles and runs in development mode

---

## Technology Stack Details

### Backend (Quarkus)
- **Runtime**: Quarkus 3.x (Jakarta EE 10)
- **REST**: RESTEasy Reactive + Jackson
- **ORM**: Hibernate ORM with Panache
- **Database**: PostgreSQL + Flyway migrations
- **Security**: SmallRye JWT
- **Build**: Maven

### Frontend (React)
- **Framework**: React 18+ with TypeScript
- **Build**: Vite
- **Styling**: TailwindCSS
- **Routing**: React Router v6
- **State**: React Query (TanStack Query)
- **HTTP Client**: Axios

### Development
- **Containers**: Docker + docker-compose
- **API Docs**: OpenAPI/Swagger UI

---

*Original specification created: January 2026*
