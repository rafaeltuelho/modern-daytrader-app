# DayTrader REST API Documentation

A modernized stock trading application API built with Quarkus.

## Base URL

```
http://localhost:8080/api
```

## Authentication

The API uses JWT (JSON Web Token) Bearer authentication for protected endpoints.

### Obtaining a Token

1. Call `POST /api/auth/login` with valid credentials
2. Receive a JWT token in the response
3. Include the token in subsequent requests

### Using the Token

Include the JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### Token Configuration

- **Issuer**: `https://daytrader.example.com`
- **Audience**: `daytrader-api`
- **Expiration**: Configurable (returned in login response as `expiresIn` seconds)

---

## Endpoints

### Authentication Resource (`/api/auth`)

All authentication endpoints are public (no token required).

#### POST /api/auth/login

Authenticate user and obtain JWT token.

**Request Body:**
```json
{
  "userID": "uid:0",
  "password": "xxx"
}
```

**Response (200 OK):**
```json
{
  "userID": "uid:0",
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials or user not found

---

#### POST /api/auth/register

Register a new user account.

**Request Body:**
```json
{
  "userID": "uid:100",
  "password": "xxx",
  "fullName": "John Doe",
  "address": "123 Main St, City, State 12345",
  "email": "john.doe@example.com",
  "creditCard": "1234-5678-9012-3456",
  "openBalance": 10000.00
}
```

**Response (201 Created):**
```json
{
  "id": 101,
  "loginCount": 0,
  "logoutCount": 0,
  "lastLogin": null,
  "creationDate": "2025-01-23T10:30:00Z",
  "balance": 10000.00,
  "openBalance": 10000.00
}
```

**Error Responses:**
- `409 Conflict` - User already exists

---

#### POST /api/auth/logout

Logout the current user. **Requires authentication.**

**Response (200 OK):**
```json
{
  "message": "Logout successful",
  "userID": "uid:0"
}
```

**Error Responses:**
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - User not found

---

### Account Resource (`/api/account`)

All account endpoints require JWT authentication with "user" role.

#### GET /api/account

Get the current user's account information.

**Response (200 OK):**
```json
{
  "id": 1,
  "loginCount": 5,
  "logoutCount": 4,
  "lastLogin": "2025-01-23T09:00:00Z",
  "creationDate": "2025-01-01T00:00:00Z",
  "balance": 8500.50,
  "openBalance": 10000.00
}
```

**Error Responses:**
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Account not found

---

#### GET /api/account/profile

Get the current user's profile information.

**Response (200 OK):**
```json
{
  "userID": "uid:0",
  "fullName": "John Doe",
  "address": "123 Main St, City, State 12345",
  "email": "john.doe@example.com",
  "creditCard": "1234-5678-9012-3456"
}
```

**Error Responses:**
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Profile not found

---

#### PUT /api/account/profile

Update the current user's profile information.

**Request Body:**
```json
{
  "fullName": "John Smith",
  "address": "456 Oak Ave, Town, State 67890",
  "email": "john.smith@example.com",
  "creditCard": "9876-5432-1098-7654"
}
```

**Response (200 OK):** Updated profile object

**Error Responses:**
- `400 Bad Request` - Invalid profile data
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Profile not found

---

### Trade Resource (`/api/trade`)

All trade endpoints require JWT authentication with "user" role.

#### GET /api/trade/holdings

Get all stock holdings for the authenticated user.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "quantity": 100.0,
    "purchasePrice": 50.25,
    "purchaseDate": "2025-01-15T14:30:00Z",
    "quote": {
      "symbol": "s:0",
      "companyName": "Company Zero",
      "price": 52.75,
      "change": 2.50
    }
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Not authenticated

---

#### GET /api/trade/holdings/{id}

Get a specific holding by ID.

**Path Parameters:**
- `id` (Long) - Holding ID

**Response (200 OK):** Single holding object

**Error Responses:**
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Holding not found

---

#### GET /api/trade/orders

Get all orders for the authenticated user.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "orderType": "buy",
    "orderStatus": "closed",
    "openDate": "2025-01-15T14:30:00Z",
    "completionDate": "2025-01-15T14:30:01Z",
    "quantity": 100.0,
    "price": 50.25,
    "orderFee": 9.99,
    "quote": {
      "symbol": "s:0",
      "companyName": "Company Zero"
    }
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Not authenticated

---

#### GET /api/trade/orders/closed

Get all closed orders for the authenticated user.

**Response (200 OK):** Array of closed order objects

**Error Responses:**
- `401 Unauthorized` - Not authenticated

---

#### POST /api/trade/buy

Place a buy order for a stock.

**Request Body:**
```json
{
  "symbol": "s:0",
  "quantity": 100
}
```

**Response (201 Created):**
```json
{
  "id": 5,
  "orderType": "buy",
  "orderStatus": "closed",
  "openDate": "2025-01-23T10:30:00Z",
  "completionDate": "2025-01-23T10:30:01Z",
  "quantity": 100.0,
  "price": 52.75,
  "orderFee": 9.99,
  "quote": {
    "symbol": "s:0",
    "companyName": "Company Zero"
  }
}
```

**Error Responses:**
- `400 Bad Request` - Invalid buy request
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Stock symbol not found

---

#### POST /api/trade/sell/{holdingId}

Sell a stock holding.

**Path Parameters:**
- `holdingId` (Long) - ID of the holding to sell

**Response (200 OK):**
```json
{
  "id": 6,
  "orderType": "sell",
  "orderStatus": "closed",
  "openDate": "2025-01-23T11:00:00Z",
  "completionDate": "2025-01-23T11:00:01Z",
  "quantity": 100.0,
  "price": 55.00,
  "orderFee": 9.99,
  "quote": {
    "symbol": "s:0",
    "companyName": "Company Zero"
  }
}
```

**Error Responses:**
- `400 Bad Request` - Invalid sell request
- `401 Unauthorized` - Not authenticated
- `404 Not Found` - Holding not found



---

### Market Resource (`/api/market`)

All market endpoints are public (no authentication required).

#### GET /api/market/quotes

Get all available stock quotes.

**Response (200 OK):**
```json
[
  {
    "symbol": "s:0",
    "companyName": "Company Zero",
    "volume": 10000.0,
    "price": 52.75,
    "open": 50.00,
    "low": 49.50,
    "high": 53.25,
    "change": 2.75
  },
  {
    "symbol": "s:1",
    "companyName": "Company One",
    "volume": 8500.0,
    "price": 101.50,
    "open": 100.00,
    "low": 99.00,
    "high": 102.00,
    "change": 1.50
  }
]
```

---

#### GET /api/market/quotes/{symbol}

Get a specific stock quote by symbol.

**Path Parameters:**
- `symbol` (String) - Stock symbol (e.g., "s:0")

**Response (200 OK):**
```json
{
  "symbol": "s:0",
  "companyName": "Company Zero",
  "volume": 10000.0,
  "price": 52.75,
  "open": 50.00,
  "low": 49.50,
  "high": 53.25,
  "change": 2.75
}
```

**Error Responses:**
- `400 Bad Request` - Invalid symbol format
- `404 Not Found` - Quote not found

---

#### GET /api/market/summary

Get market summary including index values and top gainers/losers.

**Response (200 OK):**
```json
{
  "tsia": 5250.75,
  "openTsia": 5200.00,
  "volume": 1500000.0,
  "topGainers": [
    {
      "symbol": "s:5",
      "companyName": "Company Five",
      "price": 85.00,
      "change": 5.25
    }
  ],
  "topLosers": [
    {
      "symbol": "s:12",
      "companyName": "Company Twelve",
      "price": 42.00,
      "change": -3.50
    }
  ],
  "summaryDate": "2025-01-23T10:30:00Z"
}
```

---

### Health Resource (`/api/health`)

All health endpoints are public (no authentication required).

#### GET /api/health

Basic health check to verify the application is running.

**Response (200 OK):**
```json
{
  "status": "UP",
  "application": "daytrader-quarkus",
  "timestamp": "2025-01-23T10:30:00Z",
  "version": "1.0.0-SNAPSHOT"
}
```

---

#### GET /api/health/ready

Readiness check to verify the application can serve requests.

**Response (200 OK):**
```json
{
  "status": "READY",
  "checks": {
    "database": "UP",
    "services": "UP"
  },
  "timestamp": "2025-01-23T10:30:00Z"
}
```

---

## Data Models

### Account
```json
{
  "id": 1,
  "loginCount": 5,
  "logoutCount": 4,
  "lastLogin": "2025-01-23T09:00:00Z",
  "creationDate": "2025-01-01T00:00:00Z",
  "balance": 8500.50,
  "openBalance": 10000.00
}
```

### AccountProfile
```json
{
  "userID": "uid:0",
  "fullName": "John Doe",
  "address": "123 Main St, City, State 12345",
  "email": "john.doe@example.com",
  "creditCard": "1234-5678-9012-3456"
}
```

### Quote
```json
{
  "symbol": "s:0",
  "companyName": "Company Zero",
  "volume": 10000.0,
  "price": 52.75,
  "open": 50.00,
  "low": 49.50,
  "high": 53.25,
  "change": 2.75
}
```


### Holding
```json
{
  "id": 1,
  "quantity": 100.0,
  "purchasePrice": 50.25,
  "purchaseDate": "2025-01-15T14:30:00Z",
  "quote": {
    "symbol": "s:0",
    "companyName": "Company Zero",
    "price": 52.75
  }
}
```

### Order
```json
{
  "id": 1,
  "orderType": "buy",
  "orderStatus": "closed",
  "openDate": "2025-01-15T14:30:00Z",
  "completionDate": "2025-01-15T14:30:01Z",
  "quantity": 100.0,
  "price": 50.25,
  "orderFee": 9.99,
  "quote": {
    "symbol": "s:0",
    "companyName": "Company Zero"
  }
}
```

**Order Types:** `buy`, `sell`

**Order Statuses:** `open`, `processing`, `completed`, `closed`, `cancelled`

### MarketSummary
```json
{
  "tsia": 5250.75,
  "openTsia": 5200.00,
  "volume": 1500000.0,
  "topGainers": [ /* Array of Quote objects */ ],
  "topLosers": [ /* Array of Quote objects */ ],
  "summaryDate": "2025-01-23T10:30:00Z"
}
```

---

## Error Responses

All API errors follow a consistent format:

```json
{
  "error": "Error message describing what went wrong"
}
```

### Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| `200 OK` | Request successful |
| `201 Created` | Resource created successfully |
| `400 Bad Request` | Invalid request data or validation error |
| `401 Unauthorized` | Missing or invalid authentication token |
| `404 Not Found` | Requested resource not found |
| `409 Conflict` | Resource already exists (e.g., duplicate user) |
| `500 Internal Server Error` | Unexpected server error |

### Authentication Errors

**401 Unauthorized** - Missing or invalid JWT token:
```json
{
  "error": "Invalid credentials"
}
```

**401 Unauthorized** - User not found:
```json
{
  "error": "User not found"
}
```

### Validation Errors

**400 Bad Request** - Invalid field value:
```json
{
  "error": "Stock symbol is required"
}
```

---

## OpenAPI Specification

The complete OpenAPI 3.0 specification is available at runtime:

- **OpenAPI JSON/YAML**: `http://localhost:8080/openapi`
- **Swagger UI**: `http://localhost:8080/q/swagger-ui`

---

## CORS Configuration

The API supports Cross-Origin Resource Sharing (CORS) with the following configuration:

- **Allowed Origins**: `http://localhost:5173` (development frontend)
- **Allowed Methods**: `GET, POST, PUT, DELETE, OPTIONS`
- **Allowed Headers**: `accept, authorization, content-type, x-requested-with`
- **Credentials**: Enabled
- **Max Age**: 24 hours
