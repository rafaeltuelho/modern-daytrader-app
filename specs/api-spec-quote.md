# Quote API Specification

## Overview

This document defines the OpenAPI 3.0 specification for the DayTrader Quote Service API. The Quote Service provides stock quote data including real-time prices, historical data, and WebSocket-based live updates.

**Service**: `daytrader-quote-service`  
**Base Path**: `/api`  
**Version**: `1.0.0`

---

## OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: DayTrader Quote API
  description: |
    RESTful API for accessing stock quote data including current prices,
    historical data, and real-time updates via WebSocket.
  version: 1.0.0
  contact:
    name: DayTrader API Team
    email: api@daytrader.example.com
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0

servers:
  - url: http://localhost:8082/api
    description: Local development
  - url: https://api.daytrader.example.com/quote/api
    description: Production

tags:
  - name: Quotes
    description: Stock quote operations
  - name: WebSocket
    description: Real-time quote streaming

paths:
  # ============================================
  # QUOTE ENDPOINTS
  # ============================================
  /quotes/{symbol}:
    get:
      tags:
        - Quotes
      summary: Get quote by symbol
      description: |
        Returns the current quote for a specific stock symbol including
        price, volume, and change information.
      operationId: getQuote
      parameters:
        - name: symbol
          in: path
          required: true
          description: Stock symbol (case-insensitive)
          schema:
            type: string
            maxLength: 10
            example: "AAPL"
      responses:
        '200':
          description: Quote data
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuoteResponse'
              example:
                symbol: "AAPL"
                companyName: "Apple Inc."
                price: 150.25
                open: 149.50
                high: 151.00
                low: 148.75
                volume: 52340000
                change: 0.75
                changePercent: 0.50
                lastUpdated: "2026-01-31T15:30:00Z"
        '404':
          description: Symbol not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                error: "SYMBOL_NOT_FOUND"
                message: "Stock symbol 'INVALID' not found"
                timestamp: "2026-01-31T10:30:00Z"
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /quotes:
    get:
      tags:
        - Quotes
      summary: Get multiple quotes
      description: |
        Returns quotes for multiple stock symbols. Useful for batch
        operations like portfolio valuation or watchlist updates.
      operationId: getQuotes
      parameters:
        - name: symbols
          in: query
          required: true
          description: Comma-separated list of stock symbols (max 50)
          schema:
            type: string
            example: "AAPL,GOOGL,MSFT,AMZN"
        - name: fields
          in: query
          description: Comma-separated list of fields to include
          schema:
            type: string
            default: "symbol,price,change,changePercent"
            example: "symbol,price,change,volume"
      responses:
        '200':
          description: List of quotes
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuoteListResponse'
        '400':
          description: Invalid request (too many symbols or invalid format)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

    post:
      tags:
        - Quotes
      summary: Get quotes with POST (for large symbol lists)
      description: |
        Alternative endpoint for fetching multiple quotes when the symbol
        list exceeds URL length limits. Accepts up to 100 symbols.
      operationId: getQuotesPost
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/BatchQuoteRequest'
      responses:
        '200':
          description: List of quotes
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuoteListResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /quotes/search:
    get:
      tags:
        - Quotes
      summary: Search for stocks
      description: |
        Searches for stocks by symbol or company name. Returns matching
        quotes with basic information for typeahead/autocomplete.
      operationId: searchQuotes
      parameters:
        - name: q
          in: query
          required: true
          description: Search query (min 2 characters)
          schema:
            type: string
            minLength: 2
            maxLength: 50
        - name: limit
          in: query
          description: Maximum results to return
          schema:
            type: integer
            minimum: 1
            maximum: 20
            default: 10
      responses:
        '200':
          description: Search results
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuoteSearchResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

