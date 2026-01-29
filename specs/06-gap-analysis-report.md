# DayTrader Migration Gap Analysis Report

**Document Version:** 1.0  
**Date:** January 28, 2026  
**Phase:** Phase 1 Architecture Review  

---

## Executive Summary

This document provides a comprehensive gap analysis between the legacy Java EE 7 DayTrader application and the current Quarkus implementation. The analysis reveals **significant gaps** in the migration, with approximately **68% of core business operations** not yet implemented.

### Key Findings

| Metric | Legacy | Quarkus | Coverage |
|--------|--------|---------|----------|
| TradeService Methods | 25+ | 8 | **32%** |
| REST Endpoints | 12+ | 4 | **33%** |
| Messaging Components | 3 | 0 | **0%** |
| Real-time Features | 1 | 0 | **0%** |

### Migration Status: 🟡 PARTIAL

The current implementation covers foundational components but lacks critical trading operations (buy/sell), portfolio management, order processing, and real-time market updates.

---

## 1. Legacy Component Inventory

### 1.1 EJB Components

| Component | Type | Description | Status |
|-----------|------|-------------|--------|
| `TradeSLSBBean` | @Stateless | Main trading facade (25+ methods) | 🟡 Partial |
| `DTBroker3MDB` | @MessageDriven | Order processing queue consumer | 🔴 Missing |
| `DTStreamer3MDB` | @MessageDriven | Market data streaming | 🔴 Missing |
| `MarketSummarySingleton` | @Singleton | Cached market summary | 🔴 Missing |

### 1.2 Web Components

| Component | Type | Description | Status |
|-----------|------|-------------|--------|
| `TradeServletAction` | Servlet | 12 action handlers | 🟡 Partial |
| `MarketSummaryWebSocket` | WebSocket | Real-time market updates | 🔴 Missing |
| `TradeAppServlet` | Servlet | Main application servlet | 🔴 Missing |
| `TradeScenarioServlet` | Servlet | Test scenario runner | 🔴 Missing |
| `TradeConfigServlet` | Servlet | Configuration management | 🔴 Missing |

### 1.3 JMS Messaging

| Component | Type | Description | Status |
|-----------|------|-------------|--------|
| `TradeBrokerQueue` | Queue | Async order processing | 🔴 Missing |
| `TradeStreamerTopic` | Topic | Quote price change pub/sub | 🔴 Missing |

---

## 2. TradeService Method Gap Analysis

### 2.1 Implemented Methods ✅

| Method | Description | Quarkus Status |
|--------|-------------|----------------|
| `login(userID, password)` | Authenticate user | ✅ Implemented |
| `logout(userID)` | Logout user | ✅ Implemented |
| `getAccountData(accountID)` | Get account info | ✅ Implemented |
| `getQuote(symbol)` | Get stock quote | ✅ Implemented |
| `getAllQuotes()` | Get all quotes | ✅ Implemented |
| `createQuote(symbol, name, price)` | Create quote | ✅ Implemented |
| `updateQuotePrice(symbol, price)` | Update price | ✅ Implemented |
| `register(...)` | Register new user | ✅ Implemented |

### 2.2 Missing Methods 🔴

| Method | Priority | Description |
|--------|----------|-------------|
| `buy(userID, symbol, quantity, mode)` | **CRITICAL** | Buy stock shares |
| `sell(userID, holdingID, mode)` | **CRITICAL** | Sell holding |
| `getMarketSummary()` | **HIGH** | Market summary data |
| `getOrders(userID)` | **HIGH** | User's orders |
| `getClosedOrders(userID)` | **HIGH** | Completed orders |
| `getHoldings(userID)` | **HIGH** | User's portfolio |
| `getHolding(holdingID)` | **MEDIUM** | Single holding |
| `getAccountProfileData(userID)` | **MEDIUM** | User profile |
| `updateAccountProfile(profile)` | **MEDIUM** | Update profile |
| `completeOrder(orderID)` | **HIGH** | Complete order |
| `cancelOrder(orderID)` | **MEDIUM** | Cancel order |
| `queueOrder(orderID)` | **HIGH** | Queue for async processing |
| `updateQuotePriceVolume(...)` | **MEDIUM** | Update with volume |
| `publishQuotePriceChange(...)` | **MEDIUM** | Publish price change |
| `resetTrade(deleteAll)` | **LOW** | Reset trade data |

---

## 3. REST API Gap Analysis

### 3.1 Implemented Endpoints ✅

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/quotes` | GET | Get all quotes |
| `/api/v1/quotes/{symbol}` | GET | Get quote by symbol |
| `/api/v1/accounts/{id}` | GET | Get account by ID |
| `/api/v1/accounts` | POST | Register account |

### 3.2 Missing Endpoints 🔴

Per `specs/03-backend-migration-spec.md` Section 4:

| Resource | Endpoint | Priority | Description |
|----------|----------|----------|-------------|
| **AuthResource** | `POST /api/v1/auth/login` | **CRITICAL** | User login |
| | `POST /api/v1/auth/logout` | **CRITICAL** | User logout |
| **OrderResource** | `GET /api/v1/orders` | **HIGH** | User's orders |
| | `POST /api/v1/orders/buy` | **CRITICAL** | Buy stock |
| | `POST /api/v1/orders/sell` | **CRITICAL** | Sell holding |
| **PortfolioResource** | `GET /api/v1/portfolio` | **HIGH** | User holdings |
| | `GET /api/v1/portfolio/{id}` | **MEDIUM** | Single holding |
| **MarketResource** | `GET /api/v1/market/summary` | **HIGH** | Market summary |
| **AccountResource** | `GET /api/v1/accounts/profile` | **MEDIUM** | User profile |
| | `PUT /api/v1/accounts/profile` | **MEDIUM** | Update profile |
| **ScenarioResource** | `POST /api/v1/scenarios/populate` | **LOW** | Populate DB |
| | `POST /api/v1/scenarios/reset` | **LOW** | Reset data |

---

## 4. Messaging Infrastructure Gap

### 4.1 Legacy JMS Architecture

```
┌─────────────────┐      ┌────────────────────┐      ┌─────────────────┐
│  TradeSLSBBean  │─────▶│  TradeBrokerQueue  │─────▶│  DTBroker3MDB   │
│  queueOrder()   │      │  (JMS Queue)       │      │  onMessage()    │
└─────────────────┘      └────────────────────┘      └─────────────────┘
                                                            │
                                                            ▼
                                                     completeOrder()
