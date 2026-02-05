# Phase 0: DayTrader Current Architecture Assessment

## Executive Summary

DayTrader is a **Java EE7 benchmark application** demonstrating stock trading functionality. It runs on WebSphere Liberty and uses a classic 3-tier architecture with EJB3, JPA, JSF/JSP, and JMS. This assessment identifies modernization opportunities and technical debt.

---

## 1. Current Technology Stack

### Runtime Environment
| Component | Current Technology | Version |
|-----------|-------------------|---------|
| Application Server | WebSphere Liberty (Open Liberty) | Full profile |
| Java Version | Java 8 | 1.8 |
| Build Tool | Maven | 3.x |
| Packaging | EAR (Enterprise Archive) | EE7 |

### Core Frameworks
| Layer | Technology | Notes |
|-------|------------|-------|
| **Business Logic** | EJB 3.2 (Session Beans, MDBs) | Stateless Session Beans, Singleton |
| **Persistence** | JPA 2.1 | JTA transactions |
| **Web Tier** | Servlets 3.1, JSF 2.2, JSP | Dual presentation layer |
| **Messaging** | JMS 2.0 | Queue-based async processing |
| **CDI** | CDI 1.2 | Dependency injection |
| **WebSocket** | JSR 356 | Real-time market updates |

### Database Support
- **Primary**: DB2 (production)
- **Development**: Apache Derby (embedded)
- **Connection Pool**: 100 connections (min/max)

---

## 2. Application Architecture

### Module Structure
```
daytrader7 (parent pom)
├── daytrader-ee7-ejb      # Business logic, entities, EJBs (JAR)
├── daytrader-ee7-web      # Web tier, servlets, JSF (WAR)
└── daytrader-ee7          # EAR packaging with Liberty config
```

### Bounded Contexts (Domain Analysis)

```
┌─────────────────────────────────────────────────────────────────┐
│                         DayTrader Domain                        │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   ACCOUNT       │    TRADING      │       MARKET                │
├─────────────────┼─────────────────┼─────────────────────────────┤
│ • AccountData   │ • OrderData     │ • QuoteData                 │
│ • AccountProfile│ • HoldingData   │ • MarketSummary             │
│ • Login/Logout  │ • Buy/Sell      │ • Price Updates             │
│ • Registration  │ • Portfolio     │ • Top Gainers/Losers        │
└─────────────────┴─────────────────┴─────────────────────────────┘
```

### Data Model (JPA Entities)

| Entity | Table | Primary Key | Description |
|--------|-------|-------------|-------------|
| `AccountDataBean` | `accountejb` | `ACCOUNTID` (generated) | User trading account |
| `AccountProfileDataBean` | `accountprofileejb` | `USERID` | User profile data |
| `QuoteDataBean` | `quoteejb` | `SYMBOL` | Stock quote information |
| `OrderDataBean` | `orderejb` | `ORDERID` (generated) | Buy/sell orders |
| `HoldingDataBean` | `holdingejb` | `HOLDINGID` (generated) | Stock holdings |
| `KEYGENEJB` | `keygenejb` | `KEYNAME` | Sequence key generator |

### Entity Relationships
```
AccountProfile 1──1 Account 1──* Order *──1 Quote
                         │
                         └──* Holding *──1 Quote
```

---

## 3. Key Components Analysis

### Business Services Layer

#### TradeServices Interface
Central business interface implemented by two strategies:
- **TradeSLSBBean** (EJB3 mode): Uses JPA EntityManager, container-managed transactions
- **TradeDirect** (JDBC mode): Raw JDBC operations, manual transaction management

| Operation | Description |
|-----------|-------------|
| `login(userId, password)` | Authenticate user |
| `logout(userId)` | End session |
| `buy(userId, symbol, quantity)` | Execute buy order |
| `sell(userId, holdingId)` | Execute sell order |
| `getQuote(symbol)` | Retrieve stock quote |
| `getHoldings(userId)` | Get user's portfolio |
| `getMarketSummary()` | Market overview data |
| `register(...)` | New user registration |

#### EJB Components
| EJB | Type | Purpose |
|-----|------|---------|
| `TradeSLSBBean` | Stateless Session Bean | Core trading operations |
| `MarketSummarySingleton` | Singleton Bean | Cached market data (20s refresh) |
| `DTBroker3MDB` | Message-Driven Bean | Async order processing |
| `DTStreamer3MDB` | Message-Driven Bean | Quote price streaming |

### Messaging Architecture

```
┌──────────────┐    JMS Queue     ┌──────────────┐
│  Trade       │ ───────────────► │ DTBroker3MDB │ → Order Completion
│  Operations  │                  └──────────────┘
└──────────────┘

┌──────────────┐    JMS Topic     ┌───────────────┐    CDI Event    ┌────────────┐
│ Quote Update │ ───────────────► │ DTStreamer3MDB│ ──────────────► │ WebSocket  │
└──────────────┘                  └───────────────┘                 │ Clients    │
                                                                    └────────────┘
```

