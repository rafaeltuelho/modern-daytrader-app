import { ReactElement, ReactNode } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../context/AuthContext';

// Create a fresh QueryClient for each test
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

interface WrapperProps {
  children: ReactNode;
}

// Wrapper with all providers
function AllTheProviders({ children }: WrapperProps) {
  const queryClient = createTestQueryClient();
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>{children}</AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

// Wrapper without AuthProvider (for testing AuthContext itself)
function ProvidersWithoutAuth({ children }: WrapperProps) {
  const queryClient = createTestQueryClient();
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  );
}

// Custom render function that wraps components with providers
function customRender(
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'> & { withAuth?: boolean }
) {
  const { withAuth = true, ...renderOptions } = options || {};
  const Wrapper = withAuth ? AllTheProviders : ProvidersWithoutAuth;
  return render(ui, { wrapper: Wrapper, ...renderOptions });
}

// Re-export everything from testing-library
export * from '@testing-library/react';
export { customRender as render };

