import { http, HttpResponse } from 'msw'

// Base URL for test API - matches the apiClient configuration
const API_BASE = 'http://localhost:3000/api'

export const handlers = [
  // Auth endpoints
  http.post(`${API_BASE}/auth/login`, async ({ request }) => {
    const body = await request.json() as { userId: string; password: string }

    // Accept testuser/password123 or newuser/password123 for testing
    if ((body.userId === 'testuser' || body.userId === 'newuser') && body.password === 'password123') {
      return HttpResponse.json({
        token: 'mock-jwt-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        userId: body.userId,
      })
    }

    return HttpResponse.json(
      { message: 'Invalid credentials' },
      { status: 401 }
    )
  }),

  http.post(`${API_BASE}/auth/logout`, () => {
    return HttpResponse.json({ message: 'Logged out successfully' })
  }),

  // Account endpoints
  http.post(`${API_BASE}/accounts`, async ({ request }) => {
    const body = await request.json() as any
    return HttpResponse.json({
      id: 1,
      userId: body.userId,
      balance: body.openBalance || 10000,
      openBalance: body.openBalance || 10000,
      loginCount: 0,
      logoutCount: 0,
      lastLogin: new Date().toISOString(),
      creationDate: new Date().toISOString(),
      profile: {
        userId: body.userId,
        fullName: body.fullName,
        email: body.email,
        address: body.address || '',
        creditCard: body.creditCard || '',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    })
  }),

  http.get(`${API_BASE}/accounts/me`, ({ request }) => {
    const authHeader = request.headers.get('Authorization')

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return HttpResponse.json(
        { message: 'Unauthorized' },
        { status: 401 }
      )
    }

    return HttpResponse.json({
      id: 1,
      userId: 'testuser',
      balance: 50000,
      openBalance: 10000,
      loginCount: 5,
      logoutCount: 4,
      lastLogin: new Date().toISOString(),
      creationDate: new Date().toISOString(),
      profile: {
        userId: 'testuser',
        fullName: 'Test User',
        email: 'test@example.com',
        address: '123 Test St',
        creditCard: '1234-5678-9012-3456',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    })
  }),

  // Market endpoints
  http.get(`${API_BASE}/market/summary`, () => {
    return HttpResponse.json({
      tsia: 2500.50,
      openTsia: 2480.00,
      volume: 1500000,
      gainPercent: 0.83,
      topGainers: [],
      topLosers: [],
    })
  }),

  http.get(`${API_BASE}/market/gainers`, () => {
    return HttpResponse.json([])
  }),

  http.get(`${API_BASE}/market/losers`, () => {
    return HttpResponse.json([])
  }),

  // Holdings endpoints - backend now returns array directly (not paginated)
  http.get(`${API_BASE}/holdings`, () => {
    return HttpResponse.json([
      {
        id: 1,
        symbol: 'AAPL',
        companyName: 'Apple Inc.',
        quantity: 10,
        purchasePrice: 150.00,
        currentPrice: 175.00,
        purchaseValue: 1500.00,
        currentValue: 1750.00,
        gain: 250.00,
        gainPercent: 16.67,
        purchaseDate: new Date().toISOString(),
      },
    ])
  }),

  // Portfolio endpoints
  http.get(`${API_BASE}/portfolio/summary`, () => {
    return HttpResponse.json({
      cashBalance: 50000,
      holdingsValue: 1750,
      totalValue: 51750,
      totalGain: 250,
      totalGainPercent: 0.48,
    })
  }),

  // Profile endpoints
  http.get(`${API_BASE}/profiles/me`, () => {
    return HttpResponse.json({
      userId: 'testuser',
      fullName: 'Test User',
      email: 'test@example.com',
      address: '123 Test St',
      creditCard: '1234-5678-9012-3456',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
  }),

  http.put(`${API_BASE}/profiles/me`, async ({ request }) => {
    const body = await request.json() as any
    return HttpResponse.json({
      userId: 'testuser',
      fullName: body.fullName || 'Test User',
      email: body.email || 'test@example.com',
      address: body.address || '123 Test St',
      creditCard: body.creditCard || '1234-5678-9012-3456',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
  }),

  // Orders endpoints
  http.get(`${API_BASE}/orders`, () => {
    return HttpResponse.json([
      {
        id: 1,
        quantity: 10,
        price: 175.00,
        orderType: 'buy',
        orderStatus: 'completed',
        openDate: new Date().toISOString(),
        completionDate: new Date().toISOString(),
        orderFee: 9.95,
        accountId: 1,
        symbol: 'AAPL',
        holdingId: 1,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ])
  }),
]

