# Phase 5: Messaging & Events - Implementation Reference

## Executive Summary

This document provides the complete specification for the **Messaging & Events** infrastructure of the modernized DayTrader application. The messaging layer enables asynchronous event-driven communication between services for order processing and quote updates.

**Status**: ✅ Implemented

---

## Phase Objectives

1. **Event-Driven Architecture**: Decouple order processing and market updates using asynchronous events
2. **In-Memory Channels**: Use Quarkus in-memory messaging for simplicity (per ADR-002)
3. **Kafka-Ready Design**: Enable future Kafka migration with configuration-only changes
4. **Event Types**: Define standardized event payloads for orders and quotes

---

## 1. Architecture Overview

### 1.1 Decision: In-Memory Messaging

Per [ADR-002: In-Memory Messaging](./adr/ADR-002-in-memory-messaging.md), the system uses **Quarkus in-memory channels** instead of Kafka/Redpanda for the current phase.

**Rationale**:
- Simpler architecture with fewer moving parts
- Faster local development (no broker containers required)
- Same reactive programming model preserved
- Easy migration path to Kafka when distributed messaging is needed

**Trade-offs**:
- No message persistence (messages lost on restart)
- Single JVM only (no distributed messaging)
- No message replay or consumer groups

### 1.2 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Event-Driven Messaging Flow                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────┐       ┌─────────────────────────────┐      │
│  │      Trading Service        │       │       Market Service         │      │
│  │         (Port 8081)         │       │         (Port 8082)          │      │
│  │                             │       │                              │      │
│  │  ┌───────────────────────┐  │       │  ┌────────────────────────┐ │      │
│  │  │     OrderService      │  │       │  │     QuoteService       │ │      │
│  │  │  ┌─────────────────┐  │  │       │  │  ┌─────────────────┐   │ │      │
│  │  │  │  createOrder()  │──┼──┼───────┼──┼─▶│ QuoteEventProducer│  │ │      │
│  │  │  │ completeOrder() │  │  │       │  │  │                   │  │ │      │
│  │  │  └────────┬────────┘  │  │       │  │  └─────────┬─────────┘  │ │      │
│  │  └───────────┼───────────┘  │       │  └────────────┼────────────┘ │      │
│  │              │              │       │               │              │      │
│  │              ▼              │       │               ▼              │      │
│  │  ┌───────────────────────┐  │       │  ┌────────────────────────┐ │      │
│  │  │  OrderEventProducer   │  │       │  │    quotes-out channel  │ │      │
│  │  └───────────┬───────────┘  │       │  └────────────────────────┘ │      │
│  │              │              │       │                              │      │
│  │              ▼              │       └──────────────────────────────┘      │
│  │  ┌───────────────────────┐  │                                             │
│  │  │   orders-out channel  │  │                                             │
│  │  └───────────┬───────────┘  │                                             │
│  │              │              │                                             │
│  │              ▼              │                                             │
│  │  ┌───────────────────────┐  │                                             │
│  │  │   orders-in channel   │  │                                             │
│  │  └───────────┬───────────┘  │                                             │
│  │              │              │                                             │
│  │              ▼              │                                             │
│  │  ┌───────────────────────┐  │                                             │
│  │  │  OrderEventConsumer   │  │                                             │
│  │  │  - Logging            │  │                                             │
│  │  │  - Future: Analytics  │  │                                             │
│  │  │  - Future: Notify     │  │                                             │
│  │  └───────────────────────┘  │                                             │
│  └─────────────────────────────┘                                             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Event Types

All event types are defined as Java records in the `daytrader-common` module for shared use across services.

**Location**: `daytrader-common/src/main/java/com/daytrader/common/event/`

### 2.1 OrderCreatedEvent

Published when a new buy or sell order is created.

```java
public record OrderCreatedEvent(
    @JsonProperty("orderId")    Long orderId,
    @JsonProperty("orderType")  String orderType,     // "buy" or "sell"
    @JsonProperty("accountId")  Long accountId,
    @JsonProperty("quoteSymbol") String quoteSymbol,
    double quantity,
    BigDecimal price,
    @JsonProperty("eventTime")  Instant eventTime
) {}
```

