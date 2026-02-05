# Phase 3: Trading Service - Implementation Reference

## Executive Summary

This document provides the complete specification for the **Trading Service** of the modernized DayTrader application. The Trading Service handles buy/sell orders, order management, holdings, and portfolio operations. It serves as the core trading engine for the platform.

**Status**: ✅ Implemented

---

## Phase Objectives

1. **Order Management**: Create, list, retrieve, cancel, and complete buy/sell orders
2. **Holdings Management**: Track stock holdings in user portfolios
3. **Portfolio Analytics**: Provide portfolio summaries with value calculations
4. **Event Publishing**: Emit order events for asynchronous processing

---

## 1. Service Overview

| Property | Value |
|----------|-------|
| **Module** | `daytrader-quarkus/daytrader-trading-service/` |
| **Package** | `com.daytrader.trading` |
| **Port** | 8081 |
| **Base Path** | `/api` |
| **Technology** | Quarkus 3.17.x with SmallRye Reactive Messaging |

### Service Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Trading Service                                      │
│                                                                              │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐            │
│  │  OrderResource  │   │ HoldingResource │   │PortfolioResource│            │
│  │  /api/orders/*  │   │ /api/holdings/* │   │ /api/portfolio/*│            │
│  └────────┬────────┘   └────────┬────────┘   └────────┬────────┘            │
│           │                     │                     │                      │
│           └─────────────────────┼─────────────────────┘                      │
│                                 ▼                                            │
│           ┌─────────────────────────────────────────────────┐               │
│           │    OrderService    │ HoldingService │ PortfolioService         │
│           │     (Business Logic Layer)                      │               │
│           └─────────────────────┬───────────────────────────┘               │
│                                 │                                            │
│              ┌──────────────────┼──────────────────┐                        │
│              ▼                  ▼                  ▼                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐            │
│  │ OrderRepository │  │HoldingRepository│  │OrderEventProducer│            │
│  └────────┬────────┘  └────────┬────────┘  └────────┬─────────┘            │
│           └───────────┬────────┘                    │                       │
│                       ▼                             ▼                       │
│              ┌─────────────────┐         ┌──────────────────┐              │
│              │   PostgreSQL    │         │  Message Channel │              │
│              │   (orders,      │         │  (orders-out)    │              │
│              │   holdings)     │         └──────────────────┘              │
│              └─────────────────┘                                           │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Domain Model

### 2.1 Order Entity

**File**: `src/main/java/com/daytrader/trading/entity/Order.java`

| Field | Type | Column | Constraints | Description |
|-------|------|--------|-------------|-------------|
| `id` | `Long` | `id` | PK, SEQUENCE | Order identifier |
| `orderType` | `String` | `order_type` | NOT NULL, VARCHAR(10) | "buy" or "sell" |
| `orderStatus` | `String` | `order_status` | NOT NULL, VARCHAR(20) | Order status |
| `openDate` | `Instant` | `open_date` | NOT NULL | Order creation time |
| `completionDate` | `Instant` | `completion_date` | NULL | Order completion time |
| `quantity` | `double` | `quantity` | NOT NULL | Number of shares |
| `price` | `BigDecimal` | `price` | DECIMAL(14,2) | Price per share |
| `orderFee` | `BigDecimal` | `order_fee` | DECIMAL(14,2) | Trading fee |
| `accountId` | `Long` | `account_id` | NOT NULL | Account reference |
| `quoteSymbol` | `String` | `quote_symbol` | NOT NULL, VARCHAR(10) | Stock symbol |
| `holdingId` | `Long` | `holding_id` | NULL | Related holding (for sell orders) |
| `version` | `int` | `version` | DEFAULT 0 | Optimistic locking |
| `createdAt` | `Instant` | `created_at` | NOT NULL | Audit: created |
| `updatedAt` | `Instant` | `updated_at` | NOT NULL | Audit: updated |

**Order Status Values**:
| Status | Description |
|--------|-------------|
| `open` | Order created, awaiting processing |
| `processing` | Order being executed |
| `closed` | Order execution in progress |
| `completed` | Order successfully executed |
| `cancelled` | Order cancelled by user |

**Static Finder Methods**:
- `findByAccountId(Long accountId)` → `List<Order>`
- `findByAccountIdAndStatus(Long accountId, String status)` → `List<Order>`
- `findByAccountIdAndSymbol(Long accountId, String symbol)` → `List<Order>`
- `findOpenOrdersByAccountId(Long accountId)` → `List<Order>`

### 2.2 Holding Entity

**File**: `src/main/java/com/daytrader/trading/entity/Holding.java`

| Field | Type | Column | Constraints | Description |
|-------|------|--------|-------------|-------------|
| `id` | `Long` | `id` | PK, SEQUENCE | Holding identifier |
| `accountId` | `Long` | `account_id` | NOT NULL | Account reference |
| `quoteSymbol` | `String` | `quote_symbol` | NOT NULL, VARCHAR(10) | Stock symbol |
| `quantity` | `double` | `quantity` | NOT NULL | Number of shares held |
| `purchasePrice` | `BigDecimal` | `purchase_price` | NOT NULL, DECIMAL(14,2) | Price at purchase |
| `purchaseDate` | `Instant` | `purchase_date` | NOT NULL | Date of purchase |
| `version` | `int` | `version` | DEFAULT 0 | Optimistic locking |
| `createdAt` | `Instant` | `created_at` | NOT NULL | Audit: created |
| `updatedAt` | `Instant` | `updated_at` | NOT NULL | Audit: updated |

**Static Finder Methods**:
- `findByAccountId(Long accountId)` → `List<Holding>`
- `findByAccountIdAndSymbol(Long accountId, String symbol)` → `List<Holding>`
- `findByIdAndAccountId(Long holdingId, Long accountId)` → `Holding`

---

## 3. Repository Layer

### 3.1 OrderRepository

**File**: `src/main/java/com/daytrader/trading/repository/OrderRepository.java`

```java
@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {
    List<Order> findByAccountId(Long accountId);
    List<Order> findByAccountIdAndStatus(Long accountId, String status);
    List<Order> findByAccountIdAndSymbol(Long accountId, String symbol);
    List<Order> findOpenOrdersByAccountId(Long accountId);
    Order findByIdAndAccountId(Long orderId, Long accountId);
}
```

### 3.2 HoldingRepository

**File**: `src/main/java/com/daytrader/trading/repository/HoldingRepository.java`

```java
@ApplicationScoped
public class HoldingRepository implements PanacheRepository<Holding> {
    List<Holding> findByAccountId(Long accountId);
    List<Holding> findByAccountIdAndSymbol(Long accountId, String symbol);
    Holding findByIdAndAccountId(Long holdingId, Long accountId);
}
```

---

## 4. Service Layer

### 4.1 OrderService

**File**: `src/main/java/com/daytrader/trading/service/OrderService.java`

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createOrder` | `OrderDTO orderDTO` | `OrderDTO` | Create new order, emit event |
| `getOrder` | `Long orderId, Long accountId` | `OrderDTO` | Get order by ID (ownership check) |
| `listOrders` | `Long accountId` | `List<OrderDTO>` | List all orders for account |
| `listOrdersByStatus` | `Long accountId, String status` | `List<OrderDTO>` | Filter orders by status |
| `cancelOrder` | `Long orderId, Long accountId` | `OrderDTO` | Cancel open order |
| `completeOrder` | `Long orderId` | `OrderDTO` | Mark order complete, emit event |

**Key Business Rules**:
- Default order fee: $9.95 if not specified
- Only orders with status `open` can be cancelled
- Order creation emits `OrderCreatedEvent`
- Order completion emits `OrderCompletedEvent`

### 4.2 HoldingService

**File**: `src/main/java/com/daytrader/trading/service/HoldingService.java`

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createHolding` | `HoldingDTO holdingDTO` | `HoldingDTO` | Create new holding |
| `getHolding` | `Long holdingId, Long accountId` | `HoldingDTO` | Get holding (ownership check) |
| `listHoldings` | `Long accountId` | `List<HoldingDTO>` | List all holdings for account |
| `listHoldingsBySymbol` | `Long accountId, String symbol` | `List<HoldingDTO>` | Filter by symbol |
| `deleteHolding` | `Long holdingId, Long accountId` | `void` | Remove holding |

### 4.3 PortfolioService

**File**: `src/main/java/com/daytrader/trading/service/PortfolioService.java`

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `getPortfolioSummary` | `Long accountId, BigDecimal cashBalance` | `PortfolioSummaryResponse` | Get portfolio analytics |

**Portfolio Calculations**:
- **Holdings Value**: Sum of (purchasePrice × quantity) for all holdings
- **Total Gain**: Holdings value - total purchase value
- **Total Gain %**: (Total Gain / Total Purchase Value) × 100
- **Total Portfolio Value**: Cash balance + holdings value
- **Recent Orders**: Last 5 orders by openDate DESC
- **Top Holdings**: Top 5 holdings by value DESC

**Note**: Currently uses purchase price as current price. TODO: Integrate with Market Service for real-time quotes.

---

## 5. REST API

### 5.1 Order Resource

**File**: `src/main/java/com/daytrader/trading/resource/OrderResource.java`
**Base Path**: `/api/orders`

#### POST /api/orders

Create a new buy or sell order.

**Request:**
```json
{
  "orderType": "buy",
  "accountId": 12345,
  "quoteSymbol": "AAPL",
  "quantity": 100.0,
  "price": 150.25,
  "orderFee": 9.95
}
```

**Response (201 Created):**
```json
{
  "id": 67890,
  "orderType": "buy",
  "orderStatus": "open",
  "accountId": 12345,
  "quoteSymbol": "AAPL",
  "quantity": 100.0,
  "price": 150.25,
  "orderFee": 9.95,
  "openDate": "2026-02-04T10:00:00Z",
  "completionDate": null,
  "holdingId": null,
  "createdAt": "2026-02-04T10:00:00Z",
  "updatedAt": "2026-02-04T10:00:00Z"
}
```

#### GET /api/orders

List orders for an account.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountId` | Long | Yes | Account identifier |
| `status` | String | No | Filter by order status |

**Response (200 OK):** Array of OrderDTO

#### GET /api/orders/{orderId}

Get order details.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountId` | Long | Yes | Account identifier (ownership check) |

**Response (200 OK):** OrderDTO

#### POST /api/orders/{orderId}/cancel

Cancel an open order.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountId` | Long | Yes | Account identifier (ownership check) |

**Response (200 OK):** OrderDTO with status "cancelled"

**Errors:**
| Code | Description |
|------|-------------|
| 400 | Order cannot be cancelled (not in "open" status) |
| 404 | Order not found |

---

### 5.2 Holding Resource

**File**: `src/main/java/com/daytrader/trading/resource/HoldingResource.java`
**Base Path**: `/api/holdings`

#### GET /api/holdings

List holdings for an account.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountId` | Long | Yes | Account identifier |
| `symbol` | String | No | Filter by stock symbol |

**Response (200 OK):**
```json
[
  {
    "id": 1001,
    "accountId": 12345,
    "quoteSymbol": "AAPL",
    "quantity": 100.0,
    "purchasePrice": 150.25,
    "purchaseDate": "2026-01-15T14:30:00Z",
    "createdAt": "2026-01-15T14:30:00Z",
    "updatedAt": "2026-01-15T14:30:00Z"
  }
]
```

#### GET /api/holdings/{holdingId}

Get holding details.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountId` | Long | Yes | Account identifier (ownership check) |

#### DELETE /api/holdings/{holdingId}

Delete a holding.

**Response (204 No Content)**

---

### 5.3 Portfolio Resource

**File**: `src/main/java/com/daytrader/trading/resource/PortfolioResource.java`
**Base Path**: `/api/portfolio`

#### GET /api/portfolio/summary

Get portfolio summary for an account.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `accountId` | Long | Yes | Account identifier |
| `cashBalance` | BigDecimal | No | Cash balance (defaults to 0) |

**Response (200 OK):**
```json
{
  "accountId": 12345,
  "cashBalance": 10000.00,
  "holdingsValue": 25000.00,
  "totalValue": 35000.00,
  "totalGain": 2500.00,
  "totalGainPercent": 11.11,
  "holdingsCount": 5,
  "recentOrders": [...],
  "topHoldings": [...]
}
```

---

## 6. Event Publishing

### 6.1 OrderEventProducer

**File**: `src/main/java/com/daytrader/trading/messaging/OrderEventProducer.java`

Produces order events to the `orders-out` channel.

```java
@ApplicationScoped
public class OrderEventProducer {
    @Inject
    @Channel("orders-out")
    @Broadcast
    Emitter<Object> orderEmitter;

    void emitOrderCreated(OrderCreatedEvent event);
    void emitOrderCompleted(OrderCompletedEvent event);
}
```

**Note**: Uses `Emitter<Object>` to support multiple event types on the same channel (SmallRye Reactive Messaging limitation).

### 6.2 Event Types

#### OrderCreatedEvent

**File**: `daytrader-common/src/main/java/com/daytrader/common/event/OrderCreatedEvent.java`

```java
public record OrderCreatedEvent(
    Long orderId,
    String orderType,
    Long accountId,
    String quoteSymbol,
    double quantity,
    BigDecimal price,
    Instant eventTime
) {}
```

#### OrderCompletedEvent

**File**: `daytrader-common/src/main/java/com/daytrader/common/event/OrderCompletedEvent.java`

```java
public record OrderCompletedEvent(
    Long orderId,
    String orderType,
    String orderStatus,
    Long accountId,
    String quoteSymbol,
    double quantity,
    BigDecimal price,
    BigDecimal orderFee,
    Instant completionDate,
    Instant eventTime
) {}
```

### 6.3 OrderEventConsumer

**File**: `src/main/java/com/daytrader/trading/messaging/OrderEventConsumer.java`

Consumes events from the `orders-in` channel for additional processing (notifications, analytics).

```java
@ApplicationScoped
public class OrderEventConsumer {
    @Incoming("orders-in")
    @Blocking
    void processOrderEvent(Object event);
}
```

### 6.4 Channel Configuration

**Default Mode**: In-memory channels (no external broker required)

**Kafka Mode**: Can be enabled via `quarkus.profile=kafka` (see ADR-002)

```properties
# In-memory loopback (dev/prod)
%dev.mp.messaging.outgoing.orders-out.merge=true
%prod.mp.messaging.outgoing.orders-out.merge=true
```

---

## 7. Security

### 7.1 JWT Validation

The service validates JWT tokens using SmallRye JWT.

**Configuration:**
```properties
mp.jwt.verify.issuer=https://daytrader.example.com
smallrye.jwt.verify.key.location=publicKey.pem
mp.jwt.verify.audiences=daytrader-api
```

**Key File**: `src/main/resources/publicKey.pem`

### 7.2 Role-Based Access Control

| Role | Description | Permissions |
|------|-------------|-------------|
| `trader` | Standard trading user | Create/view/cancel own orders |
| `admin` | Administrator | All trader permissions + management |

**Note**: Current implementation uses query parameters for accountId. TODO: Extract from JWT claims in production.

---

## 8. Exception Handling

### 8.1 Exception Mappers

| Exception | Mapper | HTTP Status | Error Code |
|-----------|--------|-------------|------------|
| `BusinessException` | `BusinessExceptionMapper` | 400 | Custom |
| `ResourceNotFoundException` | `ResourceNotFoundExceptionMapper` | 404 | `RESOURCE_NOT_FOUND` |

### 8.2 Error Response Format

```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Order not found: 12345",
  "timestamp": "2026-02-04T10:00:00Z",
  "path": "/api/orders/12345",
  "traceId": null
}
```

---

## 9. Configuration

**File**: `src/main/resources/application.properties`

### Key Configuration Properties

| Property | Value | Description |
|----------|-------|-------------|
| `quarkus.http.port` | 8081 | Service port |
| `quarkus.application.name` | daytrader-trading-service | Service name |
| `quarkus.datasource.db-kind` | postgresql | Database type |
| `quarkus.flyway.migrate-at-start` | true | Auto-run migrations |
| `quarkus.flyway.baseline-on-migrate` | true | Create baseline if needed |
| `quarkus.micrometer.export.prometheus.enabled` | true | Enable metrics |
| `quarkus.otel.enabled` | true | Enable tracing |

### Database Connection

```properties
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/daytrader_trading
%dev.quarkus.datasource.username=daytrader
%dev.quarkus.datasource.password=daytrader
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/daytrader_trading
%prod.quarkus.datasource.username=daytrader
%prod.quarkus.datasource.password=daytrader
```

### Hibernate Configuration

```properties
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=no-file
```

### Messaging Configuration

```properties
# In-memory channels (default - no connector = in-memory)
%dev.mp.messaging.outgoing.orders-out.merge=true
%prod.mp.messaging.outgoing.orders-out.merge=true

# Kafka (optional - activate with -Dquarkus.profile=kafka)
# %kafka.kafka.bootstrap.servers=localhost:19092
# %kafka.mp.messaging.outgoing.orders-out.connector=smallrye-kafka
# %kafka.mp.messaging.outgoing.orders-out.topic=orders
```

### Observability

| Property | Value |
|----------|-------|
| `quarkus.micrometer.export.prometheus.path` | /metrics |
| `quarkus.otel.exporter.otlp.traces.endpoint` | http://localhost:4317 |
| `quarkus.smallrye-health.root-path` | /health |

---

## 10. Database Schema

### 10.1 Tables (Hibernate-Generated)

The database schema is managed by Hibernate ORM based on entity annotations. The `db/migration` folder is empty as Flyway baseline-on-migrate handles schema creation.

#### orders Table

```sql
CREATE SEQUENCE order_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE orders (
    id              BIGINT PRIMARY KEY DEFAULT nextval('order_id_seq'),
    order_type      VARCHAR(10) NOT NULL,
    order_status    VARCHAR(20) NOT NULL,
    open_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    quantity        DOUBLE PRECISION NOT NULL,
    price           DECIMAL(14,2),
    order_fee       DECIMAL(14,2),
    account_id      BIGINT NOT NULL,
    quote_symbol    VARCHAR(10) NOT NULL,
    holding_id      BIGINT,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE
);
```

#### holding Table

```sql
CREATE SEQUENCE holding_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE holding (
    id              BIGINT PRIMARY KEY DEFAULT nextval('holding_id_seq'),
    account_id      BIGINT NOT NULL,
    quote_symbol    VARCHAR(10) NOT NULL,
    quantity        DOUBLE PRECISION NOT NULL,
    purchase_price  DECIMAL(14,2) NOT NULL,
    purchase_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE
);
```

### 10.2 Indexes

| Index | Table | Column(s) | Purpose |
|-------|-------|-----------|---------|
| `idx_orders_account_id` | orders | account_id | Account lookup |
| `idx_orders_symbol` | orders | quote_symbol | Symbol lookup |
| `idx_orders_status` | orders | order_status | Status filtering |
| `idx_orders_open_date` | orders | open_date | Date ordering |
| `idx_holding_account_id` | holding | account_id | Account lookup |
| `idx_holding_symbol` | holding | quote_symbol | Symbol lookup |

---

## 11. Dependencies

**File**: `pom.xml`

### Core Dependencies

| Dependency | Purpose |
|------------|---------|
| `daytrader-common` | Shared DTOs, events, and exceptions |
| `quarkus-arc` | CDI implementation |
| `quarkus-rest-jackson` | REST with JSON |
| `quarkus-hibernate-orm-panache` | ORM with Panache |
| `quarkus-jdbc-postgresql` | PostgreSQL driver |
| `quarkus-flyway` | Database migrations |
| `quarkus-messaging` | Reactive messaging (in-memory) |
| `quarkus-smallrye-jwt` | JWT validation |
| `mapstruct` | Entity-DTO mapping |

### Observability

| Dependency | Purpose |
|------------|---------|
| `quarkus-micrometer-registry-prometheus` | Metrics |
| `quarkus-opentelemetry` | Distributed tracing |
| `quarkus-smallrye-health` | Health checks |
| `quarkus-smallrye-openapi` | OpenAPI/Swagger |

### Testing

| Dependency | Purpose |
|------------|---------|
| `quarkus-junit5` | Quarkus test framework |
| `rest-assured` | REST API testing |
| `testcontainers-postgresql` | Integration tests |
| `smallrye-reactive-messaging-in-memory` | In-memory messaging for tests |

---

## 12. Testing

### 12.1 Test Classes

| Test Class | Type | Description |
|------------|------|-------------|
| `OrderServiceTest` | Unit | Order business logic (11 tests) |
| `HoldingServiceTest` | Unit | Holding business logic (5 tests) |
| `PortfolioServiceTest` | Unit | Portfolio calculations (4 tests) |
| `OrderResourceTest` | Integration | Order REST endpoints (7 tests) |
| `HoldingResourceTest` | Integration | Holding REST endpoints (6 tests) |
| `PortfolioResourceTest` | Integration | Portfolio REST endpoints (4 tests) |

**Total**: 37 tests across 6 test classes

### 12.2 Test Configuration

**File**: `src/test/resources/application.properties`

Tests use Testcontainers for PostgreSQL and in-memory messaging channels.

### 12.3 Test Reports

**Location**: `target/surefire-reports/`

**Latest Report**: [Trading Service Test Report](./test-reports/trading-service-test-2026-02-03-1400.md)

### 12.4 Running Tests

```bash
# Run all tests
cd daytrader-quarkus/daytrader-trading-service && mvn test

# Run specific test class
mvn test -Dtest=OrderServiceTest
mvn test -Dtest=HoldingServiceTest
mvn test -Dtest=PortfolioServiceTest
```

---

## 13. Mapper Layer

### TradingMapper

**File**: `src/main/java/com/daytrader/trading/mapper/TradingMapper.java`

MapStruct-based mapper with CDI integration.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface TradingMapper {
    OrderDTO toOrderDTO(Order order);
    @Mapping(target = "version", ignore = true)
    Order toOrder(OrderDTO orderDTO);
    HoldingDTO toHoldingDTO(Holding holding);
    @Mapping(target = "version", ignore = true)
    Holding toHolding(HoldingDTO holdingDTO);
}
```

---

## 14. API Summary

### Endpoint Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/orders` | ✅ | Create new order |
| GET | `/api/orders` | ✅ | List orders |
| GET | `/api/orders/{orderId}` | ✅ | Get order details |
| POST | `/api/orders/{orderId}/cancel` | ✅ | Cancel open order |
| GET | `/api/holdings` | ✅ | List holdings |
| GET | `/api/holdings/{holdingId}` | ✅ | Get holding details |
| DELETE | `/api/holdings/{holdingId}` | ✅ | Delete holding |
| GET | `/api/portfolio/summary` | ✅ | Get portfolio summary |

### OpenAPI Documentation

Available at runtime: `http://localhost:8081/openapi`
Swagger UI: `http://localhost:8081/swagger-ui/`

---

## 15. Order Completion Processing ✅ IMPLEMENTED

**Reference**: [ADR-003: Order Completion Implementation](./adr/ADR-003-order-completion-implementation.md)

The order completion workflow is implemented in `OrderEventConsumer`. When an `OrderCreatedEvent` is received:

### 15.1 Order Processing Flow

```
OrderCreatedEvent received
         │
         ▼
  ┌──────────────────┐
  │ Fetch Quote Price│──────────────────────────────────────────┐
  │ (Market Service) │                                          │
  └────────┬─────────┘                                          │
           │                                                    │
           ▼                                                    │
  ┌──────────────────┐     ┌──────────────────┐                │
  │ ORDER TYPE = BUY │────▶│ Validate Balance │                │
  └────────┬─────────┘     └────────┬─────────┘                │
           │                        │ Insufficient?            │
           │                        ▼                          │
           │               ┌──────────────────┐                │
           │               │  Cancel Order    │◀───────────────┤
           │               └──────────────────┘       Error    │
           │                        │                          │
           ▼                        │                          │
  ┌──────────────────┐              │                          │
  │ Debit Account    │              │                          │
  └────────┬─────────┘              │                          │
           │                        │                          │
           ▼                        │                          │
  ┌──────────────────┐              │                          │
  │ Create Holding   │              │                          │
  └────────┬─────────┘              │                          │
           │                        │                          │
           ▼                        │                          │
  ┌──────────────────┐              │                          │
  │ Complete Order   │◀─────────────┘                          │
  │ (status=complete)│                                         │
  └──────────────────┘                                         │
```

### 15.2 Key Components

1. **MarketServiceClient**: REST client to fetch current quote prices
2. **AccountServiceClient**: Extended to support balance updates
3. **OrderEventConsumer**: Enhanced to process orders (not just log)
4. **HoldingService**: Creates holdings for buy orders

### 15.3 Implementation Details

**OrderEventConsumer.java**:
- Listens on `orders-out` channel
- Processes `OrderCreatedEvent` by fetching price, validating, updating balance, creating holding
- Handles errors by cancelling orders

**AccountServiceClient.java** (new method):
```java
@PUT @Path("/accounts/{accountId}/balance")
AccountResponse updateBalance(@HeaderParam("Authorization") String auth,
                              @PathParam("accountId") Long accountId,
                              BalanceUpdateRequest request);
```

**MarketServiceClient.java** (new):
```java
@RegisterRestClient(configKey = "market-service")
public interface MarketServiceClient {
    @GET @Path("/api/quotes/{symbol}")
    QuoteResponse getQuote(@HeaderParam("Authorization") String auth,
                           @PathParam("symbol") String symbol);
}
```

---

## 16. Remaining TODOs / Future Enhancements

1. **JWT-Based Account ID**: Extract accountId from JWT claims instead of query parameters
2. **Pagination**: Implement pagination for order and holding lists
3. **Saga Pattern**: Replace synchronous calls with choreography for resilience
4. **Sell Order Implementation**: Full sell order processing with holding validation

---

## Appendix A: File Structure

```
daytrader-trading-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/daytrader/trading/
│   │   │   ├── entity/
│   │   │   │   ├── Order.java
│   │   │   │   └── Holding.java
│   │   │   ├── repository/
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── HoldingRepository.java
│   │   │   ├── service/
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── HoldingService.java
│   │   │   │   └── PortfolioService.java
│   │   │   ├── resource/
│   │   │   │   ├── OrderResource.java
│   │   │   │   ├── HoldingResource.java
│   │   │   │   └── PortfolioResource.java
│   │   │   ├── messaging/
│   │   │   │   ├── OrderEventProducer.java
│   │   │   │   └── OrderEventConsumer.java
│   │   │   ├── mapper/
│   │   │   │   └── TradingMapper.java
│   │   │   └── exception/
│   │   │       ├── BusinessExceptionMapper.java
│   │   │       └── ResourceNotFoundExceptionMapper.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── publicKey.pem
│   │       └── db/migration/
│   └── test/
│       ├── java/com/daytrader/trading/
│       │   ├── resource/
│       │   │   ├── OrderResourceTest.java
│       │   │   ├── HoldingResourceTest.java
│       │   │   └── PortfolioResourceTest.java
│       │   └── service/
│       │       ├── OrderServiceTest.java
│       │       ├── HoldingServiceTest.java
│       │       └── PortfolioServiceTest.java
│       └── resources/
│           └── application.properties
└── target/
    └── surefire-reports/
```

---

## Appendix B: Related Specifications

- [Phase 1: Core Infrastructure](./phase-01-core-infrastructure.md) - Foundation and database setup
- [Phase 2: Account Service](./phase-02-account-service.md) - User accounts and authentication
- [Trading API Specification](./api-spec-trading.md) - OpenAPI 3.0 specification
- [ADR-002: In-Memory Messaging](./adr/ADR-002-in-memory-messaging.md) - Messaging architecture decision

---

## Appendix C: Legacy Mapping

This service modernizes the following legacy components from DayTrader EE7:

| Legacy Component | Modern Component |
|------------------|------------------|
| `OrderDataBean` (EJB Entity) | `Order` (Panache Entity) |
| `HoldingDataBean` (EJB Entity) | `Holding` (Panache Entity) |
| `TradeDirect.java` / `TradeBean.java` | `OrderService`, `HoldingService` |
| JMS Queue-based ordering | SmallRye Reactive Messaging |
| JSF-based portfolio view | React + REST API |

---

*Document Version: 1.0 | Created: 2026-02-04 | Status: Implemented*

