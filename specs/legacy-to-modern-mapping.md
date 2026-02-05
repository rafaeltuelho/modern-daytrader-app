# Legacy to Modern Technology Mapping

## Overview

This document maps legacy Java EE7 DayTrader components to their modern Quarkus equivalents. Use this as a reference during implementation.

---

## 1. Framework & Runtime Mapping

| Legacy (Java EE7 / Liberty) | Modern (Quarkus / Jakarta EE 10) | Notes |
|-----------------------------|----------------------------------|-------|
| WebSphere Liberty | Quarkus 3.x | Cloud-native, fast startup |
| EAR packaging | Container images | One image per service |
| Java 8 | Java 21 LTS | Records, pattern matching, virtual threads |
| `javax.*` packages | `jakarta.*` packages | Namespace migration |
| `javaee-api:7.0` | Quarkus BOM | Curated dependencies |

---

## 2. EJB to CDI Mapping

### Session Beans

| Legacy EJB | Modern CDI | Example |
|------------|------------|---------|
| `@Stateless` | `@ApplicationScoped` | Most services |
| `@Singleton` | `@ApplicationScoped` | Use `@Startup` if needed |
| `@Stateful` | `@SessionScoped` (avoid) | Prefer stateless design |
| `@Local` | Interface injection | CDI handles automatically |
| `@Remote` | REST/gRPC API | Network boundary |
| `@EJB` injection | `@Inject` | Standard CDI |

### Legacy EJB Example → Modern CDI

**Before (EJB):**
```java
@Stateless
public class TradeSLSBBean implements TradeSLSBLocal {
    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SessionContext sessionContext;
}
```

**After (CDI + Panache):**
```java
@ApplicationScoped
@Transactional
public class TradingService {
    // Panache repositories are injected
    @Inject
    AccountRepository accountRepository;

    @Inject
    SecurityIdentity securityIdentity; // Replaces SessionContext
}
```

### Message-Driven Beans

| Legacy MDB | Modern Reactive Messaging | Notes |
|------------|---------------------------|-------|
| `@MessageDriven` | `@Incoming("channel")` | Smallrye Reactive Messaging |
| JMS `Queue` | Kafka topic | Or in-memory for testing |
| `onMessage(Message)` | Method with payload parameter | Type-safe |

**Before (MDB):**
```java
@MessageDriven(activationConfig = {...})
public class DTBroker3MDB implements MessageListener {
    @Override
    public void onMessage(Message message) {
        Integer orderID = message.getIntProperty("orderID");
        // process order
    }
}
```

**After (Reactive Messaging):**
```java
@ApplicationScoped
public class OrderProcessor {
    @Incoming("orders")
    @Transactional
    public void processOrder(OrderEvent event) {
        tradingService.completeOrder(event.orderId());
    }
}
```

---

## 3. JPA / Persistence Mapping

### Entity Changes

| Legacy | Modern | Change |
|--------|--------|--------|
| `@Entity(name = "quoteejb")` | `@Entity` | Use class name |
| `@Table(name = "quoteejb")` | `@Table(name = "quote")` | Clean naming |
| `@TableGenerator` | Database `SEQUENCE` | More efficient |
| `Integer` IDs | `Long` IDs | Better capacity |
| No Panache | Extend `PanacheEntity` | Active Record pattern |

### Repository Pattern

**Before (EntityManager):**
```java
QuoteDataBean quote = entityManager.find(QuoteDataBean.class, symbol);
```

**After (Panache Repository):**
```java
@ApplicationScoped
public class QuoteRepository implements PanacheRepository<Quote> {
    public Quote findBySymbol(String symbol) {
        return find("symbol", symbol).firstResult();
    }
}
```

---

## 4. Web Tier Mapping

### Servlets to REST

| Legacy Servlet | Modern REST | Notes |
|---------------|-------------|-------|
| `@WebServlet("/app")` | `@Path("/api")` | JAX-RS resources |
| `doGet()/doPost()` | `@GET/@POST` methods | HTTP method annotations |
| `HttpServletRequest` | `@Context UriInfo` | When needed |
| `HttpSession` | JWT claims | Stateless |

### JSF to React/Vue