**Sample Payload**:
```json
{
  "orderId": 12345,
  "orderType": "buy",
  "accountId": 100,
  "quoteSymbol": "AAPL",
  "quantity": 50.0,
  "price": 175.50,
  "eventTime": "2026-02-04T10:30:00Z"
}
```

### 2.2 OrderCompletedEvent

Published when an order is completed (executed successfully).

```java
public record OrderCompletedEvent(
    @JsonProperty("orderId")       Long orderId,
    @JsonProperty("orderType")     String orderType,
    @JsonProperty("orderStatus")   String orderStatus,  // "completed"
    @JsonProperty("accountId")     Long accountId,
    @JsonProperty("quoteSymbol")   String quoteSymbol,
    double quantity,
    BigDecimal price,
    @JsonProperty("orderFee")      BigDecimal orderFee,
    @JsonProperty("completionDate") Instant completionDate,
    @JsonProperty("eventTime")     Instant eventTime
) {}
```

**Sample Payload**:
```json
{
  "orderId": 12345,
  "orderType": "buy",
  "orderStatus": "completed",
  "accountId": 100,
  "quoteSymbol": "AAPL",
  "quantity": 50.0,
  "price": 175.50,
  "orderFee": 9.95,
  "completionDate": "2026-02-04T10:30:05Z",
  "eventTime": "2026-02-04T10:30:05Z"
}
```

### 2.3 QuoteUpdatedEvent

Published when a stock quote price is updated.

```java
public record QuoteUpdatedEvent(
    String symbol,
    BigDecimal price,
    @JsonProperty("priceChange") double priceChange,
    double volume,
    @JsonProperty("eventTime")   Instant eventTime
) {}
```

**Sample Payload**:
```json
{
  "symbol": "AAPL",
  "price": 180.00,
  "priceChange": 4.50,
  "volume": 1500000.0,
  "eventTime": "2026-02-04T15:30:00Z"
}
```

---

## 3. Event Producers

### 3.1 OrderEventProducer (Trading Service)

**File**: `daytrader-trading-service/src/main/java/com/daytrader/trading/messaging/OrderEventProducer.java`

| Method | Event Type | Channel | Trigger |
|--------|------------|---------|---------|
| `emitOrderCreated()` | `OrderCreatedEvent` | `orders-out` | Order created |
| `emitOrderCompleted()` | `OrderCompletedEvent` | `orders-out` | Order completed |

```java
@ApplicationScoped
public class OrderEventProducer {
    @Inject
    @Channel("orders-out")
    @Broadcast
    Emitter<Object> orderEmitter;  // Object type to support multiple event types

    public void emitOrderCreated(OrderCreatedEvent event) {
        LOG.debugf("Emitting OrderCreatedEvent for order %d", event.orderId());
        orderEmitter.send(event);
    }

    public void emitOrderCompleted(OrderCompletedEvent event) {
        LOG.debugf("Emitting OrderCompletedEvent for order %d", event.orderId());
        orderEmitter.send(event);
    }
}
```

**Note**: Uses `Emitter<Object>` because SmallRye Reactive Messaging cannot inject multiple emitters with different generic types for the same channel.

### 3.2 QuoteEventProducer (Market Service)

**File**: `daytrader-market-service/src/main/java/com/daytrader/market/messaging/QuoteEventProducer.java`

| Method | Event Type | Channel | Trigger |
|--------|------------|---------|---------|
| `emitQuoteUpdated()` | `QuoteUpdatedEvent` | `quotes-out` | Quote price updated |

```java
@ApplicationScoped
public class QuoteEventProducer {
    @Inject
    @Channel("quotes-out")
    @Broadcast
    Emitter<QuoteUpdatedEvent> quoteUpdatedEmitter;

    public void emitQuoteUpdated(QuoteUpdatedEvent event) {
        LOG.debugf("Emitting QuoteUpdatedEvent for symbol %s", event.symbol());
        quoteUpdatedEmitter.send(event);
    }
}
```

