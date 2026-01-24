# DayTrader Modernization - Original Specification

> This document captures the original specification that initiated the DayTrader modernization project from Java EE7/WebSphere to Quarkus + React.

## Project Goal

Modernize the IBM DayTrader7 legacy Java EE7 stock trading application to a modern architecture with:
- **Frontend**: React with TypeScript (modern SPA)
- **Backend**: Quarkus.io (cloud-native Java)
- **Database**: PostgreSQL (production-ready, replaces Derby)

## Legacy Codebase Analysis

### Entity Model (5 JPA Entities)

| Entity | Table | Key Fields | Relationships |
|--------|-------|------------|---------------|
| AccountDataBean | ACCOUNTEJB | accountID, balance, openBalance, loginCount | 1:N Orders, 1:N Holdings, 1:1 Profile |
| AccountProfileDataBean | ACCOUNTPROFILEEJB | userID, password, fullName, email | 1:1 Account |
| HoldingDataBean | HOLDINGEJB | holdingID, quantity, purchasePrice, purchaseDate | N:1 Account, N:1 Quote |
| OrderDataBean | ORDEREJB | orderID, orderType, orderStatus, quantity, price | N:1 Account, N:1 Quote, N:1 Holding |
| QuoteDataBean | QUOTEEJB | symbol, companyName, price, volume, change | 1:N Holdings, 1:N Orders |

### TradeServices Interface (Core Business Operations)

| Category | Methods |
|----------|---------|
| Authentication | login(), logout(), register() |
| Account | getAccountData(), getAccountProfileData(), updateAccountProfile() |
| Trading | buy(), sell(), getOrders(), getClosedOrders(), getHoldings(), getHolding() |
| Market | getQuote(), getAllQuotes(), getMarketSummary(), createQuote(), updateQuotePriceVolume() |
| Admin | resetTrade() |

### Legacy UI Pages (JSP/JSF)

- Login/Registration: `welcome.jsp`, `register.jsp`
- Dashboard: `tradehome.jsp`, `marketSummary.jsp`
- Portfolio: `portfolio.jsp`, `order.jsp`
- Quotes: `quote.jsp`, `displayQuote.jsp`
- Account: `account.jsp`, `config.jsp`

### Legacy Technology Stack

| Layer | Technology | Location |
|-------|------------|----------|
| Web | JSP/JSF + Servlets | daytrader-ee7-web/ |
| Business | EJB3 Stateless Beans | daytrader-ee7-ejb/ |
| Data | JPA 2.1 Entities | 5 entities |
| Database | Derby/DB2 | Via JDBC DataSource |
| Runtime | WebSphere Liberty | Java EE7 |

## Phased Implementation Plan

### Phase 1: Foundation (Backend Infrastructure)
1. Create Quarkus Backend Project Structure
2. Migrate JPA Entities to Quarkus/Panache

### Phase 2: Core Services & REST API
1. Implement Trade Services Layer
2. Create REST API Endpoints

### Phase 3: React Frontend
1. Initialize React Frontend Project
2. Build Core Frontend Components
3. Implement Frontend Pages and State Management

### Phase 4: Integration & Testing
1. Configure CORS and Authentication
2. Database Migration and Seeding

## Target Technology Stack

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

## Success Criteria

1. All core trading functionality working (login, buy, sell, portfolio, quotes)
2. React frontend communicating with Quarkus backend via REST API
3. JWT-based authentication securing API endpoints
4. PostgreSQL database with migrated schema
5. Docker-compose for local development setup

---

*Document created: January 2026*
*Modernization Status: ✅ Complete*