# ============================================
# COMPONENTS
# ============================================
components:
  schemas:
    # ========== REQUEST SCHEMAS ==========
    BatchQuoteRequest:
      type: object
      required:
        - symbols
      properties:
        symbols:
          type: array
          maxItems: 100
          items:
            type: string
          example: ["AAPL", "GOOGL", "MSFT", "AMZN"]
        fields:
          type: array
          items:
            type: string
          description: Fields to include in response
          example: ["symbol", "price", "change", "volume"]

    # ========== RESPONSE SCHEMAS ==========
    QuoteResponse:
      type: object
      properties:
        symbol:
          type: string
          description: Stock symbol
          example: "AAPL"
        companyName:
          type: string
          description: Company name
          example: "Apple Inc."
        price:
          type: number
          format: double
          description: Current price
          example: 150.25
        open:
          type: number
          format: double
          description: Opening price
          example: 149.50
        high:
          type: number
          format: double
          description: Day high
          example: 151.00
        low:
          type: number
          format: double
          description: Day low
          example: 148.75
        volume:
          type: integer
          format: int64
          description: Trading volume
          example: 52340000
        change:
          type: number
          format: double
          description: Price change from previous close
          example: 0.75
        changePercent:
          type: number
          format: double
          description: Percentage change
          example: 0.50
        lastUpdated:
          type: string
          format: date-time
          description: Last quote update time

    QuoteListResponse:
      type: object
      properties:
        quotes:
          type: array
          items:
            $ref: '#/components/schemas/QuoteResponse'
        count:
          type: integer
          description: Number of quotes returned
        notFound:
          type: array
          items:
            type: string
          description: Symbols that were not found

    QuoteSearchResponse:
      type: object
      properties:
        results:
          type: array
          items:
            type: object
            properties:
              symbol:
                type: string
              companyName:
                type: string
              price:
                type: number
                format: double
        count:
          type: integer

    ErrorResponse:
      type: object
      required:
        - error
        - message
        - timestamp
      properties:
        error:
          type: string
          example: "SYMBOL_NOT_FOUND"
        message:
          type: string
        timestamp:
          type: string
          format: date-time
        path:
          type: string
        traceId:
          type: string

  responses:
    BadRequest:
      description: Invalid request parameters
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
        X-RateLimit-Remaining:
          schema:
            type: integer
        X-RateLimit-Reset:
          schema:
            type: integer
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

## WebSocket API

### Endpoint

```
wss://api.daytrader.example.com/quote/ws/quotes
```

### Connection

```javascript
const ws = new WebSocket('wss://api.daytrader.example.com/quote/ws/quotes');
ws.onopen = () => {
  // Subscribe to symbols
  ws.send(JSON.stringify({
    type: 'subscribe',
    symbols: ['AAPL', 'GOOGL', 'MSFT']
  }));
};
```

### Message Types

#### Subscribe Request
```json
{
  "type": "subscribe",
  "symbols": ["AAPL", "GOOGL", "MSFT"]
}
```

#### Unsubscribe Request
```json
{
  "type": "unsubscribe",
  "symbols": ["AAPL"]
}
```

#### Quote Update (Server → Client)
```json
{
  "type": "quote_update",
  "data": {
    "symbol": "AAPL",
    "price": 150.30,
    "change": 0.80,
    "changePercent": 0.53,
    "volume": 52500000,
    "timestamp": "2026-01-31T15:30:05Z"
  }
}
```

#### Subscription Confirmation
```json
{
  "type": "subscribed",
  "symbols": ["AAPL", "GOOGL", "MSFT"],
  "count": 3
}
```

#### Error Message
```json
{
  "type": "error",
  "code": "INVALID_SYMBOL",
  "message": "Symbol 'INVALID' not found"
}
```

### Connection Limits
- Maximum 50 symbol subscriptions per connection
- Heartbeat ping every 30 seconds
- Idle timeout: 5 minutes (no activity)

---

## Rate Limiting

| Endpoint | Rate Limit | Window |
|----------|------------|--------|
| `GET /quotes/{symbol}` | 120 requests | 1 minute |
| `GET /quotes` (batch) | 30 requests | 1 minute |
| `POST /quotes` (batch) | 30 requests | 1 minute |
| `GET /quotes/search` | 60 requests | 1 minute |
| WebSocket connections | 5 connections | per user |

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `SYMBOL_NOT_FOUND` | 404 | Stock symbol does not exist |
| `INVALID_SYMBOL` | 400 | Symbol format is invalid |
| `TOO_MANY_SYMBOLS` | 400 | Batch request exceeds limit |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Example Requests

### Get Single Quote

```bash
curl https://api.daytrader.example.com/quote/api/quotes/AAPL
```

### Get Multiple Quotes

```bash
curl "https://api.daytrader.example.com/quote/api/quotes?symbols=AAPL,GOOGL,MSFT"
```

### Search Stocks

```bash
curl "https://api.daytrader.example.com/quote/api/quotes/search?q=apple&limit=5"
```

---

*Document Version: 1.0 | Created: 2026-01-31 | Status: Draft*

