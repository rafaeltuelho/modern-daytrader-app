# Account API Specification

## Overview

This document defines the OpenAPI 3.0 specification for the DayTrader Account Service API. The Account Service handles user registration, authentication, profile management, and account operations.

**Service**: `daytrader-account-service`  
**Base Path**: `/api`  
**Version**: `1.0.0`

---

## OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: DayTrader Account API
  description: |
    RESTful API for account management including user registration,
    authentication, profile updates, and account balance operations.
  version: 1.0.0
  contact:
    name: DayTrader API Team
    email: api@daytrader.example.com
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0

servers:
  - url: http://localhost:8080/api
    description: Local development
  - url: https://api.daytrader.example.com/account/api
    description: Production

tags:
  - name: Authentication
    description: User authentication operations
  - name: Registration
    description: New user registration
  - name: Accounts
    description: Account management operations
  - name: Profiles
    description: User profile management

paths:
  # ============================================
  # AUTHENTICATION ENDPOINTS
  # ============================================
  /auth/login:
    post:
      tags:
        - Authentication
      summary: Authenticate user and obtain tokens
      description: |
        Authenticates user credentials against Keycloak and returns
        JWT access and refresh tokens. This is a convenience endpoint
        that proxies to Keycloak's token endpoint.
      operationId: login
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
            example:
              userId: "uid:0"
              password: "xxx"
      responses:
        '200':
          description: Authentication successful
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LoginResponse'
        '401':
          description: Invalid credentials
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                error: "INVALID_CREDENTIALS"
                message: "Invalid username or password"
                timestamp: "2026-01-31T10:30:00Z"
        '423':
          description: Account locked due to too many failed attempts
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /auth/logout:
    post:
      tags:
        - Authentication
      summary: Logout and invalidate tokens
      description: |
        Logs out the user by incrementing logout count and optionally
        invalidating the refresh token in Keycloak.
      operationId: logout
      security:
        - bearerAuth: []
      responses:
        '204':
          description: Logout successful
        '401':
          $ref: '#/components/responses/Unauthorized'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /auth/refresh:
    post:
      tags:
        - Authentication
      summary: Refresh access token
      description: |
        Exchanges a valid refresh token for a new access token.
      operationId: refreshToken
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshTokenRequest'
      responses:
        '200':
          description: Token refreshed successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LoginResponse'
        '401':
          description: Invalid or expired refresh token
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ============================================
  # REGISTRATION ENDPOINTS
  # ============================================
  /accounts:
    post:
      tags:
        - Registration
      summary: Register a new user account
      description: |
        Creates a new user account with the provided profile information.
        The user is created in both Keycloak (for authentication) and the
        account database. An initial balance can be specified.
      operationId: registerAccount
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterRequest'
            example:
              userId: "newuser123"
              password: "SecureP@ss123"
              fullName: "John Doe"
              email: "john.doe@example.com"
              address: "123 Main St, New York, NY 10001"
              creditCard: "4111-1111-1111-1111"
              openBalance: 50000.00
      responses:
        '201':
          description: Account created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountResponse'
          headers:
            Location:
              description: URL of the created account
              schema:
                type: string
        '400':
          $ref: '#/components/responses/BadRequest'
        '409':
          description: User ID or email already exists
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                error: "USER_EXISTS"
                message: "User ID 'newuser123' is already registered"
                timestamp: "2026-01-31T10:30:00Z"
        '422':
          description: Validation failed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
        '429':
          $ref: '#/components/responses/TooManyRequests'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ============================================
  # ACCOUNT MANAGEMENT ENDPOINTS
  # ============================================
  /accounts/me:
    get:
      tags:
        - Accounts
      summary: Get current user's account
      description: |
        Returns the account details for the currently authenticated user.
      operationId: getCurrentAccount
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Account details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Account not found for authenticated user
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /accounts/{accountId}:
    get:
      tags:
        - Accounts
      summary: Get account by ID
      description: |
        Returns account details. Users can only access their own account
        unless they have admin role.
      operationId: getAccount
      security:
        - bearerAuth: []
      parameters:
        - name: accountId
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: Account details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'
        '500':
          $ref: '#/components/responses/InternalServerError'

  # ============================================
  # PROFILE MANAGEMENT ENDPOINTS
  # ============================================
  /profiles/me:
    get:
      tags:
        - Profiles
      summary: Get current user's profile
      description: Returns the profile for the currently authenticated user.
      operationId: getCurrentProfile
      security:
        - bearerAuth: []
      responses:
        '200':
          description: Profile details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProfileResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '500':
          $ref: '#/components/responses/InternalServerError'

    put:
      tags:
        - Profiles
      summary: Update current user's profile
      description: |
        Updates profile information for the authenticated user.
        Password changes require the current password for verification.
      operationId: updateCurrentProfile
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateProfileRequest'
      responses:
        '200':
          description: Profile updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProfileResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '422':
          description: Validation failed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
        '500':
          $ref: '#/components/responses/InternalServerError'

  /profiles/me/password:
    put:
      tags:
        - Profiles
      summary: Change password
      description: |
        Changes the user's password. Requires current password verification.
      operationId: changePassword
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ChangePasswordRequest'
      responses:
        '204':
          description: Password changed successfully
        '400':
          description: Current password incorrect
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '422':
          description: New password does not meet requirements
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
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
      description: JWT token from Keycloak authentication.

  schemas:
    # ========== REQUEST SCHEMAS ==========
    LoginRequest:
      type: object
      required:
        - userId
        - password
      properties:
        userId:
          type: string
          minLength: 3
          maxLength: 50
          example: "uid:0"
        password:
          type: string
          format: password
          minLength: 6

    RefreshTokenRequest:
      type: object
      required:
        - refreshToken
      properties:
        refreshToken:
          type: string

    RegisterRequest:
      type: object
      required:
        - userId
        - password
        - fullName
        - email
      properties:
        userId:
          type: string
          minLength: 3
          maxLength: 50
          pattern: "^[a-zA-Z0-9_:-]+$"
        password:
          type: string
          format: password
          minLength: 8
        fullName:
          type: string
          maxLength: 100
        email:
          type: string
          format: email
        address:
          type: string
          maxLength: 200
        creditCard:
          type: string
          pattern: "^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$"
        openBalance:
          type: number
          format: double
          minimum: 0
          maximum: 1000000
          default: 10000.00

    UpdateProfileRequest:
      type: object
      properties:
        fullName:
          type: string
          maxLength: 100
        email:
          type: string
          format: email
        address:
          type: string
          maxLength: 200
        creditCard:
          type: string

    ChangePasswordRequest:
      type: object
      required:
        - currentPassword
        - newPassword
      properties:
        currentPassword:
          type: string
          format: password
        newPassword:
          type: string
          format: password
          minLength: 8

    # ========== RESPONSE SCHEMAS ==========
    LoginResponse:
      type: object
      properties:
        accessToken:
          type: string
        refreshToken:
          type: string
        tokenType:
          type: string
          example: "Bearer"
        expiresIn:
          type: integer
          example: 300
        refreshExpiresIn:
          type: integer
          example: 1800
        account:
          $ref: '#/components/schemas/AccountSummary'

    AccountSummary:
      type: object
      properties:
        accountId:
          type: integer
          format: int64
        userId:
          type: string
        fullName:
          type: string
        loginCount:
          type: integer
        lastLogin:
          type: string
          format: date-time

    AccountResponse:
      type: object
      properties:
        id:
          type: integer
          format: int64
        userId:
          type: string
        balance:
          type: number
          format: double
        openBalance:
          type: number
          format: double
        loginCount:
          type: integer
        logoutCount:
          type: integer
        lastLogin:
          type: string
          format: date-time
        creationDate:
          type: string
          format: date-time
        profile:
          $ref: '#/components/schemas/ProfileResponse'

    ProfileResponse:
      type: object
      properties:
        userId:
          type: string
        fullName:
          type: string
        email:
          type: string
        address:
          type: string
        creditCard:
          type: string
          description: Masked credit card (last 4 digits visible)
          example: "****-****-****-1111"
        createdAt:
          type: string
          format: date-time
        updatedAt:
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
          example: "INVALID_CREDENTIALS"
        message:
          type: string
        timestamp:
          type: string
          format: date-time
        path:
          type: string
        traceId:
          type: string

    ValidationErrorResponse:
      type: object
      properties:
        error:
          type: string
          example: "VALIDATION_ERROR"
        message:
          type: string
        timestamp:
          type: string
          format: date-time
        violations:
          type: array
          items:
            type: object
            properties:
              field:
                type: string
                example: "email"
              message:
                type: string
                example: "must be a valid email address"

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
      description: Insufficient permissions
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    NotFound:
      description: Resource not found
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

