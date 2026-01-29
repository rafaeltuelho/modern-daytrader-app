# Augment Agent Memories

## Project: DayTrader7 Modernization

### Key Patterns Learned

#### Hibernate/JPA Entity Graph Issues (2026-01-28)

**Problem**: `TransientPropertyValueException` when persisting Order entities that reference Holding entities from previous transactions.

**Root Cause**: When Hibernate traverses entity relationships (e.g., Account → Orders → Holdings), it may encounter entities that were loaded in a different transaction/persistence context and considers them "transient".

**Solution Pattern**:
1. Use **projection queries** to get only scalar data (IDs, values) without loading full entity graphs
2. **Clear persistence context** after getting initial data
3. Use **native SQL** for updates that would otherwise trigger entity graph traversal
4. Fetch minimal entities fresh after clearing

**Example**:
```java
// Instead of: Account account = profile.getAccount();
// Use projection query:
Integer accountId = (Integer) entityManager.createQuery(
    "SELECT a.accountID FROM Account a WHERE a.profile.userID = :userId")
    .setParameter("userId", userID)
    .getSingleResult();

// Clear context
entityManager.clear();

// Fetch fresh
Account account = accountRepository.find("accountID", accountId).firstResult();

// Use native SQL for FK updates to avoid entity state issues
entityManager.createNativeQuery(
    "UPDATE orderejb SET HOLDING_HOLDINGID = ?1 WHERE ORDERID = ?2")
    .setParameter(1, holdingId)
    .setParameter(2, orderId)
    .executeUpdate();
```

#### Test Isolation Patterns

1. **REST tests should NOT use `@Transactional`** - REST calls execute in separate threads with their own transactions
2. **Use unique test data** - Generate unique symbols/userIDs using timestamps to avoid conflicts
3. **Use `compareTo()` for BigDecimal** - `equals()` considers scale, use `compareTo() == 0`
4. **Return completed order DTO** - When calling `completeOrder()` synchronously, return its result, not the original order object

### Entity Relationships (DayTrader)

- `Account` ↔ `AccountProfile` (OneToOne)
- `Account` → `Order` (OneToMany)
- `Account` → `Holding` (OneToMany)
- `Order` → `Holding` (ManyToOne, was OneToOne - changed due to unique constraint violations)
- `Holding` → `Quote` (ManyToOne)
- `Order` → `Quote` (ManyToOne)

### Test Counts

- **92 tests total** - All passing after fixes
- Entity tests: 15
- Repository tests: 19
- Service tests: 26 (TradeService) + 4 (MarketSummaryService)
- REST tests: ~28

