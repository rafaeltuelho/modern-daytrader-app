# Market API Specification

## Overview

This document defines the OpenAPI 3.0 specification for the DayTrader Market Service API. The Market Service provides market-wide data including indices, top movers, market statistics, and real-time updates via WebSocket.

**Service**: `daytrader-market-service`  
**Base Path**: `/api`  
**Version**: `1.0.0`

---

## OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: DayTrader Market API
  description: |
    RESTful API for accessing market-wide data including market summary,
    indices, top gainers/losers, and real-time market updates via WebSocket.
  version: 1.0.0
  contact:
    name: DayTrader API Team
    email: api@daytrader.example.com
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0

servers:
  - url: http://localhost:8083/api
    description: Local development
  - url: https://api.daytrader.example.com/market/api
    description: Production

tags:
  - name: Market Summary
    description: Market overview and statistics
  - name: Top Movers
    description: Top gaining and losing stocks
  - name: WebSocket
    description: Real-time market streaming

paths:
  # ============================================
  # MARKET SUMMARY ENDPOINTS
  # ============================================
  /market/summary:
    get:
      tags:
        - Market Summary
      summary: Get market summary
      description: |
        Returns the current market summary including TSIA index value,
        trading volume, gain/loss percentage, and market status.
      operationId: getMarketSummary
      responses:
        '200':
          description: Market summary data
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MarketSummaryResponse'
              example:
                tsia: 10542.35
                openTsia: 10500.00
                volume: 1250000000
                gainPercent: 0.40
                summaryDate: "2026-01-31T16:00:00Z"
                marketStatus: "OPEN"
                topGainersCount: 150
                topLosersCount: 120
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /market/status:
    get:
      tags:
        - Market Summary
      summary: Get market status
      description: |
        Returns the current market status (open, closed, pre-market, after-hours)
        and trading session times.
      operationId: getMarketStatus
      responses:
        '200':
          description: Market status
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MarketStatusResponse'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ============================================
  # TOP MOVERS ENDPOINTS
  # ============================================
  /market/gainers:
    get:
      tags:
        - Top Movers
      summary: Get top gaining stocks
      description: |
        Returns the top gaining stocks by percentage change.
      operationId: getTopGainers
      parameters:
        - name: limit
          in: query
          description: Number of stocks to return
          schema:
            type: integer
            minimum: 1
            maximum: 50
            default: 10
      responses:
        '200':
          description: List of top gainers
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TopMoversResponse'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /market/losers:
    get:
      tags:
        - Top Movers
      summary: Get top losing stocks
      description: |
        Returns the top losing stocks by percentage change.
      operationId: getTopLosers
      parameters:
        - name: limit
          in: query
          description: Number of stocks to return
          schema:
            type: integer
            minimum: 1
            maximum: 50
            default: 10
      responses:
        '200':
          description: List of top losers
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TopMoversResponse'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /market/volume:
    get:
      tags:
        - Market Summary
      summary: Get market volume statistics
      description: |
        Returns detailed volume statistics for the trading day.
      operationId: getMarketVolume
      responses:
        '200':
          description: Volume statistics
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VolumeStatsResponse'
        '500':
          $ref: '#/components/responses/InternalServerError'