## Rate Limiting

| Endpoint | Rate Limit | Window |
|----------|------------|--------|
| `POST /auth/login` | 5 requests | 1 minute |
| `POST /auth/refresh` | 30 requests | 1 minute |
| `POST /accounts` (register) | 3 requests | 1 hour |
| `GET /accounts/*` | 60 requests | 1 minute |
| `PUT /profiles/*` | 10 requests | 1 minute |
| `PUT /profiles/me/password` | 3 requests | 1 hour |

---

## Authentication Requirements

| Endpoint | Authentication | Roles |
|----------|----------------|-------|
| `POST /auth/login` | None | - |
| `POST /auth/refresh` | None (uses refresh token) | - |
| `POST /auth/logout` | Bearer token | Any |
| `POST /accounts` | None | - |
| `GET /accounts/me` | Bearer token | `trader`, `admin` |
| `GET /accounts/{id}` | Bearer token | Owner or `admin` |
| `GET /profiles/me` | Bearer token | Any |
| `PUT /profiles/me` | Bearer token | Owner |
| `PUT /profiles/me/password` | Bearer token | Owner |

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_CREDENTIALS` | 401 | Wrong username or password |
| `ACCOUNT_LOCKED` | 423 | Account locked (too many failures) |
| `TOKEN_EXPIRED` | 401 | JWT token has expired |
| `TOKEN_INVALID` | 401 | JWT token is malformed |
| `USER_EXISTS` | 409 | User ID already registered |
| `EMAIL_EXISTS` | 409 | Email already registered |
| `VALIDATION_ERROR` | 422 | Request validation failed |
| `WEAK_PASSWORD` | 422 | Password does not meet requirements |
| `CURRENT_PASSWORD_INCORRECT` | 400 | Wrong current password |
| `NOT_FOUND` | 404 | Account/profile not found |
| `FORBIDDEN` | 403 | Insufficient permissions |

---

## Password Requirements

Passwords must meet the following criteria:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (!@#$%^&*)

---

## Example Requests

### Login

```bash
curl -X POST https://api.daytrader.example.com/account/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "uid:0",
    "password": "xxx"
  }'
```

### Register New Account

```bash
curl -X POST https://api.daytrader.example.com/account/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "newuser",
    "password": "SecureP@ss123",
    "fullName": "John Doe",
    "email": "john@example.com",
    "openBalance": 50000.00
  }'
```

### Update Profile

```bash
curl -X PUT https://api.daytrader.example.com/account/api/profiles/me \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John D. Trader",
    "address": "456 Wall Street, NY"
  }'
```

---

*Document Version: 1.0 | Created: 2026-01-31 | Status: Draft*
