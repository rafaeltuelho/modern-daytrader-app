# ADR-003: Order Completion Implementation

**Status**: Accepted  
**Date**: 2026-02-04  
**Decision Makers**: Project Team  

## Context

During E2E testing of the modernized DayTrader application, we discovered a critical gap in the order processing flow:

### Problem Statement

1. **Orders remain in "OPEN" status indefinitely** - The `OrderEventConsumer` only logs events but does not trigger order completion
2. **Holdings are never created for buy orders** - No code path creates `Holding` records when orders complete
3. **Account balances are never updated** - Buy/sell operations don't debit/credit account balances
4. **Portfolio shows "No holdings"** - Users cannot see their purchased stocks

### Legacy Behavior (Java EE7 / WebSphere Liberty)

The original application in [`TradeDirect.java`](../../daytrader-ee7-ejb/src/main/java/com/ibm/websphere/samples/daytrader/direct/TradeDirect.java) and [`TradeSLSBBean.java`](../../daytrader-ee7-ejb/src/main/java/com/ibm/websphere/samples/daytrader/ejb3/TradeSLSBBean.java):

1. Creates order with initial status
2. **Immediately debits** account balance for buy orders
3. Either completes **synchronously** OR queues via **JMS** for async processing
4. On completion:
   - **BUY**: Creates `Holding` record with purchase details
   - **SELL**: Removes `Holding` record, credits account balance

### Modernized Implementation (Before Fix)

The Quarkus implementation stopped at event emission:

```java
// OrderService.java - createOrder()
orderRepository.persist(order);
orderEventProducer.emitOrderCreated(event);  // Event emitted
return mapper.toOrderDTO(order);              // But never processed!

// OrderEventConsumer.java
@Incoming("orders-out")
public void processOrderEvent(Object event) {
    LOG.infof("Processing OrderCreatedEvent...");  // Only logging!
    // Missing: completeOrder(), createHolding(), updateBalance()
}
```

### Root Cause Analysis

This gap occurred because:

1. **Phase 3 spec** ([`phase-03-trading-service.md`](../phase-03-trading-service.md)) listed order completion as "Future Enhancement" (lines 756-762)
2. **Phase 5 spec** ([`phase-05-messaging-events.md`](../phase-05-messaging-events.md)) focused on infrastructure, not business logic
3. The implementation followed specs literally - events were emitted but processing was deferred
4. No E2E testing was performed until Phase 6 (Frontend) was complete

## Decision

**Implement complete order processing flow in `OrderEventConsumer`.**

When an `OrderCreatedEvent` is received, the consumer must:

1. **Fetch current quote price** from Market Service
2. **Validate account balance** (for buy orders)
3. **Update account balance** via Account Service
4. **Create/remove holdings** via HoldingService
5. **Complete the order** via OrderService.completeOrder()
6. **Handle errors** by cancelling orders that fail processing

### Order Processing Flow (After Fix)

```
┌──────────────────┐     ┌─────────────────────┐     ┌───────────────┐
│  OrderResource   │────▶│  OrderService       │────▶│ OrderCreated  │
│  POST /orders    │     │  createOrder()      │     │ Event Emitted │
└──────────────────┘     └─────────────────────┘     └───────┬───────┘
                                                             │
                         ┌───────────────────────────────────┘
                         ▼
┌───────────────────────────────────────────────────────────────────────┐
│  OrderEventConsumer.processOrderCreated()                              │
├────────────────────────────────────────────────────────────────────────┤
│  1. Fetch quote price from Market Service                              │
│  2. Calculate order total: (quantity × price) + orderFee               │
│  3. FOR BUY:                                                           │
│     a. Validate account balance ≥ total                                │
│     b. Debit account balance                                           │
│     c. Create Holding record                                           │
│  4. FOR SELL:                                                          │
│     a. Validate holding exists and quantity sufficient                 │
│     b. Credit account balance                                          │
│     c. Update/delete Holding record                                    │
│  5. Complete order (status → "completed")                              │
│  6. On error: Cancel order (status → "cancelled")                      │
└───────────────────────────────────────────────────────────────────────┘
```

## Consequences

### Positive
- Orders complete automatically after creation
- Holdings appear in user portfolios
- Account balances reflect trading activity
- Full trading cycle works end-to-end
- Matches legacy application behavior

### Negative
- Added coupling between Trading Service and Market/Account Services
- Synchronous REST calls within event handler (could be improved with saga pattern)
- Error handling complexity increases

### Technical Debt Accepted
- Using synchronous service calls instead of choreography/saga pattern
- No compensation logic for partial failures (acceptable for MVP)

## Implementation Requirements

### 1. Add Market Service Client to Trading Service

```java
@RegisterRestClient(configKey = "market-service")
public interface MarketServiceClient {
    @GET @Path("/api/quotes/{symbol}")
    QuoteResponse getQuote(@HeaderParam("Authorization") String auth, 
                           @PathParam("symbol") String symbol);
}
```

### 2. Add Account Balance Update to Account Service

```java
// AccountResource.java
@PUT @Path("/balance")
public Response updateBalance(@Valid BalanceUpdateRequest request);
```

### 3. Enhance OrderEventConsumer

See implementation in `OrderEventConsumer.java`.

## References

- [Phase 3: Trading Service Spec](../phase-03-trading-service.md)
- [Phase 5: Messaging & Events Spec](../phase-05-messaging-events.md)
- [ADR-002: In-Memory Messaging](./ADR-002-in-memory-messaging.md)
- [Legacy TradeDirect.java](../../daytrader-ee7-ejb/src/main/java/com/ibm/websphere/samples/daytrader/direct/TradeDirect.java)