```

### 4.2 Required Quarkus Implementation

Per `specs/03-backend-migration-spec.md` Section 7:

| Legacy Component | Quarkus Replacement | Status |
|-----------------|---------------------|--------|
| `TradeBrokerQueue` | `orders-in` channel (Kafka/AMQP) | 🔴 Missing |
| `DTBroker3MDB` | `OrderProcessorService` with `@Incoming` | 🔴 Missing |
| `TradeStreamerTopic` | `market-events` channel | 🔴 Missing |
| `DTStreamer3MDB` | `MarketDataStreamer` with `@Incoming` | 🔴 Missing |

---

## 5. Real-time Features Gap

### 5.1 Legacy WebSocket Architecture

The legacy `MarketSummaryWebSocket` provides:
- Real-time market summary updates
- Stock price change notifications
- Scheduled updates every 2 seconds
- CDI event integration with JMS messages

### 5.2 Required Quarkus Implementation

| Feature | Implementation | Status |
|---------|---------------|--------|
| WebSocket endpoint `/marketsummary` | Quarkus WebSocket | 🔴 Missing |
| SSE endpoint for market updates | RESTEasy SSE | 🔴 Missing |
| `MarketSummaryService` (CDI singleton) | `@ApplicationScoped` | 🔴 Missing |
| Scheduled cache refresh | `@Scheduled` | 🔴 Missing |

---

## 6. Recommendations

### 6.1 Priority 1: Core Trading Operations (Week 1-2)

1. **Implement TradeService methods:**
   - `buy()`, `sell()`, `completeOrder()`, `queueOrder()`
   - `getHoldings()`, `getHolding()`, `getOrders()`, `getClosedOrders()`

2. **Create REST resources:**
   - `AuthResource` - `/api/v1/auth/login`, `/api/v1/auth/logout`
   - `OrderResource` - `/api/v1/orders`, buy/sell endpoints
   - `PortfolioResource` - `/api/v1/portfolio`

### 6.2 Priority 2: Market Summary & Profiles (Week 2-3)

1. **Implement market summary:**
   - `MarketSummaryService` CDI singleton
   - `MarketSummaryDataBean` DTO
   - `getMarketSummary()` in TradeService
   - `MarketResource` REST endpoint

2. **Implement profile management:**
   - `getAccountProfileData()`, `updateAccountProfile()`
   - Profile endpoints in AccountResource

### 6.3 Priority 3: Messaging & Real-time (Week 3-4)

1. **Add SmallRye Reactive Messaging:**
   - `OrderProcessorService` with `@Incoming("orders-in")`
   - `MarketDataStreamer` with `@Incoming("market-events")`

2. **Implement real-time features:**
   - WebSocket endpoint for market summary
   - SSE endpoint for price updates

---

## 7. Updated Roadmap

| Phase | Duration | Components | Status |
|-------|----------|------------|--------|
| Phase 2A | 2 weeks | Core Trading (buy/sell/orders) | ⏳ Next |
| Phase 2B | 1 week | Portfolio & Holdings | ⏳ Pending |
| Phase 3A | 1 week | Market Summary & Profiles | ⏳ Pending |
| Phase 3B | 1 week | Messaging Infrastructure | ⏳ Pending |
| Phase 4 | 1 week | Real-time (WebSocket/SSE) | ⏳ Pending |

**Total Estimated Additional Effort:** 6 weeks

---

## 8. Appendix: File References

### Legacy Files Analyzed

- [`TradeSLSBBean.java`](../daytrader-ee7-ejb/src/main/java/com/ibm/websphere/samples/daytrader/ejb3/TradeSLSBBean.java)
- [`DTBroker3MDB.java`](../daytrader-ee7-ejb/src/main/java/com/ibm/websphere/samples/daytrader/ejb3/DTBroker3MDB.java)
- [`MarketSummarySingleton.java`](../daytrader-ee7-ejb/src/main/java/com/ibm/websphere/samples/daytrader/ejb3/MarketSummarySingleton.java)
- [`TradeServletAction.java`](../daytrader-ee7-web/src/main/java/com/ibm/websphere/samples/daytrader/web/TradeServletAction.java)
- [`MarketSummaryWebSocket.java`](../daytrader-ee7-web/src/main/java/com/ibm/websphere/samples/daytrader/web/websocket/MarketSummaryWebSocket.java)

### Current Quarkus Files

- [`TradeService.java`](../daytrader-quarkus/src/main/java/com/ibm/websphere/samples/daytrader/service/TradeService.java)
- [`QuoteResource.java`](../daytrader-quarkus/src/main/java/com/ibm/websphere/samples/daytrader/rest/QuoteResource.java)
- [`AccountResource.java`](../daytrader-quarkus/src/main/java/com/ibm/websphere/samples/daytrader/rest/AccountResource.java)