### Web Tier Components

| Component Type | Technology | Files |
|---------------|------------|-------|
| Entry Servlet | `TradeAppServlet` | Handles `/app` actions |
| JSF Managed Beans | CDI `@Named` beans | `TradeAppJSF`, `PortfolioJSF`, `QuoteJSF`, etc. |
| JSP Pages | Classic JSP with JSTL | `tradehome.jsp`, `quote.jsp`, etc. |
| JSF/Facelets | `.xhtml` views | `tradehome.xhtml`, `account.xhtml`, etc. |
| WebSocket | `MarketSummaryWebSocket` | Real-time market updates |

### Security Configuration

- **Authentication**: Container-managed (Liberty)
- **Authorization**: Role-based (grp1-grp5)
- **Protected URLs**: `/app/*`, `/TradeAppServlet/*`, etc.
- **Session Timeout**: 30 minutes

---

## 4. Strengths & What's Working Well

| Aspect | Strength |
|--------|----------|
| **Domain Logic** | Well-defined `TradeServices` interface |
| **Entity Model** | Clean JPA entities with proper relationships |
| **Dual Runtime Modes** | EJB3 and JDBC modes provide flexibility |
| **Async Processing** | JMS-based async order completion |
| **Real-time Updates** | WebSocket integration for market data |
| **Containerization Ready** | Existing Dockerfile and Kubernetes manifests |
| **Configuration** | Externalized via Liberty server.xml |


---

## 5. Technical Debt & Modernization Needs

### Critical Issues

| Issue | Impact | Location |
|-------|--------|----------|
| **Java 8** | EOL, security vulnerabilities | `pom.xml` (all modules) |
| **Java EE 7 APIs** | Outdated, no Jakarta EE 10 compatibility | All modules |
| **EJB Dependency** | Heavyweight, limits deployment options | `TradeSLSBBean`, MDBs |
| **Monolithic EAR** | Single deployment unit, no independent scaling | `daytrader-ee7` |
| **JMS Dependency** | Requires application server JMS provider | Order processing |
| **JSF 2.2** | Outdated, poor developer experience | Web module |
| **Dual Presentation** | JSP + JSF duplication, maintenance burden | Web module |

### Architectural Concerns

| Concern | Description |
|---------|-------------|
| **Tight Coupling** | Web tier directly depends on EJB module |
| **No API Layer** | No RESTful API for modern clients |
| **Stateful Session** | Heavy server-side session state |
| **Database Coupling** | Direct JDBC in `TradeDirect` with SQL strings |
| **No Circuit Breakers** | No resilience patterns |
| **Limited Observability** | Custom logging only, no metrics/tracing |

### Code Quality Issues

| Issue | Files Affected |
|-------|---------------|
| Raw JDBC with inline SQL | `TradeDirect.java` (2000+ lines) |
| Exception handling swallows errors | Multiple EJBs |
| Thread.sleep() in async processing | `CompleteOrderThread.java` |
| Deprecated APIs usage | Integer constructors, etc. |

---

## 6. Modernization Recommendations Summary

### Target Architecture: Cloud-Native Microservices

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API Gateway                                    │
│                         (Kong / Envoy / Istio)                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│   Account     │         │   Trading     │         │    Market     │
│   Service     │         │   Service     │         │   Service     │
│   (Quarkus)   │         │   (Quarkus)   │         │   (Quarkus)   │
└───────────────┘         └───────────────┘         └───────────────┘
        │                         │                         │
        └─────────────────────────┼─────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
            ┌───────────────┐         ┌───────────────┐
            │  PostgreSQL   │         │    Kafka      │
            │  (Primary DB) │         │  (Messaging)  │
            └───────────────┘         └───────────────┘
```

### Technology Mapping

| Current | Target | Rationale |
|---------|--------|-----------|
| Java 8 | Java 21 (LTS) | Modern features, performance |
| Java EE 7 | Jakarta EE 10 / Quarkus | Cloud-native, standards-based |
| EJB 3.2 | CDI + Panache | Lightweight, testable |
| JPA 2.1 | Hibernate ORM with Panache | Simplified repository pattern |
| JMS 2.0 | Smallrye Reactive Messaging + Kafka | Event-driven, scalable |
| JSF/JSP | React or Vue.js SPA | Modern UX, decoupled |
| WebSocket | Quarkus WebSocket + SSE | Native support |
| Liberty | Quarkus | Fast startup, low memory |
| EAR | Container images per service | Independent deployment |
| DB2/Derby | PostgreSQL | Open source, cloud-ready |

---

## 7. Next Steps

1. **Review this assessment** with stakeholders
2. **Proceed to Phase 1** specification upon approval
3. **Create API specifications** for each bounded context
4. **Define data migration strategy**

---
*Document Version: 1.0 | Created: 2026-01-31 | Status: Complete*

