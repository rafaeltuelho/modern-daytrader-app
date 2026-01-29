import { ReactElement, ReactNode } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider } from '../store/AuthContext';
import { lightTheme } from '../theme';
import type { User } from '../types';

// Create a custom render function that includes all providers
interface AllTheProvidersProps {
  children: ReactNode;
}

const AllTheProviders = ({ children }: AllTheProvidersProps) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  });

  return (
    <BrowserRouter>
      <ThemeProvider theme={lightTheme}>
        <CssBaseline />
        <QueryClientProvider client={queryClient}>
          <AuthProvider>{children}</AuthProvider>
        </QueryClientProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => render(ui, { wrapper: AllTheProviders, ...options });

// Re-export everything
export * from '@testing-library/react';
export { customRender as render };

// Mock user data for tests
export const mockUser: User = {
  accountID: 1,
  profileID: 'testuser',
  loginCount: 5,
  logoutCount: 4,
  lastLogin: '2024-01-15T10:30:00Z',
  creationDate: '2024-01-01T00:00:00Z',
  balance: 10000.0,
  openBalance: 10000.0,
};

// Helper to mock authenticated state
export const mockAuthToken = (token: string = 'mock-jwt-token') => {
  sessionStorage.setItem('auth_token', token);
};

// Helper to clear auth
export const clearAuth = () => {
  sessionStorage.removeItem('auth_token');
};

// Helper to wait for loading states to complete
export const waitForLoadingToFinish = () => {
  return new Promise((resolve) => setTimeout(resolve, 0));
};