---

## 4. Event Consumers

### 4.1 OrderEventConsumer (Trading Service)

**File**: `daytrader-trading-service/src/main/java/com/daytrader/trading/messaging/OrderEventConsumer.java`

Consumes order events from the `orders-in` channel for processing and logging.

```java
@ApplicationScoped
public class OrderEventConsumer {
    @Incoming("orders-in")
    @Blocking
    public void processOrderEvent(Object event) {
        if (event instanceof OrderCreatedEvent orderCreated) {
            LOG.infof("Processing OrderCreatedEvent: orderId=%d, symbol=%s",
                    orderCreated.orderId(), orderCreated.quoteSymbol());
            // Additional processing: notifications, analytics, etc.

        } else if (event instanceof OrderCompletedEvent orderCompleted) {
            LOG.infof("Processing OrderCompletedEvent: orderId=%d, status=%s",
                    orderCompleted.orderId(), orderCompleted.orderStatus());
            // Additional processing: portfolio updates, notifications, etc.
        }
    }
}
```

**Processing Flow**:
1. Receives events from `orders-in` channel
2. Pattern matches event type (`OrderCreatedEvent` or `OrderCompletedEvent`)
3. Logs event details for observability
4. Extension points for additional business logic

**Future Enhancements**:
- Send push notifications on order completion
- Update analytics dashboards
- Trigger portfolio recalculation

---

## 5. Channel Configuration

### 5.1 In-Memory Configuration (Default)

When no `connector` is specified, SmallRye Reactive Messaging uses in-memory channels.

**Trading Service** (`application.properties`):
```properties
# In-memory channels: No connector = internal in-memory channel
# Channel merge: orders-out -> orders-in (in-memory loopback)
%dev.mp.messaging.outgoing.orders-out.merge=true
%prod.mp.messaging.outgoing.orders-out.merge=true
```

**Market Service** (`application.properties`):
```properties
# quotes-out channel is in-memory (no connector = in-memory)
mp.messaging.outgoing.quotes-out.merge=true
```

### 5.2 Kafka Configuration (Optional Profile)

To enable Kafka messaging, activate the `kafka` profile:

```bash
mvn quarkus:dev -Dquarkus.profile=kafka
```

**Trading Service** (Kafka profile):
```properties
# Kafka Configuration (activate with -Dquarkus.profile=kafka)
%kafka.kafka.bootstrap.servers=localhost:19092

# Outgoing channel (producer)
%kafka.mp.messaging.outgoing.orders-out.connector=smallrye-kafka
%kafka.mp.messaging.outgoing.orders-out.topic=orders
%kafka.mp.messaging.outgoing.orders-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer

# Incoming channel (consumer)
%kafka.mp.messaging.incoming.orders-in.connector=smallrye-kafka
%kafka.mp.messaging.incoming.orders-in.topic=orders
%kafka.mp.messaging.incoming.orders-in.value.deserializer=io.quarkus.kafka.client.serialization.ObjectMapperDeserializer
%kafka.mp.messaging.incoming.orders-in.auto.offset.reset=earliest
```

**Market Service** (Kafka profile):
```properties
%kafka.kafka.bootstrap.servers=localhost:19092
%kafka.mp.messaging.outgoing.quotes-out.connector=smallrye-kafka
%kafka.mp.messaging.outgoing.quotes-out.topic=quotes
%kafka.mp.messaging.outgoing.quotes-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

---

## 6. Message Flow Diagrams

### 6.1 Order Created Flow

```
┌──────────┐      ┌──────────────┐      ┌────────────┐      ┌──────────────────┐
│  Client  │      │ OrderService │      │ Producer   │      │ OrderEventConsumer│
└────┬─────┘      └──────┬───────┘      └─────┬──────┘      └────────┬─────────┘
     │                   │                    │                      │
     │  POST /orders     │                    │                      │
     │──────────────────▶│                    │                      │
     │                   │                    │                      │
     │                   │ persist(order)     │                      │
     │                   │────────────┐       │                      │
     │                   │            │       │                      │
     │                   │◀───────────┘       │                      │
     │                   │                    │                      │
     │                   │ emitOrderCreated() │                      │
     │                   │───────────────────▶│                      │
     │                   │                    │                      │
     │                   │                    │  send(event)         │
     │                   │                    │─────────────────────▶│
     │                   │                    │                      │
     │   201 Created     │                    │                      │ processOrderEvent()
     │◀──────────────────│                    │                      │─────────────┐
     │                   │                    │                      │             │
     │                   │                    │                      │◀────────────┘
