import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useQuote } from '../useQuotes';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('useQuote', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('fetches quote for a symbol successfully', async () => {
    const { result } = renderHook(() => useQuote('AAPL'), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
    expect(result.current.data).toMatchObject({
      symbol: 'AAPL',
      companyName: expect.any(String),
      price: expect.any(Number),
      volume: expect.any(Number),
    });
  });

  it('does not fetch when symbol is empty', () => {
    const { result } = renderHook(() => useQuote(''), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.data).toBeUndefined();
  });

  it('handles loading state', () => {
    const { result } = renderHook(() => useQuote('AAPL'), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();
  });

  it('fetches different symbols independently', async () => {
    const { result: result1 } = renderHook(() => useQuote('AAPL'), {
      wrapper: createWrapper(),
    });

    const { result: result2 } = renderHook(() => useQuote('GOOGL'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result1.current.isSuccess).toBe(true);
      expect(result2.current.isSuccess).toBe(true);
    });

    expect(result1.current.data?.symbol).toBe('AAPL');
    expect(result2.current.data?.symbol).toBe('GOOGL');
  });
});

