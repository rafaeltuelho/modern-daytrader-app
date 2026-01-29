# DayTrader Frontend - Quick Start Guide

## Prerequisites

1. **Node.js 18+** installed
2. **DayTrader Quarkus backend** running on `http://localhost:8080`

## Installation & Setup

```bash
# Navigate to frontend directory
cd daytrader-frontend

# Install dependencies (if not already done)
npm install

# Start development server
npm run dev
```

The application will be available at **http://localhost:3000**

## First Time Setup

1. **Start the backend**: Ensure the Quarkus backend is running on port 8080
2. **Start the frontend**: Run `npm run dev` in the daytrader-frontend directory
3. **Open browser**: Navigate to http://localhost:3000
4. **Register**: Click "Register here" and create a new account
5. **Login**: Use your credentials to log in
6. **Explore**: Navigate through the app using the sidebar

## Available Commands

```bash
# Development
npm run dev          # Start dev server with hot reload (port 3000)

# Production
npm run build        # Build for production
npm run preview      # Preview production build

# Code Quality
npm run lint         # Run ESLint
```

## Application Structure

### Public Routes
- `/login` - User login
- `/register` - New user registration

### Protected Routes (require authentication)
- `/` - Dashboard with portfolio summary
- `/portfolio` - Holdings and positions
- `/trade` - Buy/sell interface
- `/quotes` - Stock quote lookup
- `/orders` - Order history
- `/market` - Market summary
- `/account` - Account settings

## Features Available in Phase 1

✅ **Authentication**
- User login with validation
- User registration
- Secure token management
- Protected routes

✅ **Navigation**
- Responsive sidebar navigation
- Mobile-friendly drawer
- User menu with logout
- Light/dark mode toggle

✅ **Layout**
- Modern fintech design
- Professional color scheme
- Responsive design
- Accessible components

✅ **Dashboard**
- Welcome message
- Portfolio value card (placeholder)
- Gain/loss card (placeholder)
- Cash balance display

## Coming in Future Phases

🔜 **Phase 2** (Core Features)
- Quote lookup and display
- Real portfolio data
- Account profile management

🔜 **Phase 3** (Trading)
- Portfolio holdings table
- Buy/sell trading
- Order history with filters

🔜 **Phase 4** (Real-time)
- WebSocket market updates
- Live portfolio updates
- Advanced charting

## Troubleshooting

### Backend Connection Issues
- Ensure Quarkus backend is running on port 8080
- Check CORS configuration on backend
- Verify API endpoints are accessible

### Build Errors
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Port Already in Use
```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9

# Or change port in vite.config.ts
```

## Environment Variables

Create a `.env` file (already provided):
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_BASE_URL=ws://localhost:8080/ws
```

## Development Tips

1. **Hot Reload**: Changes to source files automatically reload the browser
2. **TypeScript**: All files use TypeScript for type safety
3. **MUI Components**: Use Material UI components for consistency
4. **React Query**: Ready for data fetching in future phases
5. **Form Validation**: React Hook Form + Zod ready for complex forms

## Support

For issues or questions:
1. Check the main README.md
2. Review IMPLEMENTATION.md for technical details
3. Refer to the specification documents in `/specs`

