# Phase 2: Account Service - Implementation Reference

## Executive Summary

This document provides the complete specification for the **Account Service** of the modernized DayTrader application. The Account Service handles user registration, authentication, profile management, and account operations. It serves as the identity and account management bounded context for the trading platform.

**Status**: ✅ Implemented

---

## Phase Objectives

1. **User Registration**: Enable new user account creation with profile information
2. **Authentication**: Implement JWT-based authentication (login/logout)
3. **Profile Management**: CRUD operations for user profiles
4. **Account Operations**: Balance tracking, login/logout counting, session management

---

## 1. Service Overview

| Property | Value |
|----------|-------|
| **Module** | `daytrader-quarkus/daytrader-account-service/` |
| **Package** | `com.daytrader.account` |
| **Port** | 8081 |
| **Base Path** | `/api` |
| **Technology** | Quarkus 3.17.x with SmallRye JWT |

### Service Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Account Service                                      │
│                                                                              │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐            │
│  │  AuthResource   │   │ AccountResource │   │ ProfileResource │            │
│  │  /api/auth/*    │   │  /api/accounts  │   │  /api/profiles  │            │
│  └────────┬────────┘   └────────┬────────┘   └────────┬────────┘            │
│           │                     │                     │                      │
│           └─────────────────────┼─────────────────────┘                      │
│                                 ▼                                            │
│                    ┌────────────────────────┐                                │
│                    │    AccountService      │                                │
│                    │   (Business Logic)     │                                │
│                    └────────────┬───────────┘                                │
│                                 │                                            │
│              ┌──────────────────┼──────────────────┐                        │
│              ▼                  ▼                  ▼                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐            │
│  │AccountRepository│  │AccountProfile   │  │ JwtTokenService  │            │
│  │                 │  │Repository       │  │                  │            │
│  └────────┬────────┘  └────────┬────────┘  └──────────────────┘            │
│           └───────────┬────────┘                                             │
│                       ▼                                                      │
│              ┌─────────────────┐                                            │
│              │   PostgreSQL    │                                            │
│              │   (account,     │                                            │
│              │account_profile) │                                            │
│              └─────────────────┘                                            │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Domain Model

### 2.1 Account Entity

**File**: `src/main/java/com/daytrader/account/entity/Account.java`

| Field | Type | Column | Constraints | Description |
|-------|------|--------|-------------|-------------|
| `id` | `Long` | `id` | PK, SEQUENCE | Account identifier |
| `profile` | `AccountProfile` | `profile_user_id` | FK, UNIQUE | Related profile |
| `loginCount` | `int` | `login_count` | NOT NULL, DEFAULT 0 | Login counter |
| `logoutCount` | `int` | `logout_count` | NOT NULL, DEFAULT 0 | Logout counter |
| `lastLogin` | `Instant` | `last_login` | NULL | Last login timestamp |
| `creationDate` | `Instant` | `creation_date` | NOT NULL | Account creation date |
| `balance` | `BigDecimal` | `balance` | NOT NULL, DECIMAL(14,2) | Current balance |
| `openBalance` | `BigDecimal` | `open_balance` | NOT NULL, DECIMAL(14,2) | Opening balance |
| `version` | `int` | `version` | DEFAULT 0 | Optimistic locking |
| `createdAt` | `Instant` | `created_at` | NOT NULL | Audit: created |
| `updatedAt` | `Instant` | `updated_at` | NOT NULL | Audit: updated |

**Static Finder Methods:**
- `findByProfileUserId(String userId)` → `Account`
- `findByAccountId(Long accountId)` → `Account`
- `existsByProfileUserId(String userId)` → `boolean`

### 2.2 AccountProfile Entity

**File**: `src/main/java/com/daytrader/account/entity/AccountProfile.java`

| Field | Type | Column | Constraints | Description |
|-------|------|--------|-------------|-------------|
| `userId` | `String` | `user_id` | PK, VARCHAR(255) | User identifier |
| `passwordHash` | `String` | `password_hash` | NOT NULL | Hashed password |
| `fullName` | `String` | `full_name` | VARCHAR(255) | Full name |
| `address` | `String` | `address` | VARCHAR(500) | Address |
| `email` | `String` | `email` | VARCHAR(255) | Email address |
| `creditCard` | `String` | `credit_card` | VARCHAR(255) | Credit card (masked) |
| `version` | `int` | `version` | DEFAULT 0 | Optimistic locking |
| `createdAt` | `Instant` | `created_at` | NOT NULL | Audit: created |
| `updatedAt` | `Instant` | `updated_at` | NOT NULL | Audit: updated |

**Static Finder Methods:**
- `findByUserId(String userId)` → `AccountProfile`
- `findByEmail(String email)` → `AccountProfile`
- `existsByUserId(String userId)` → `boolean`
- `existsByEmail(String email)` → `boolean`

---

## 3. Repository Layer

### 3.1 AccountRepository

**File**: `src/main/java/com/daytrader/account/repository/AccountRepository.java`

```java
@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {
    Optional<Account> findByProfileUserId(String userId);
    Optional<Account> findByAccountId(Long accountId);
    boolean existsByProfileUserId(String userId);
}
```

### 3.2 AccountProfileRepository

**File**: `src/main/java/com/daytrader/account/repository/AccountProfileRepository.java`

```java
@ApplicationScoped
public class AccountProfileRepository implements PanacheRepository<AccountProfile> {
    Optional<AccountProfile> findByUserId(String userId);
    Optional<AccountProfile> findByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
}
```

---

## 4. Service Layer

### 4.1 AccountService

**File**: `src/main/java/com/daytrader/account/service/AccountService.java`

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `getAccount` | `Long accountId` | `AccountResponse` | Get account by ID |
| `getAccountByUserId` | `String userId` | `AccountResponse` | Get account by user ID |
| `getProfile` | `String userId` | `ProfileDTO` | Get user profile |
| `validateCredentials` | `String userId, String password` | `ProfileDTO` or `null` | Validate login |
| `register` | `RegisterRequest request` | `AccountResponse` | Register new account |
| `recordLogin` | `String userId` | `void` | Record login event |
| `recordLogout` | `String userId` | `void` | Record logout event |
| `updateBalance` | `Long accountId, BigDecimal newBalance` | `void` | Update account balance |

**Key Business Rules:**
- User ID must be unique across the system
- Email must be unique across the system
- Password is hashed before storage (TODO: implement BCrypt)
- Credit card numbers are masked in responses (show last 4 digits only)

---

## 5. REST API

### 5.1 Authentication Resource

**File**: `src/main/java/com/daytrader/account/resource/AuthResource.java`
**Base Path**: `/api/auth`

#### POST /api/auth/login

Authenticate user and obtain JWT token.

**Request:**
```json
{
  "userId": "string (3-50 chars, alphanumeric with _ : -)",
  "password": "string (min 6 chars)"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "user123"
}
```

**Errors:**
| Code | Description |
|------|-------------|
| 400 | Invalid request (validation failed) |
| 401 | Invalid credentials |
| 500 | Internal server error |

#### POST /api/auth/logout

Logout user (requires authentication).

**Headers:**
```
Authorization: Bearer <token>
```

**Response (204 No Content)**

**Roles Required:** `user`, `trader`, or `admin`

---

### 5.2 Account Resource

**File**: `src/main/java/com/daytrader/account/resource/AccountResource.java`
**Base Path**: `/api/accounts`

#### POST /api/accounts

Register a new user account (public endpoint).

**Request:**
```json
{
  "userId": "string (3-50 chars, pattern: ^[a-zA-Z0-9_:-]+$)",
  "password": "string (min 8 chars)",
  "fullName": "string (max 100 chars)",
  "email": "string (valid email)",
  "address": "string (optional, max 200 chars)",
  "creditCard": "string (optional, pattern: ^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$)",
  "openBalance": "number (optional, 0-1000000, default: 10000.00)"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": "user123",
  "balance": 10000.00,
  "openBalance": 10000.00,
  "loginCount": 0,
  "logoutCount": 0,
  "lastLogin": null,
  "creationDate": "2026-02-04T10:00:00Z",
  "profile": {
    "userId": "user123",
    "fullName": "John Doe",
    "email": "john@example.com",
    "address": "123 Main St",
    "creditCard": "****-****-****-1234",
    "createdAt": "2026-02-04T10:00:00Z",
    "updatedAt": "2026-02-04T10:00:00Z"
  }
}
```

**Errors:**
| Code | Description |
|------|-------------|
| 400 | Validation failed |
| 409 | User ID or email already exists |
| 500 | Internal server error |

#### GET /api/accounts/{accountId}

Get account by ID (requires authentication).

**Response (200 OK):** Same as registration response

**Errors:**
| Code | Description |
|------|-------------|
| 401 | Missing or invalid token |
| 403 | Insufficient permissions |
| 404 | Account not found |

#### GET /api/accounts/me

Get current authenticated user's account.

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):** Same as registration response

---

### 5.3 Profile Resource

**File**: `src/main/java/com/daytrader/account/resource/ProfileResource.java`
**Base Path**: `/api/profiles`

#### GET /api/profiles/me

Get current user's profile (requires authentication).

**Response (200 OK):**
```json
{
  "userId": "user123",
  "fullName": "John Doe",
  "email": "john@example.com",
  "address": "123 Main St",
  "creditCard": "****-****-****-1234",
  "createdAt": "2026-02-04T10:00:00Z",
  "updatedAt": "2026-02-04T10:00:00Z"
}
```

**Planned Endpoints (TODO):**
- `PUT /api/profiles/me` - Update profile
- `PUT /api/profiles/me/password` - Change password

---

## 6. Security

### 6.1 JWT Token Service

**File**: `src/main/java/com/daytrader/account/security/JwtTokenService.java`

The service generates JWT tokens using SmallRye JWT (no external OIDC provider).

**Token Claims:**
| Claim | Description |
|-------|-------------|
| `sub` | User ID (subject) |
| `upn` | Email address |
| `name` | Full name |
| `email` | Email address |
| `groups` | User roles (e.g., `["trader"]`) |
| `iss` | Issuer (configurable) |
| `aud` | Audience: `daytrader-api` |
| `iat` | Issued at timestamp |
| `exp` | Expiration timestamp |

**Methods:**
- `generateToken(userId, email, fullName, roles)` → Generate token with custom roles
- `generateTraderToken(userId, email, fullName)` → Generate token with `trader` role
- `generateAdminToken(userId, email, fullName)` → Generate token with `trader` and `admin` roles

### 6.2 JWT Configuration

**Key Files:**
- `src/main/resources/privateKey.pem` - RSA private key for signing
- `src/main/resources/publicKey.pem` - RSA public key for verification

**Configuration (application.properties):**
```properties
mp.jwt.verify.issuer=https://daytrader.example.com
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.verify.key.location=publicKey.pem
mp.jwt.verify.audiences=daytrader-api
smallrye.jwt.new-token.lifespan=3600
```

### 6.3 Role-Based Access Control

| Role | Description | Permissions |
|------|-------------|-------------|
| `trader` | Standard trading user | Access own account, trade |
| `admin` | Administrator | All trader permissions + user management |
| `user` | Basic user | Limited access |

**Public Endpoints (no auth required):**
- `POST /api/auth/login`
- `POST /api/accounts` (registration)
- `/q/health/*`, `/q/metrics/*`, `/q/openapi/*`

**Authenticated Endpoints:**
- All `/api/*` endpoints (except public ones above)

---

## 7. Exception Handling

### 7.1 Exception Mappers

| Exception | Mapper | HTTP Status | Error Code |
|-----------|--------|-------------|------------|
| `BusinessException` | `BusinessExceptionMapper` | 400 | Custom |
| `ResourceNotFoundException` | `ResourceNotFoundExceptionMapper` | 404 | `RESOURCE_NOT_FOUND` |
| `ConstraintViolationException` | `ConstraintViolationExceptionMapper` | 400 | `VALIDATION_ERROR` |

### 7.2 Error Response Format

**Standard Error:**
```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable message",
  "timestamp": "2026-02-04T10:00:00Z",
  "path": "/api/accounts/123",
  "traceId": "abc123"
}
```

**Validation Error:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-02-04T10:00:00Z",
  "path": "/api/accounts",
  "violations": [
    { "field": "email", "message": "must be a valid email" },
    { "field": "password", "message": "size must be at least 8" }
  ]
}
```

---

## 8. Configuration

**File**: `src/main/resources/application.properties`

### Key Configuration Properties

| Property | Value | Description |
|----------|-------|-------------|
| `quarkus.http.port` | 8081 | Service port |
| `quarkus.application.name` | daytrader-account-service | Service name |
| `quarkus.datasource.db-kind` | postgresql | Database type |
| `quarkus.flyway.migrate-at-start` | true | Auto-run migrations |
| `quarkus.micrometer.enabled` | true | Enable metrics |
| `quarkus.otel.enabled` | true | Enable tracing |

### Database Connection

```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:daytrader}
%prod.quarkus.datasource.username=${DB_USERNAME:daytrader}
%prod.quarkus.datasource.password=${DB_PASSWORD:daytrader}
```

### Connection Pool (Agroal)

| Property | Value |
|----------|-------|
| min-size | 5 |
| max-size | 20 |
| initial-size | 5 |
| acquisition-timeout | 30s |
| idle-removal-interval | 2m |
| max-lifetime | 10m |

---

## 9. Database Schema

### 9.1 Flyway Migration

**File**: `src/main/resources/db/migration/V1.0.0__create_account_schema.sql`

### 9.2 Tables

#### account_profile

```sql
CREATE TABLE account_profile (
    user_id         VARCHAR(255) PRIMARY KEY,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    address         VARCHAR(500),
    email           VARCHAR(255),
    credit_card     VARCHAR(255),
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

#### account

```sql
CREATE SEQUENCE account_id_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE account (
    id              BIGINT PRIMARY KEY DEFAULT nextval('account_id_seq'),
    profile_user_id VARCHAR(255) NOT NULL REFERENCES account_profile(user_id) ON DELETE CASCADE,
    login_count     INTEGER DEFAULT 0 NOT NULL,
    logout_count    INTEGER DEFAULT 0 NOT NULL,
    last_login      TIMESTAMP WITH TIME ZONE,
    creation_date   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    balance         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    open_balance    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT account_profile_unique UNIQUE (profile_user_id)
);
```

### 9.3 Indexes

| Index | Table | Column(s) | Purpose |
|-------|-------|-----------|---------|
| `idx_account_profile_user_id` | account | profile_user_id | FK lookup |
| `idx_account_profile_email` | account_profile | email | Email lookup |

### 9.4 Triggers

- `update_account_profile_updated_at` - Auto-update `updated_at` on profile changes
- `update_account_updated_at` - Auto-update `updated_at` on account changes

---

## 10. Dependencies

**File**: `pom.xml`

### Core Dependencies

| Dependency | Purpose |
|------------|---------|
| `daytrader-common` | Shared DTOs and exceptions |
| `quarkus-arc` | CDI implementation |
| `quarkus-rest-jackson` | REST with JSON |
| `quarkus-hibernate-orm-panache` | ORM with Panache |
| `quarkus-jdbc-postgresql` | PostgreSQL driver |
| `quarkus-flyway` | Database migrations |
| `quarkus-smallrye-jwt` | JWT validation |
| `quarkus-smallrye-jwt-build` | JWT generation |
| `quarkus-hibernate-validator` | Bean validation |
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

---

## 11. Testing

### 11.1 Test Classes

| Test Class | Type | Description |
|------------|------|-------------|
| `AccountServiceTest` | Unit | Business logic tests (13 tests) |
| `AuthResourceTest` | Integration | Login/logout endpoint tests |
| `AccountResourceTest` | Integration | Registration and account retrieval |
| `ProfileResourceTest` | Integration | Profile endpoint tests |
| `JwtTokenServiceTest` | Unit | Token generation tests |

### 11.2 Test Configuration

**File**: `src/test/resources/application.properties`

Test-specific JWT keys are provided in:
- `src/test/resources/privateKey.pem`
- `src/test/resources/publicKey.pem`
- `src/test/resources/privateKey-pkcs8.pem`

### 11.3 Test Reports

Location: `target/surefire-reports/`

---

## 12. Mapper Layer

### AccountMapper

**File**: `src/main/java/com/daytrader/account/mapper/AccountMapper.java`

MapStruct-based mapper with CDI integration.

| Method | From | To | Notes |
|--------|------|-----|-------|
| `toAccountDTO` | `Account` | `AccountDTO` | Manual impl |
| `toProfileDTO` | `AccountProfile` | `ProfileDTO` | Masks credit card |
| `toAccountResponse` | `Account` | `AccountResponse` | Includes profile |

**Credit Card Masking:**
```java
// Input: "1234-5678-9012-3456"
// Output: "****-****-****-3456"
```

---

## 13. API Summary

### Endpoint Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | ❌ | Authenticate user |
| POST | `/api/auth/logout` | ✅ | Logout user |
| POST | `/api/accounts` | ❌ | Register new account |
| GET | `/api/accounts/{id}` | ✅ | Get account by ID |
| GET | `/api/accounts/me` | ✅ | Get current user's account |
| GET | `/api/profiles/me` | ✅ | Get current user's profile |

### OpenAPI Documentation

Available at runtime: `http://localhost:8081/q/openapi`
Swagger UI: `http://localhost:8081/swagger-ui/`

---

## 14. Next Steps / TODOs

1. **Password Security**: Implement BCrypt password hashing (currently placeholder)
2. **Profile Updates**: Implement `PUT /api/profiles/me` endpoint
3. **Password Change**: Implement `PUT /api/profiles/me/password` endpoint
4. **Token Refresh**: Add refresh token support for long sessions
5. **Account Deactivation**: Add soft-delete capability for accounts

---

## Appendix A: File Structure

```
daytrader-account-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/daytrader/account/
│   │   │   ├── entity/
│   │   │   │   ├── Account.java
│   │   │   │   └── AccountProfile.java
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   └── AccountProfileRepository.java
│   │   │   ├── service/
│   │   │   │   └── AccountService.java
│   │   │   ├── resource/
│   │   │   │   ├── AuthResource.java
│   │   │   │   ├── AccountResource.java
│   │   │   │   └── ProfileResource.java
│   │   │   ├── security/
│   │   │   │   └── JwtTokenService.java
│   │   │   ├── mapper/
│   │   │   │   └── AccountMapper.java
│   │   │   └── exception/
│   │   │       ├── BusinessExceptionMapper.java
│   │   │       ├── ResourceNotFoundExceptionMapper.java
│   │   │       └── ConstraintViolationExceptionMapper.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── privateKey.pem
│   │       ├── publicKey.pem
│   │       └── db/migration/
│   │           └── V1.0.0__create_account_schema.sql
│   └── test/
│       ├── java/com/daytrader/account/
│       │   ├── resource/
│       │   │   ├── AccountResourceTest.java
│       │   │   ├── AuthResourceTest.java
│       │   │   └── ProfileResourceTest.java
│       │   ├── security/
│       │   │   └── JwtTokenServiceTest.java
│       │   └── service/
│       │       └── AccountServiceTest.java
│       └── resources/
│           ├── application.properties
│           ├── privateKey.pem
│           ├── privateKey-pkcs8.pem
│           └── publicKey.pem
└── target/
    └── surefire-reports/
```

---

## Appendix B: Related Specifications

- [Phase 1: Core Infrastructure](./phase-01-core-infrastructure.md) - Foundation and database setup
- Phase 3: Trading Service (planned) - Orders and holdings
- Phase 4: Market Service (planned) - Quotes and market data