| Legacy JSF | Modern SPA | Notes |
|------------|------------|-------|
| `@Named @SessionScoped` | React Context/Redux | State management |


---

## 7. File-by-File Migration Guide

### Core Business Logic

| Legacy File | New Location | Service |
|-------------|--------------|---------|
| `TradeSLSBBean.java` | Split into multiple services | Trading, Account, Market |
| `TradeDirect.java` | Remove (use Panache) | N/A |
| `TradeAction.java` | REST Resource classes | All |
| `TradeServices.java` | Interface definitions per service | All |
| `MarketSummarySingleton.java` | `MarketService.java` | Market |

### Entities

| Legacy Entity | New Entity | Service Owner |
|---------------|------------|---------------|
| `AccountDataBean.java` | `Account.java` | Account Service |
| `AccountProfileDataBean.java` | `AccountProfile.java` | Account Service |
| `QuoteDataBean.java` | `Quote.java` | Market Service |
| `OrderDataBean.java` | `Order.java` | Trading Service |
| `HoldingDataBean.java` | `Holding.java` | Trading Service |

### MDBs

| Legacy MDB | New Component | Service |
|------------|---------------|---------|
| `DTBroker3MDB.java` | `OrderProcessor.java` | Trading Service |
| `DTStreamer3MDB.java` | `QuoteStreamer.java` | Market Service |

### Web Components

| Legacy Component | New Component | Notes |
|-----------------|---------------|-------|
| `TradeAppServlet.java` | Remove (use REST) | N/A |
| `TradeServletAction.java` | REST Resources | Split per domain |
| `TradeAppJSF.java` | React components | Frontend app |
| `PortfolioJSF.java` | React components | Frontend app |
| `QuoteJSF.java` | React components | Frontend app |
| `MarketSummaryWebSocket.java` | Quarkus WebSocket | Market Service |

---

## 8. Testing Migration

| Legacy Testing | Modern Testing |
|---------------|----------------|
| No tests present | JUnit 5 + REST Assured |
| N/A | Testcontainers for integration |
| N/A | @QuarkusTest annotation |

---

## 9. Quick Reference Card

### Annotation Translations

```
@Stateless          → @ApplicationScoped
@Singleton          → @ApplicationScoped @Startup
@EJB                → @Inject
@PersistenceContext → @Inject (with Panache)
@Resource           → @ConfigProperty or @Inject
@MessageDriven      → @Incoming("channel")
@WebServlet         → @Path + @GET/@POST
@Named              → @Path (for REST) or remove
```

### Package Translations

```
javax.ejb           → (removed, use CDI)
javax.persistence   → jakarta.persistence
javax.ws.rs         → jakarta.ws.rs
javax.inject        → jakarta.inject
javax.jms           → io.smallrye.reactive.messaging
javax.websocket     → io.quarkus.websocket
javax.faces         → (removed, use SPA)
```

---
*Document Version: 1.0 | Created: 2026-01-31 | Status: Draft*

---

## 5. Service Interface Mapping

### TradeServices Methods → REST Endpoints

| Legacy Method | REST Endpoint | HTTP Method |
|--------------|---------------|-------------|
| `login(userId, password)` | `POST /api/auth/login` | POST |
| `logout(userId)` | `POST /api/auth/logout` | POST |
| `buy(userId, symbol, qty)` | `POST /api/orders` | POST |
| `sell(userId, holdingId)` | `POST /api/orders` | POST |
| `getQuote(symbol)` | `GET /api/quotes/{symbol}` | GET |
| `getHoldings(userId)` | `GET /api/accounts/{id}/holdings` | GET |
| `getMarketSummary()` | `GET /api/market/summary` | GET |
| `register(...)` | `POST /api/accounts` | POST |
| `getAccountData(userId)` | `GET /api/accounts/{id}` | GET |

---

## 6. Configuration Mapping

| Legacy (server.xml) | Modern (application.properties) |
|---------------------|--------------------------------|
| `<dataSource>` | `quarkus.datasource.*` |
| `<jmsQueue>` | `mp.messaging.incoming.*` |
| `<authData>` | `quarkus.oidc.*` |
| `<connectionManager>` | `quarkus.datasource.jdbc.max-size` |

