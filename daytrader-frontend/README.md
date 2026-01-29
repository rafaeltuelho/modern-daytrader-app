# DayTrader7 Frontend

Modern fintech-style React SPA frontend for the DayTrader7 stock trading application.

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Material UI (MUI)** - Component library
- **React Router v6** - Client-side routing
- **React Query** - Server state management
- **React Hook Form + Zod** - Form handling and validation
- **Emotion** - CSS-in-JS styling

## Project Structure

```
src/
  api/           # API client and endpoint modules
  components/    # Reusable UI components
  features/      # Feature modules (auth, portfolio, trading, etc.)
  hooks/         # Custom React hooks
  layouts/       # Layout components (AppShell, etc.)
  pages/         # Route-level page components
  store/         # Context providers (AuthContext)
  types/         # TypeScript type definitions
  utils/         # Utility functions
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- DayTrader7 Quarkus backend running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Copy environment file
cp .env.example .env

# Start development server
npm run dev
```

The app will be available at `http://localhost:3000`

### Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run preview` - Preview production build locally
- `npm run lint` - Run ESLint

## Features

### Phase 1: Foundation (Current)
- ✅ Project setup with Vite + React + TypeScript
- ✅ MUI theme with modern fintech styling
- ✅ Light/dark mode support
- ✅ API client with typed endpoints
- ✅ Authentication (login/register)
- ✅ Protected routes
- ✅ Responsive navigation layout
- ✅ Placeholder pages for all routes

### Phase 2: Core Features (Coming Soon)
- Quote lookup and display
- Dashboard with portfolio summary
- Account profile management

### Phase 3: Trading Features (Coming Soon)
- Portfolio view with holdings
- Buy/sell interface
- Order history

### Phase 4: Real-time & Advanced (Coming Soon)
- Real-time market updates via WebSocket
- Live portfolio updates
- Advanced charting

## API Integration

The frontend connects to the Quarkus backend API at `http://localhost:8080/api/v1`.

### Available Endpoints

- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `POST /auth/register` - User registration
- `GET /quotes` - Get all quotes
- `GET /quotes/{symbol}` - Get quote by symbol
- `GET /portfolio` - Get user holdings
- `POST /orders/buy` - Buy stock
- `POST /orders/sell` - Sell holding
- `GET /orders` - Get user orders
- `GET /market/summary` - Get market summary
- `GET /accounts/profile` - Get account profile
- `PUT /accounts/profile` - Update profile

## Design System

### Colors

- **Primary**: Deep blue (#1e3a8a) - Professional, trustworthy
- **Success**: Green (#10b981) - Gains, positive values
- **Error**: Red (#ef4444) - Losses, negative values
- **Background**: Light gray (#f8fafc) / Dark slate (#0f172a)

### Typography

- Font family: Inter, Roboto, Helvetica, Arial
- Headings: Bold (600-700 weight)
- Body: Regular (400 weight)

### Components

All components use Material UI with custom theme overrides for a modern fintech aesthetic.

## Development Guidelines

1. **Type Safety**: Use TypeScript for all new code
2. **API Calls**: Use React Query for data fetching
3. **Forms**: Use React Hook Form + Zod for validation
4. **Styling**: Use MUI components and theme system
5. **State**: Use React Query for server state, Context for auth/UI state
6. **Routing**: Use React Router v6 with protected routes

## Environment Variables

Create a `.env` file based on `.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_BASE_URL=ws://localhost:8080/ws
```

## License

Same as DayTrader7 parent project