# ============================================
# COMPONENTS
# ============================================
components:
  schemas:
    MarketSummaryResponse:
      type: object
      properties:
        tsia:
          type: number
          format: double
          description: Current TSIA index value
          example: 10542.35
        openTsia:
          type: number
          format: double
          description: Opening TSIA index value
          example: 10500.00
        volume:
          type: integer
          format: int64
          description: Total market trading volume
          example: 1250000000
        gainPercent:
          type: number
          format: double
          description: Market-wide gain/loss percentage
          example: 0.40
        summaryDate:
          type: string
          format: date-time
          description: Summary timestamp
        marketStatus:
          type: string
          enum: [PRE_MARKET, OPEN, CLOSED, AFTER_HOURS]
          example: "OPEN"
        topGainersCount:
          type: integer
          description: Number of stocks with positive change
        topLosersCount:
          type: integer
          description: Number of stocks with negative change
        topGainers:
          type: array
          maxItems: 5
          items:
            $ref: '#/components/schemas/QuoteSummary'
          description: Top 5 gainers (preview)
        topLosers:
          type: array
          maxItems: 5
          items:
            $ref: '#/components/schemas/QuoteSummary'
          description: Top 5 losers (preview)

    MarketStatusResponse:
      type: object
      properties:
        status:
          type: string
          enum: [PRE_MARKET, OPEN, CLOSED, AFTER_HOURS]
        currentTime:
          type: string
          format: date-time
        marketOpen:
          type: string
          format: time
          example: "09:30:00"
        marketClose:
          type: string
          format: time
          example: "16:00:00"
        timezone:
          type: string
          example: "America/New_York"
        tradingDay:
          type: string
          format: date
        isHoliday:
          type: boolean

    TopMoversResponse:
      type: object
      properties:
        movers:
          type: array
          items:
            $ref: '#/components/schemas/QuoteSummary'
        count:
          type: integer
        asOf:
          type: string
          format: date-time

    QuoteSummary:
      type: object
      properties:
        symbol:
          type: string
          example: "AAPL"
        companyName:
          type: string
          example: "Apple Inc."
        price:
          type: number
          format: double
          example: 150.25
        change:
          type: number
          format: double
          example: 5.25
        changePercent:
          type: number
          format: double
          example: 3.62
        volume:
          type: integer
          format: int64

    VolumeStatsResponse:
      type: object
      properties:
        totalVolume:
          type: integer
          format: int64
        advancingVolume:
          type: integer
          format: int64
        decliningVolume:
          type: integer
          format: int64
        unchangedVolume:
          type: integer
          format: int64
        asOf:
          type: string
          format: date-time

    ErrorResponse:
      type: object
      required:
        - error
        - message
        - timestamp
      properties:
        error:
          type: string
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
wss://api.daytrader.example.com/market/ws/market
```

### Connection

```javascript
const ws = new WebSocket('wss://api.daytrader.example.com/market/ws/market');
ws.onopen = () => {
  // Subscribe to market updates
  ws.send(JSON.stringify({
    type: 'subscribe',
    channels: ['summary', 'gainers', 'losers']
  }));
};
```

### Message Types

#### Subscribe Request
```json
{
  "type": "subscribe",
  "channels": ["summary", "gainers", "losers", "volume"]
}
```

#### Market Summary Update (Server → Client)
```json
{
  "type": "market_summary",
  "data": {
    "tsia": 10545.20,
    "openTsia": 10500.00,
    "volume": 1255000000,
    "gainPercent": 0.43,
    "marketStatus": "OPEN",
    "timestamp": "2026-01-31T15:30:00Z"
  }
}
```

#### Top Movers Update
```json
{
  "type": "top_movers",
  "data": {
    "gainers": [
      {"symbol": "AAPL", "changePercent": 5.2},
      {"symbol": "MSFT", "changePercent": 4.1}
    ],
    "losers": [
      {"symbol": "XYZ", "changePercent": -3.5}
    ],
    "timestamp": "2026-01-31T15:30:00Z"
  }
}
```

### Update Frequency
- Market summary: Every 5 seconds during market hours
- Top movers: Every 15 seconds during market hours
- Volume stats: Every 30 seconds during market hours

---

## Rate Limiting

| Endpoint | Rate Limit | Window |
|----------|------------|--------|
| `GET /market/summary` | 60 requests | 1 minute |
| `GET /market/status` | 60 requests | 1 minute |
| `GET /market/gainers` | 60 requests | 1 minute |
| `GET /market/losers` | 60 requests | 1 minute |
| `GET /market/volume` | 30 requests | 1 minute |
| WebSocket connections | 3 connections | per user |

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `MARKET_DATA_UNAVAILABLE` | 503 | Market data feed is unavailable |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Example Requests

### Get Market Summary

```bash
curl https://api.daytrader.example.com/market/api/market/summary
```

### Get Top Gainers

```bash
curl "https://api.daytrader.example.com/market/api/market/gainers?limit=20"
```

### Get Market Status

```bash
curl https://api.daytrader.example.com/market/api/market/status
```

---

*Document Version: 1.0 | Created: 2026-01-31 | Status: Draft*

