# Trading API Specification

## Overview

This document defines the OpenAPI 3.0 specification for the DayTrader Trading Service API. The Trading Service handles buy/sell orders, order management, holdings, and portfolio operations.

**Service**: `daytrader-trading-service`  
**Base Path**: `/api`  
**Version**: `1.0.0`

---

## OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: DayTrader Trading API
  description: |
    RESTful API for stock trading operations including buy/sell orders,
    order management, and portfolio holdings.
  version: 1.0.0
  contact:
    name: DayTrader API Team
    email: api@daytrader.example.com
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0

servers:
  - url: http://localhost:8081/api
    description: Local development
  - url: https://api.daytrader.example.com/trading/api
    description: Production

tags:
  - name: Orders
    description: Buy and sell order operations
  - name: Holdings
    description: Portfolio holdings management
  - name: Portfolio
    description: Portfolio summary and analytics

security:
  - bearerAuth: []

paths:
  # ============================================
  # ORDERS ENDPOINTS
  # ============================================
  /orders:
    post:
      tags:
        - Orders
      summary: Create a new order (buy or sell)
      description: |
        Creates a new buy or sell order. For buy orders, the account balance
        is debited. For sell orders, a valid holding ID must be provided.
        Orders are processed asynchronously via Kafka messaging.
      operationId: createOrder
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateOrderRequest'
            examples:
              buyOrder:
                summary: Buy 100 shares of AAPL
                value:
                  orderType: buy
                  symbol: "AAPL"
                  quantity: 100
              sellOrder:
                summary: Sell holding #12345
                value:
                  orderType: sell
                  holdingId: 12345
      responses:
        '201':
          description: Order created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderResponse'
          headers:
            Location:
              description: URL of the created order
              schema:
                type: string
                example: /api/orders/67890
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '422':
          description: Unprocessable entity (e.g., insufficient funds, invalid holding)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                insufficientFunds:
                  summary: Insufficient account balance
                  value:
                    error: INSUFFICIENT_FUNDS
                    message: "Account balance $500.00 is insufficient for order total $1,250.00"
                    timestamp: "2026-01-31T10:30:00Z"
                    path: "/api/orders"
                invalidHolding:
                  summary: Holding not found or not owned
                  value:
                    error: INVALID_HOLDING
                    message: "Holding 12345 not found or not owned by account"
                    timestamp: "2026-01-31T10:30:00Z"
                    path: "/api/orders"
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

    get:
      tags:
        - Orders
      summary: List orders for the authenticated user
      description: |
        Returns a paginated list of orders for the authenticated user.
        Orders can be filtered by status and sorted by date.
      operationId: listOrders
      security:
        - bearerAuth: []
      parameters:
        - $ref: '#/components/parameters/PageParam'
        - $ref: '#/components/parameters/SizeParam'
        - name: status
          in: query
          description: Filter by order status
          schema:
            type: string
            enum: [open, processing, closed, completed, cancelled]
        - name: orderType
          in: query
          description: Filter by order type
          schema:
            type: string
            enum: [buy, sell]
        - name: symbol
          in: query
          description: Filter by stock symbol
          schema:
            type: string
            example: "AAPL"
        - name: fromDate
          in: query
          description: Filter orders from this date (inclusive)
          schema:
            type: string
            format: date
        - name: toDate
          in: query
          description: Filter orders until this date (inclusive)
          schema:
            type: string
            format: date
        - name: sort
          in: query
          description: Sort field and direction
          schema:
            type: string
            default: openDate,desc
            example: "openDate,desc"
      responses:
        '200':
          description: List of orders
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderListResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /orders/{orderId}:
    get:
      tags:
        - Orders
      summary: Get order details
      description: Retrieves details of a specific order by ID.
      operationId: getOrder
      security:
        - bearerAuth: []
      parameters:
        - name: orderId
          in: path
          required: true
          description: Unique order identifier
          schema:
            type: integer
            format: int64
            example: 67890
      responses:
        '200':
          description: Order details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /orders/{orderId}/cancel:
    post:
      tags:
        - Orders
      summary: Cancel an open order
      description: |
        Cancels an order that is still in 'open' status. Orders that have
        started processing cannot be cancelled.
      operationId: cancelOrder
      security:
        - bearerAuth: []
      parameters:
        - name: orderId
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: Order cancelled successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderResponse'
        '400':
          description: Order cannot be cancelled (already processing or completed)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ============================================
  # HOLDINGS ENDPOINTS
  # ============================================
  /holdings:
    get:
      tags:
        - Holdings
      summary: List holdings for the authenticated user
      description: |
        Returns all stock holdings in the user's portfolio with current
        market values calculated based on latest quote prices.
      operationId: listHoldings
      security:
        - bearerAuth: []
      parameters:
        - $ref: '#/components/parameters/PageParam'
        - $ref: '#/components/parameters/SizeParam'
        - name: symbol
          in: query
          description: Filter by stock symbol
          schema:
            type: string
        - name: sort
          in: query
          description: Sort field and direction
          schema:
            type: string
            default: purchaseDate,desc
      responses:
        '200':
          description: List of holdings
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HoldingListResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /holdings/{holdingId}:
    get:
      tags:
        - Holdings
      summary: Get holding details
      description: |
        Retrieves details of a specific holding including current market
        value, gain/loss, and associated quote information.
      operationId: getHolding
      security:
        - bearerAuth: []
      parameters:
        - name: holdingId
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: Holding details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HoldingResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ============================================
  # PORTFOLIO ENDPOINTS
  # ============================================
  /portfolio/summary:
    get:
      tags:
        - Portfolio
      summary: Get portfolio summary
      description: |
        Returns a summary of the user's portfolio including total value,
        total gain/loss, number of holdings, and recent orders.
      operationId: getPortfolioSummary
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Portfolio summary
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PortfolioSummaryResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '500':
          $ref: '#/components/responses/InternalServerError'

