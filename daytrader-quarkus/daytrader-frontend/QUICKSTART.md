# DayTrader Frontend - Quick Start Guide

## Prerequisites

1. **Node.js 18+** and npm installed
2. **Backend services running**:
   - Account Service on port 8080
   - Trading Service on port 8081
   - Market Service on port 8082

## Installation

```bash
cd daytrader-quarkus/daytrader-frontend
npm install
```

## Start Development Server

```bash
npm run dev
```

The application will be available at: **http://localhost:3000**

## First Time Setup

### 1. Register a New Account

1. Navigate to http://localhost:3000
2. You'll be redirected to the login page
3. Click "Register here"
4. Fill in the registration form:
   - **User ID**: Choose a unique ID (e.g., `user123`)
   - **Password**: At least 8 characters
   - **Confirm Password**: Must match password
   - **Full Name**: Your name
   - **Email**: Valid email address
   - **Address**: Optional
   - **Credit Card**: Optional (format: XXXX-XXXX-XXXX-XXXX)
   - **Opening Balance**: Default $10,000 (can be changed)
5. Click "Create Account"
6. You'll be automatically logged in and redirected to the home page

### 2. Explore the Dashboard

After login, you'll see:
- **Account Summary**: Your cash balance and account details
- **Market Summary**: TSIA index, trading volume, market status
- **Top Movers**: Preview of top gainers and losers
- **Quick Quote Lookup**: Search for stock quotes

### 3. View Your Portfolio

1. Click "Portfolio" in the navigation
2. See your portfolio summary (cash, holdings value, total value)
3. View your holdings table (initially empty)
4. Sell holdings by clicking the "Sell" button

### 4. Search and Buy Stocks

1. Click "Quotes" in the navigation
2. Enter stock symbols (e.g., `AAPL, MSFT, GOOGL`)
3. Click "Search"
4. View quote details (price, change, volume, etc.)
5. Enter quantity in the "Quantity" field
6. Click "Buy" to place an order

### 5. View Market Overview

1. Click "Market" in the navigation
2. See the TSIA index and market status
3. Browse top 20 gainers
4. Browse top 20 losers

### 6. Manage Your Account

1. Click "Account" in the navigation
2. View account details
3. Edit your profile (name, email, address, credit card)
4. View order history

## Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code (if configured)
npm run lint
```

## Environment Variables

The application uses these environment variables (configured in `.env.development`):

```env
VITE_ACCOUNT_SERVICE_URL=http://localhost:8080
VITE_TRADING_SERVICE_URL=http://localhost:8081
VITE_MARKET_SERVICE_URL=http://localhost:8082
VITE_API_TIMEOUT=10000
```

## Troubleshooting

### Backend Services Not Running

**Error**: API calls fail with network errors

**Solution**: Ensure all three backend services are running:
```bash
# Check if services are running
curl http://localhost:8080/q/health
curl http://localhost:8081/q/health
curl http://localhost:8082/q/health
```

### CORS Errors

**Error**: CORS policy blocks requests

**Solution**: The Vite dev server proxy should handle this. If issues persist, check backend CORS configuration.

### Token Expired

**Error**: 401 Unauthorized errors

**Solution**: The app automatically refreshes tokens. If issues persist, logout and login again.

### Build Errors

**Error**: TypeScript compilation errors

**Solution**: 
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
npm run build
```

## Key Features

### Authentication
- JWT-based authentication
- Automatic token refresh
- Persistent sessions (localStorage)
- Protected routes

### Real-time Data
- Market data refreshes every 5 seconds
- Top movers refresh every 10 seconds
- Automatic cache invalidation on mutations

### Responsive Design
- Mobile-first approach
- Works on all screen sizes
- Touch-friendly interface

### Form Validation
- Client-side validation with Zod
- Inline error messages
- Type-safe forms

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## Production Deployment

1. Build the application:
   ```bash
   npm run build
   ```

2. The `dist/` folder contains static files ready for deployment

3. Deploy to any static hosting service:
   - Netlify
   - Vercel
   - AWS S3 + CloudFront
   - Azure Static Web Apps
   - GitHub Pages

4. Configure environment variables for production backend URLs

## Support

For issues or questions, refer to:
- `README.md` - Detailed documentation
- `IMPLEMENTATION.md` - Implementation details
- Phase 6 specification: `specs/phase-06-frontend-modernization.md`

## License

Copyright © 2026 DayTrader. All rights reserved.