```

### 6.2 Quote Updated Flow

```
┌──────────┐      ┌──────────────┐      ┌────────────────────┐
│  Client  │      │ QuoteService │      │ QuoteEventProducer │
└────┬─────┘      └──────┬───────┘      └─────────┬──────────┘
     │                   │                        │
     │  PUT /quotes/AAPL/price?price=180          │
     │──────────────────▶│                        │
     │                   │                        │
     │                   │ update(quote)          │
     │                   │────────────┐           │
     │                   │            │           │
     │                   │◀───────────┘           │
     │                   │                        │
     │                   │ emitQuoteUpdated()     │
     │                   │───────────────────────▶│
     │                   │                        │
     │   200 OK          │                        │ send(event)
     │◀──────────────────│                        │──────────────▶ (future consumers)
```

---

## 7. Error Handling

### 7.1 Current Strategy

| Scenario | Behavior |
|----------|----------|
| Emitter fails | Exception logged, no retry |
| Consumer exception | Exception logged, message consumed |
| Invalid event type | Logged as warning, skipped |

### 7.2 Recommended Future Enhancements

| Enhancement | Description |
|-------------|-------------|
| **Dead Letter Queue** | Route failed messages to DLQ for investigation |
| **Retry Policy** | Implement exponential backoff for transient failures |
| **Acknowledgement** | Use manual ack for reliable processing |
| **Metrics** | Track message throughput, latency, and error rates |

**Example DLQ Configuration** (future):
```properties
# Dead Letter Queue (future Kafka mode)
%kafka.mp.messaging.incoming.orders-in.failure-strategy=dead-letter-queue
%kafka.mp.messaging.incoming.orders-in.dead-letter-queue.topic=orders-dlq
```

---

## 8. Testing

### 8.1 Test Configuration

**Trading Service** (`src/test/resources/application.properties`):
```properties
# Messaging Configuration for Tests (In-Memory)
mp.messaging.outgoing.orders-out.connector=smallrye-in-memory
mp.messaging.incoming.orders-in.connector=smallrye-in-memory
```

**Market Service** (`src/test/resources/application.properties`):
```properties
# Messaging Configuration for Tests (In-Memory)
mp.messaging.outgoing.quotes-out.connector=smallrye-in-memory
```

### 8.2 Test Dependencies

```xml
<dependency>
    <groupId>io.smallrye.reactive</groupId>
    <artifactId>smallrye-reactive-messaging-in-memory</artifactId>
    <scope>test</scope>
</dependency>
```

### 8.3 Testing Approach

| Test Type | Description |
|-----------|-------------|
| **Unit Tests** | Mock `OrderEventProducer`/`QuoteEventProducer` in service tests |
| **Integration Tests** | Use in-memory connector to verify event flow |
| **End-to-End** | Full flow from REST endpoint to event consumer |

**Example: Verifying Event Emission** (service test):
```java
@QuarkusTest
class OrderServiceTest {
    @Inject
    OrderService orderService;

    @Inject
    @Channel("orders-out")
    InMemorySink<Object> orderEvents;

    @Test
    void testCreateOrder_EmitsEvent() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderType("buy");
        orderDTO.setAccountId(100L);
        orderDTO.setQuoteSymbol("AAPL");
        orderDTO.setQuantity(50.0);
        orderDTO.setPrice(new BigDecimal("175.50"));

        orderService.createOrder(orderDTO);