# ============================================
# COMPONENTS
# ============================================
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: |
        JWT token obtained from Keycloak authentication.
        Include as: `Authorization: Bearer <token>`

  parameters:
    PageParam:
      name: page
      in: query
      description: Page number (0-based)
      schema:
        type: integer
        minimum: 0
        default: 0
    SizeParam:
      name: size
      in: query
      description: Number of items per page
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 20

  schemas:
    # ========== REQUEST SCHEMAS ==========
    CreateOrderRequest:
      type: object
      required:
        - orderType
      properties:
        orderType:
          type: string
          enum: [buy, sell]
          description: Type of order
        symbol:
          type: string
          maxLength: 10
          description: Stock symbol (required for buy orders)
          example: "AAPL"
        quantity:
          type: number
          format: double
          minimum: 0.01
          description: Number of shares (required for buy orders)
          example: 100.0
        holdingId:
          type: integer
          format: int64
          description: Holding ID (required for sell orders)
          example: 12345

    # ========== RESPONSE SCHEMAS ==========
    OrderResponse:
      type: object
      properties:
        id:
          type: integer
          format: int64
          example: 67890
        orderType:
          type: string
          enum: [buy, sell]
        orderStatus:
          type: string
          enum: [open, processing, closed, completed, cancelled]
        symbol:
          type: string
          example: "AAPL"
        quantity:
          type: number
          format: double
          example: 100.0
        price:
          type: number
          format: double
          example: 150.25
        orderFee:
          type: number
          format: double
          example: 9.95
        totalValue:
          type: number
          format: double
          example: 15034.95
        openDate:
          type: string
          format: date-time
        completionDate:
          type: string
          format: date-time
          nullable: true
        holdingId:
          type: integer
          format: int64
          nullable: true
        accountId:
          type: integer
          format: int64

    OrderListResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/OrderResponse'
        page:
          $ref: '#/components/schemas/PageMetadata'

    HoldingResponse:
      type: object
      properties:
        id:
          type: integer
          format: int64
        symbol:
          type: string
        companyName:
          type: string
        quantity:
          type: number
          format: double
        purchasePrice:
          type: number
          format: double
        purchaseDate:
          type: string
          format: date-time
        purchaseValue:
          type: number
          format: double
        currentPrice:
          type: number
          format: double
        currentValue:
          type: number
          format: double
        gain:
          type: number
          format: double
        gainPercent:
          type: number
          format: double

    HoldingListResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/HoldingResponse'
        page:
          $ref: '#/components/schemas/PageMetadata'
        totalValue:
          type: number
          format: double
        totalGain:
          type: number
          format: double

    PortfolioSummaryResponse:
      type: object
      properties:
        accountId:
          type: integer
          format: int64
        cashBalance:
          type: number
          format: double
          description: Available cash balance
        holdingsValue:
          type: number
          format: double
          description: Total value of all holdings
        totalValue:
          type: number
          format: double
          description: Cash balance + holdings value
        totalGain:
          type: number
          format: double
          description: Total unrealized gain/loss
        totalGainPercent:
          type: number
          format: double
        holdingsCount:
          type: integer
          description: Number of distinct holdings
        recentOrders:
          type: array
          maxItems: 5
          items:
            $ref: '#/components/schemas/OrderResponse'
        topHoldings:
          type: array
          maxItems: 5
          items:
            $ref: '#/components/schemas/HoldingResponse'

    PageMetadata:
      type: object
      properties:
        number:
          type: integer
          description: Current page number (0-based)
        size:
          type: integer
          description: Page size
        totalElements:
          type: integer
          format: int64
          description: Total number of elements
        totalPages:
          type: integer
          description: Total number of pages

    ErrorResponse:
      type: object
      required:
        - error
        - message
        - timestamp
      properties:
        error:
          type: string
          description: Error code
          example: "INSUFFICIENT_FUNDS"
        message:
          type: string
          description: Human-readable error message
        timestamp:
          type: string
          format: date-time
        path:
          type: string
          description: Request path that caused the error
        traceId:
          type: string
          description: Distributed tracing ID for debugging

  responses:
    BadRequest:
      description: Invalid request parameters
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Unauthorized:
      description: Missing or invalid authentication token
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Forbidden:
      description: Insufficient permissions for this operation
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    NotFound:
      description: Requested resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    TooManyRequests:
      description: Rate limit exceeded
      headers:
        X-RateLimit-Limit:
          schema:
            type: integer
          description: Request limit per window
        X-RateLimit-Remaining:
          schema:
            type: integer
          description: Remaining requests in window
        X-RateLimit-Reset:
          schema:
            type: integer
          description: Seconds until rate limit resets
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    InternalServerError:
      description: Internal server error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
