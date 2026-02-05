# Phase 4: Market Service - Implementation Reference

## Executive Summary

This document provides the complete specification for the **Market Service** of the modernized DayTrader application. The Market Service provides stock quote data, market summary statistics, top gainers/losers, and publishes quote update events for real-time market data distribution.

**Status**: ✅ Implemented

---

## Phase Objectives

1. **Quote Management**: Create, retrieve, update, and list stock quotes
2. **Market Analytics**: Provide market summary with top gainers and losers
3. **Event Publishing**: Emit quote update events for real-time distribution
4. **Price Updates**: Update quote prices and calculate price changes

---

## 1. Service Overview

| Property | Value |
|----------|-------|
| **Module** | `daytrader-quarkus/daytrader-market-service/` |
| **Package** | `com.daytrader.market` |
| **Port** | 8082 |
| **Base Path** | `/api` |
| **Technology** | Quarkus 3.17.x with SmallRye Reactive Messaging |

### Service Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Market Service                                       │
│                                                                              │
│  ┌─────────────────┐           ┌─────────────────┐                          │
│  │  QuoteResource  │           │ MarketResource  │                          │
│  │  /api/quotes/*  │           │  /api/market/*  │                          │
│  └────────┬────────┘           └────────┬────────┘                          │
│           │                             │                                    │
│           └─────────────────────────────┘                                    │
│                         │                                                    │
│                         ▼                                                    │
│           ┌─────────────────────────────────────┐                           │
│           │           QuoteService              │                           │
│           │       (Business Logic Layer)        │                           │
│           └─────────────────┬───────────────────┘                           │
│                             │                                                │
│              ┌──────────────┼──────────────────┐                            │
│              ▼              ▼                  ▼                            │
│  ┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐           │
│  │ QuoteRepository │  │   MarketMapper   │  │QuoteEventProducer│           │
│  └────────┬────────┘  └──────────────────┘  └────────┬─────────┘           │
│           │                                          │                      │
│           ▼                                          ▼                      │
│  ┌─────────────────┐                      ┌──────────────────┐             │
│  │   PostgreSQL    │                      │  Message Channel │             │
│  │   (quote)       │                      │  (quotes-out)    │             │
│  └─────────────────┘                      └──────────────────┘             │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Domain Model

### 2.1 Quote Entity

**File**: `src/main/java/com/daytrader/market/entity/Quote.java`

| Field | Type | Column | Constraints | Description |
|-------|------|--------|-------------|-------------|
| `symbol` | `String` | `symbol` | PK, VARCHAR(10) | Stock ticker symbol |
| `companyName` | `String` | `company_name` | VARCHAR(255) | Company name |
| `volume` | `double` | `volume` | NOT NULL | Trading volume |
| `price` | `BigDecimal` | `price` | NOT NULL, DECIMAL(14,2) | Current price |
| `openPrice` | `BigDecimal` | `open_price` | DECIMAL(14,2) | Opening price |
| `lowPrice` | `BigDecimal` | `low_price` | DECIMAL(14,2) | Day's low price |
| `highPrice` | `BigDecimal` | `high_price` | DECIMAL(14,2) | Day's high price |
| `priceChange` | `double` | `price_change` | | Price change from open |
| `version` | `int` | `version` | DEFAULT 0 | Optimistic locking |
| `createdAt` | `Instant` | `created_at` | NOT NULL | Audit: created |
| `updatedAt` | `Instant` | `updated_at` | NOT NULL | Audit: updated |

**Note**: Unlike other entities, Quote uses `symbol` (String) as the primary key rather than a sequence-generated Long ID.

**Static Finder Methods**:
- `findBySymbol(String symbol)` → `Quote`
- `existsBySymbol(String symbol)` → `boolean`
- `findAllOrdered()` → `List<Quote>`

```java
@Entity
@Table(name = "quote", indexes = {
    @Index(name = "idx_quote_symbol", columnList = "symbol", unique = true)
})
public class Quote extends PanacheEntityBase {
    @Id
    @Column(length = 10)
    public String symbol;
    
    @Column(name = "company_name", length = 255)
    public String companyName;
    
    @Column(nullable = false)
    public double volume;
    
    @Column(precision = 14, scale = 2, nullable = false)
    public BigDecimal price;
    
    // ... additional fields
}
```

---

## 3. Repository Layer

### 3.1 QuoteRepository

**File**: `src/main/java/com/daytrader/market/repository/QuoteRepository.java`

```java
@ApplicationScoped
public class QuoteRepository implements PanacheRepositoryBase<Quote, String> {
    Quote findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
    List<Quote> findAllOrdered();
    List<Quote> findTopGainers(int limit);
    List<Quote> findTopLosers(int limit);
}
```

| Method | Query | Description |
|--------|-------|-------------|
| `findBySymbol` | `findById(symbol.toUpperCase())` | Find quote by symbol (case-insensitive) |
| `existsBySymbol` | `findById(symbol) != null` | Check if quote exists |
| `findAllOrdered` | `listAll(Sort.by("symbol"))` | List all quotes ordered by symbol |
| `findTopGainers` | `ORDER BY priceChange DESC` | Top N stocks with highest price change |
| `findTopLosers` | `ORDER BY priceChange ASC` | Top N stocks with lowest price change |

---

## 4. Service Layer

### 4.1 QuoteService

**File**: `src/main/java/com/daytrader/market/service/QuoteService.java`

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `getQuote` | `String symbol` | `QuoteDTO` | Get quote by symbol |
| `listQuotes` | - | `List<QuoteDTO>` | List all quotes |
| `saveQuote` | `QuoteDTO quoteDTO` | `QuoteDTO` | Create or update quote |
| `updateQuotePrice` | `String symbol, BigDecimal newPrice` | `QuoteDTO` | Update price, emit event |
| `getTopGainers` | `int limit` | `List<QuoteDTO>` | Get top gaining stocks |
| `getTopLosers` | `int limit` | `List<QuoteDTO>` | Get top losing stocks |

**Key Business Rules**:
- Price update calculates `priceChange = newPrice - oldPrice`
- Price updates emit `QuoteUpdatedEvent` for downstream consumers
- Symbol lookups are case-insensitive (converted to uppercase)
- `ResourceNotFoundException` thrown for unknown symbols

---

## 5. REST API

### 5.1 Quote Resource

**File**: `src/main/java/com/daytrader/market/resource/QuoteResource.java`
**Base Path**: `/api/quotes`

#### GET /api/quotes/{symbol}

Get quote by symbol.

**Response (200 OK):**
```json
{
  "symbol": "AAPL",
  "companyName": "Apple Inc.",
  "volume": 1000000.0,
  "price": 175.50,
  "openPrice": 173.00,
  "lowPrice": 172.50,
  "highPrice": 176.00,
  "priceChange": 2.5,
  "createdAt": "2026-02-04T10:00:00Z",
  "updatedAt": "2026-02-04T15:30:00Z"
}
```

**Errors:**
| Code | Description |
|------|-------------|
| 404 | Quote not found for symbol |

#### GET /api/quotes

List all quotes.

**Response (200 OK):** Array of QuoteDTO ordered by symbol

#### POST /api/quotes

Create or update a quote.

**Request:**
```json
{
  "symbol": "MSFT",
  "companyName": "Microsoft Corporation",
  "volume": 2000000.0,
  "price": 380.00,
  "openPrice": 375.00,
  "lowPrice": 374.00,
  "highPrice": 382.00,
  "priceChange": 5.0
}
```

**Response (200 OK):** QuoteDTO

#### PUT /api/quotes/{symbol}/price

Update quote price.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `price` | BigDecimal | Yes | New price |

**Response (200 OK):**
```json
{
  "symbol": "AAPL",
  "price": 180.00,
  "priceChange": 5.0,
  ...
}
```

**Side Effect**: Emits `QuoteUpdatedEvent` to `quotes-out` channel

---

### 5.2 Market Resource

**File**: `src/main/java/com/daytrader/market/resource/MarketResource.java`
**Base Path**: `/api/market`

#### GET /api/market/gainers

Get top gaining stocks.

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | int | No | 10 | Number of stocks to return |

**Response (200 OK):** Array of QuoteDTO (sorted by priceChange DESC)

#### GET /api/market/losers

Get top losing stocks.

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | int | No | 10 | Number of stocks to return |

**Response (200 OK):** Array of QuoteDTO (sorted by priceChange ASC)

#### GET /api/market/summary

Get market summary.

**Response (200 OK):**
```json
{
  "tsia": 10500.00,
  "openTsia": 10500.00,
  "volume": 1000000.0,
  "topGainers": [...],
  "topLosers": [...],
  "mostActive": [],
  "summaryDate": "2026-02-04T15:30:00Z"
}
```

**Note**: Market summary currently uses placeholder TSIA values. TODO: Calculate TSIA from quote prices.

---

## 6. Event Publishing

### 6.1 QuoteEventProducer

**File**: `src/main/java/com/daytrader/market/messaging/QuoteEventProducer.java`

Produces quote update events to the `quotes-out` channel.

```java
@ApplicationScoped
public class QuoteEventProducer {
    @Inject
    @Channel("quotes-out")
    @Broadcast
    Emitter<QuoteUpdatedEvent> quoteUpdatedEmitter;

    void emitQuoteUpdated(QuoteUpdatedEvent event);
}
```

### 6.2 QuoteUpdatedEvent

**File**: `daytrader-common/src/main/java/com/daytrader/common/event/QuoteUpdatedEvent.java`

```java
public record QuoteUpdatedEvent(
    String symbol,
    BigDecimal price,
    double priceChange,
    double volume,
    Instant eventTime
) {}
```

### 6.3 Channel Configuration

**Default Mode**: In-memory channels (no external broker required)

**Kafka Mode**: Can be enabled via `quarkus.profile=kafka` (see ADR-002)

```properties
# In-memory (default - no connector)
mp.messaging.outgoing.quotes-out.merge=true

# Kafka (optional - activate with -Dquarkus.profile=kafka)
# %kafka.mp.messaging.outgoing.quotes-out.connector=smallrye-kafka
# %kafka.mp.messaging.outgoing.quotes-out.topic=quotes
```

---

## 7. Security

### 7.1 Public Endpoints

Currently, all Market Service endpoints are **public** (no authentication required):

| Endpoint | Auth Required | Rationale |
|----------|---------------|-----------|
| `GET /api/quotes/*` | No | Quote data is public market information |
| `GET /api/market/*` | No | Market summary is public information |
| `POST /api/quotes` | **Should be protected** | Admin only in production |
| `PUT /api/quotes/{symbol}/price` | **Should be protected** | Admin only in production |

**TODO**: Add JWT validation for write operations in production.

### 7.2 Future Security Configuration

```properties
# Add to application.properties for production
mp.jwt.verify.issuer=https://daytrader.example.com
smallrye.jwt.verify.key.location=publicKey.pem
mp.jwt.verify.audiences=daytrader-api
```

---

## 8. Configuration

**File**: `src/main/resources/application.properties`

### Key Configuration Properties

| Property | Value | Description |
|----------|-------|-------------|
| `quarkus.http.port` | 8082 | Service port |
| `quarkus.application.name` | daytrader-market-service | Service name |
| `quarkus.datasource.db-kind` | postgresql | Database type |
| `quarkus.flyway.migrate-at-start` | true | Auto-run migrations |
| `quarkus.flyway.baseline-on-migrate` | true | Create baseline if needed |
| `quarkus.micrometer.export.prometheus.enabled` | true | Enable metrics |
| `quarkus.otel.enabled` | true | Enable tracing |

### Database Connection

```properties
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/daytrader_market
%dev.quarkus.datasource.username=daytrader
%dev.quarkus.datasource.password=daytrader
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/daytrader_market
%prod.quarkus.datasource.username=daytrader
%prod.quarkus.datasource.password=daytrader
```

### Hibernate Configuration

```properties
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=no-file
```

### Observability

| Property | Value |
|----------|-------|
| `quarkus.micrometer.export.prometheus.path` | /metrics |
| `quarkus.otel.exporter.otlp.traces.endpoint` | http://localhost:4317 |
| `quarkus.smallrye-health.root-path` | /health |

---

## 9. Database Schema

### 9.1 Quote Table (Hibernate-Generated)

The database schema is managed by Hibernate ORM based on entity annotations. The `db/migration` folder is empty as `quarkus.flyway.baseline-on-migrate=true` handles schema creation.

```sql
CREATE TABLE quote (
    symbol          VARCHAR(10) PRIMARY KEY,
    company_name    VARCHAR(255),
    volume          DOUBLE PRECISION NOT NULL,
    price           DECIMAL(14,2) NOT NULL,
    open_price      DECIMAL(14,2),
    low_price       DECIMAL(14,2),
    high_price      DECIMAL(14,2),
    price_change    DOUBLE PRECISION,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_quote_symbol ON quote(symbol);
```

### 9.2 Indexes

| Index | Table | Column(s) | Purpose |
|-------|-------|-----------|---------|
| `idx_quote_symbol` | quote | symbol | Unique primary key index |

---

## 10. Sample Data

Currently, the Market Service does **not** include sample data initialization. Quote data must be created via the REST API.

**TODO**: Consider adding `import.sql` with sample quotes for development:

```sql
-- Example import.sql (not yet implemented)
INSERT INTO quote (symbol, company_name, volume, price, open_price, low_price, high_price, price_change, version)
VALUES
  ('AAPL', 'Apple Inc.', 1000000, 175.50, 173.00, 172.50, 176.00, 2.5, 0),
  ('MSFT', 'Microsoft Corporation', 800000, 380.00, 375.00, 374.00, 382.00, 5.0, 0),
  ('GOOGL', 'Alphabet Inc.', 500000, 140.00, 138.00, 137.50, 141.00, 2.0, 0);
```

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
| `quarkus-websockets` | WebSocket support (future real-time quotes) |
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
| `MarketMapperTest` | Unit | Entity-DTO mapping (4 tests) |
| `QuoteServiceTest` | Unit | Quote business logic (7 tests) |
| `QuoteResourceTest` | Integration | Quote REST endpoints (5 active, 1 skipped) |
| `MarketResourceTest` | Integration | Market REST endpoints (6 tests) |

**Total**: 22 active tests, 1 skipped

### 12.2 Test Configuration

**File**: `src/test/resources/application.properties`

```properties
# PostgreSQL Testcontainers
quarkus.datasource.devservices.enabled=true
quarkus.datasource.devservices.image-name=postgres:15-alpine

# Hibernate for Tests
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.flyway.migrate-at-start=false

# In-Memory Messaging
mp.messaging.outgoing.quotes-out.connector=smallrye-in-memory

# Java 25 Workaround
quarkus.test.arg-line=-Dnet.bytebuddy.experimental=true
```

### 12.3 Test Reports

**Location**: `target/surefire-reports/`

**Latest Report**: [Market Service Test Report](./test-reports/market-service-test-2026-02-03.md)

### 12.4 Running Tests

```bash
# Run all tests
cd daytrader-quarkus/daytrader-market-service && mvn test

# Run specific test class
mvn test -Dtest=QuoteServiceTest
mvn test -Dtest=MarketResourceTest
```

### 12.5 Known Issues

| Issue | Status | Workaround |
|-------|--------|------------|
| Hibernate Validator + Java 25 | ⚠️ | `testSaveQuote_Success` test disabled |

---

## 13. Mapper Layer

### MarketMapper

**File**: `src/main/java/com/daytrader/market/mapper/MarketMapper.java`

MapStruct-based mapper with CDI integration.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface MarketMapper {
    QuoteDTO toQuoteDTO(Quote quote);

    @Mapping(target = "version", ignore = true)
    Quote toQuote(QuoteDTO quoteDTO);
}
```

---

## 14. Exception Handling

### 14.1 Exception Mappers

| Exception | Mapper | HTTP Status | Error Code |
|-----------|--------|-------------|------------|
| `BusinessException` | `BusinessExceptionMapper` | 400 | Custom |
| `ResourceNotFoundException` | `ResourceNotFoundExceptionMapper` | 404 | `RESOURCE_NOT_FOUND` |

### 14.2 Error Response Format

```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Quote not found for symbol: XYZ",
  "timestamp": "2026-02-04T10:00:00Z",
  "path": "/api/quotes/XYZ",
  "traceId": null
}
```

---

## 15. API Summary

### Endpoint Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/quotes` | No | List all quotes |
| GET | `/api/quotes/{symbol}` | No | Get quote by symbol |
| POST | `/api/quotes` | No* | Create/update quote |
| PUT | `/api/quotes/{symbol}/price` | No* | Update quote price |
| GET | `/api/market/gainers` | No | Get top gainers |
| GET | `/api/market/losers` | No | Get top losers |
| GET | `/api/market/summary` | No | Get market summary |

*Should be protected in production

### OpenAPI Documentation

Available at runtime: `http://localhost:8082/openapi`
Swagger UI: `http://localhost:8082/swagger-ui/`

---

## 16. Next Steps / TODOs

1. **TSIA Calculation**: Implement real TSIA index calculation from quote prices
2. **Most Active Stocks**: Add endpoint for most actively traded stocks
3. **WebSocket Streaming**: Implement real-time quote streaming via WebSocket
4. **JWT Security**: Add authentication for write operations
5. **Sample Data**: Add import.sql with sample quotes for development
6. **Market Hours**: Implement market status (open/closed/pre-market/after-hours)
7. **Rate Limiting**: Add rate limiting for public endpoints

---

## Appendix A: File Structure

```
daytrader-market-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/daytrader/market/
│   │   │   ├── entity/
│   │   │   │   └── Quote.java
│   │   │   ├── repository/
│   │   │   │   └── QuoteRepository.java
│   │   │   ├── service/
│   │   │   │   └── QuoteService.java
│   │   │   ├── resource/
│   │   │   │   ├── QuoteResource.java
│   │   │   │   └── MarketResource.java
│   │   │   ├── messaging/
│   │   │   │   └── QuoteEventProducer.java
│   │   │   ├── mapper/
│   │   │   │   └── MarketMapper.java
│   │   │   └── exception/
│   │   │       ├── BusinessExceptionMapper.java
│   │   │       └── ResourceNotFoundExceptionMapper.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/
│       ├── java/com/daytrader/market/
│       │   ├── mapper/
│       │   │   └── MarketMapperTest.java
│       │   ├── resource/
│       │   │   ├── QuoteResourceTest.java
│       │   │   └── MarketResourceTest.java
│       │   └── service/
│       │       └── QuoteServiceTest.java
│       └── resources/
│           └── application.properties
└── target/
    └── surefire-reports/
```

---

## Appendix B: Related Specifications

- [Phase 1: Core Infrastructure](./phase-01-core-infrastructure.md) - Foundation and database setup
- [Phase 2: Account Service](./phase-02-account-service.md) - User accounts and authentication
- [Phase 3: Trading Service](./phase-03-trading-service.md) - Orders and holdings management
- [Market API Specification](./api-spec-market.md) - OpenAPI 3.0 specification
- [Quote API Specification](./api-spec-quote.md) - Quote endpoints specification
- [ADR-002: In-Memory Messaging](./adr/ADR-002-in-memory-messaging.md) - Messaging architecture decision

---

## Appendix C: Legacy Mapping

This service modernizes the following legacy components from DayTrader EE7:

| Legacy Component | Modern Component |
|------------------|------------------|
| `QuoteDataBean` (JPA Entity) | `Quote` (Panache Entity) |
| `quoteejb` table | `quote` table |
| `TradeDirect.java` / `TradeBean.java` | `QuoteService` |
| `MarketSummaryDataBean` | `MarketSummaryDTO` |
| JSF-based market view | React + REST API |
| Synchronous updates | Event-driven with SmallRye Reactive Messaging |

### Legacy to Modern Field Mapping

| Legacy Field | Modern Field |
|--------------|--------------|
| `SYMBOL` | `symbol` |
| `COMPANYNAME` | `company_name` |
| `VOLUME` | `volume` |
| `PRICE` | `price` |
| `OPEN1` | `open_price` |
| `LOW` | `low_price` |
| `HIGH` | `high_price` |
| `CHANGE1` | `price_change` |
| `optLock` | `version` |

---

*Document Version: 1.0 | Created: 2026-02-04 | Status: Implemented*


