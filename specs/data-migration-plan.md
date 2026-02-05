# Data Migration Plan: DB2/Derby to PostgreSQL

## Overview

This document outlines the strategy for migrating DayTrader data from DB2 (production) and Derby (development) to PostgreSQL as part of the modernization effort.

---

## 1. Schema Migration Strategy

### Approach: Flyway-Based Migration

- **Tool**: Flyway for version-controlled schema migrations
- **Location**: `src/main/resources/db/migration/`
- **Naming**: `V1.0.0__create_*.sql` for initial schema
- **Execution**: Automatic on application startup

### Schema Mapping

| Legacy Table | Modern Table | Changes |
|--------------|--------------|---------|
| `accountejb` | `accounts` | Rename, add `created_at`, `updated_at` |
| `accountprofileejb` | `account_profiles` | Rename, add timestamps |
| `quoteejb` | `quotes` | Rename, add `last_updated` |
| `orderejb` | `orders` | Rename, add `created_at`, `status_updated_at` |
| `holdingejb` | `holdings` | Rename, add timestamps |
| `keygenejb` | Sequences | Use PostgreSQL native sequences |

---

## 2. Data Transformation Rules

### Account Data
- Preserve all account IDs and balances
- Migrate user credentials (hash passwords if needed)
- Map legacy status codes to new enum values

### Quote Data
- Preserve symbol and price history
- Add `last_updated` timestamp
- Validate price data integrity

### Order Data
- Preserve order history with timestamps
- Map legacy order status to new enum
- Maintain referential integrity with accounts and quotes

### Holding Data
- Preserve holding records with quantities
- Maintain foreign keys to accounts and quotes
- Calculate cost basis from historical orders

---

## 3. Parallel Run Approach

### Phase 1: Dual-Write (Weeks 1-2)
- Legacy system writes to DB2
- New system writes to PostgreSQL
- Validation layer compares results

### Phase 2: Dual-Read (Weeks 3-4)
- Read from PostgreSQL
- Validate against DB2
- Fix discrepancies

### Phase 3: Cutover (Week 5)
- Stop legacy writes
- Final validation
- Switch to PostgreSQL only

---

## 4. Rollback Procedures

- **Automated Backups**: Daily PostgreSQL backups
- **Point-in-Time Recovery**: Enabled for 7 days
- **Fallback Plan**: Revert to legacy system if critical issues found
- **Data Sync**: Maintain DB2 in sync for 2 weeks post-migration

---

## 5. Testing Strategy

- **Data Integrity Tests**: Row counts, checksums, referential integrity
- **Performance Tests**: Query performance benchmarks
- **Reconciliation**: Automated comparison of legacy vs. new data
- **User Acceptance**: Sample data validation by business users

---

## 6. Performance Considerations

- **Connection Pooling**: Agroal with 20-50 connections
- **Indexes**: Create on foreign keys and frequently queried columns
- **Partitioning**: Consider for large order/holding tables
- **Vacuum**: Schedule regular maintenance

---

## 7. Timeline & Resources

| Phase | Duration | Resources |
|-------|----------|-----------|
| Schema Design | 1 week | Architect, DBA |
| Data Extraction | 1 week | DBA, Engineer |
| Transformation | 2 weeks | Engineer, QA |
| Testing | 2 weeks | QA, Business |
| Cutover | 1 day | DBA, On-call |

---

## 8. Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Data Loss | Automated backups, dual-write validation |
| Performance Degradation | Load testing, index optimization |
| Referential Integrity | Constraint validation, reconciliation |
| Downtime | Parallel run, quick rollback capability |

---

*Document Version: 1.0 | Created: 2026-01-31 | Status: Complete*