```

---

## Rate Limiting

| Endpoint | Rate Limit | Window |
|----------|------------|--------|
| `POST /orders` | 10 requests | 1 minute |
| `GET /orders` | 60 requests | 1 minute |
| `GET /holdings` | 60 requests | 1 minute |
| `GET /portfolio/summary` | 30 requests | 1 minute |

Rate limit headers are included in all responses:
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Requests remaining
- `X-RateLimit-Reset`: Seconds until limit resets

---

## Authentication Requirements

All endpoints require a valid JWT bearer token from Keycloak. The token must contain:

| Claim | Description |
|-------|-------------|
| `preferred_username` | User ID for account lookup |
| `realm_access.roles` | Must include `trader` or `admin` role |
| `aud` | Must include `daytrader-api` audience |

**Required Role**: `trader` or `admin`

---

## Error Handling

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Invalid request data |
| `UNAUTHORIZED` | 401 | Missing/invalid token |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `INSUFFICIENT_FUNDS` | 422 | Not enough account balance |
| `INVALID_HOLDING` | 422 | Holding not found/owned |
| `ORDER_NOT_CANCELLABLE` | 400 | Order already processing |
| `QUOTE_NOT_FOUND` | 422 | Stock symbol not found |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Example Requests

### Buy Order

```bash
curl -X POST https://api.daytrader.example.com/trading/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderType": "buy",
    "symbol": "AAPL",
    "quantity": 100
  }'
```

### Sell Order

```bash
curl -X POST https://api.daytrader.example.com/trading/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderType": "sell",
    "holdingId": 12345
  }'
```

### Get Portfolio Summary

```bash
curl https://api.daytrader.example.com/trading/api/portfolio/summary \
  -H "Authorization: Bearer <token>"
```

---

*Document Version: 1.0 | Created: 2026-01-31 | Status: Draft*