        assertEquals(1, orderEvents.received().size());
        Object event = orderEvents.received().get(0).getPayload();
        assertTrue(event instanceof OrderCreatedEvent);
    }
}
```

---

## 9. Kafka Migration Guide

When distributed messaging is needed, follow these steps:

### Step 1: Add Kafka Dependency

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-messaging-kafka</artifactId>
</dependency>
```

### Step 2: Start Redpanda/Kafka

```bash
cd docker
docker-compose up -d redpanda redpanda-console
```

### Step 3: Activate Kafka Profile

```bash
mvn quarkus:dev -Dquarkus.profile=kafka
```

### Step 4: Verify Configuration

Uncomment Kafka configuration in `application.properties`:
```properties
%kafka.kafka.bootstrap.servers=localhost:19092
%kafka.mp.messaging.outgoing.orders-out.connector=smallrye-kafka
# ... etc
```

### Step 5: No Code Changes Required

The same `@Channel`, `@Incoming`, and `@Outgoing` annotations work with both in-memory and Kafka connectors.

---

## 10. Dependencies

### Required Dependencies (In-Memory)

| Dependency | Purpose |
|------------|---------|
| `quarkus-messaging` | SmallRye Reactive Messaging (in-memory) |
| `smallrye-reactive-messaging-in-memory` | In-memory connector for tests |

### Optional Dependencies (Kafka)

| Dependency | Purpose |
|------------|---------|
| `quarkus-messaging-kafka` | Kafka connector (includes serializers) |

---

## 11. Summary Tables

### Channels Overview

| Channel | Direction | Service | Event Types |
|---------|-----------|---------|-------------|
| `orders-out` | Outgoing | Trading | `OrderCreatedEvent`, `OrderCompletedEvent` |
| `orders-in` | Incoming | Trading | `OrderCreatedEvent`, `OrderCompletedEvent` |
| `quotes-out` | Outgoing | Market | `QuoteUpdatedEvent` |

### Event Summary

| Event | Published By | Consumed By | Trigger |
|-------|-------------|-------------|---------|
| `OrderCreatedEvent` | Trading | Trading | Order created |
| `OrderCompletedEvent` | Trading | Trading | Order completed |
| `QuoteUpdatedEvent` | Market | (future) | Quote price updated |

---

## Appendix A: File Structure

```
daytrader-common/
└── src/main/java/com/daytrader/common/event/
    ├── OrderCreatedEvent.java
    ├── OrderCompletedEvent.java
    └── QuoteUpdatedEvent.java

daytrader-trading-service/
├── src/main/java/com/daytrader/trading/messaging/
│   ├── OrderEventProducer.java
│   └── OrderEventConsumer.java
└── src/main/resources/
    └── application.properties   # Messaging config

daytrader-market-service/
├── src/main/java/com/daytrader/market/messaging/
│   └── QuoteEventProducer.java
└── src/main/resources/
    └── application.properties   # Messaging config
```

---

## Appendix B: Related Documentation

- [ADR-002: In-Memory Messaging](./adr/ADR-002-in-memory-messaging.md) - Architecture decision
- [Phase 3: Trading Service](./phase-03-trading-service.md) - Order service integration
- [Phase 4: Market Service](./phase-04-market-service.md) - Quote service integration
- [Implementation Notes: Kafka Messaging](./implementation-notes/phase-5-kafka-messaging-2026-01-31.md)

---

## Appendix C: Legacy Mapping

This implementation modernizes JMS-based messaging from the legacy DayTrader EE7 application:

| Legacy Component | Modern Component |
|------------------|------------------|
| `QueueConnectionFactory` | `@Channel` + `Emitter` |
| `Queue` (JMS) | In-memory channel / Kafka topic |
| `TopicConnectionFactory` | `@Channel` + `@Broadcast` |
| `Topic` (JMS) | In-memory channel / Kafka topic |
| `TextMessage` | Java records with Jackson |
| Manual serialization | Automatic JSON serialization |
| Container-managed JMS | SmallRye Reactive Messaging |

---

*Document Version: 1.0 | Created: 2026-02-04 | Status: Implemented*


