import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useOrders, useBuy, useSell } from '../useOrders';

const createWrapper = () => {
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

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('useOrders', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('fetches orders successfully', async () => {
    const { result } = renderHook(() => useOrders(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
    expect(Array.isArray(result.current.data)).toBe(true);
    expect(result.current.data?.[0]).toMatchObject({
      orderID: expect.any(Number),
      orderType: expect.any(String),
      symbol: expect.any(String),
    });
  });

  it('handles loading state', () => {
    const { result } = renderHook(() => useOrders(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();
  });
});

describe('useBuy', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('executes buy mutation successfully', async () => {
    const { result } = renderHook(() => useBuy(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isPending).toBe(false);

    result.current.mutate({ symbol: 'AAPL', quantity: 10 });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
  });

  it('handles mutation loading state', () => {
    const { result } = renderHook(() => useBuy(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isPending).toBe(false);
    expect(result.current.data).toBeUndefined();
  });

  it('accepts buy request parameters', async () => {
    const { result } = renderHook(() => useBuy(), {
      wrapper: createWrapper(),
    });

    const buyRequest = { symbol: 'AAPL', quantity: 100 };
    result.current.mutate(buyRequest);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
  });
});

describe('useSell', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('executes sell mutation successfully', async () => {
    const { result } = renderHook(() => useSell(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isPending).toBe(false);

    result.current.mutate({ holdingID: 1 });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
  });

  it('handles mutation loading state', () => {
    const { result } = renderHook(() => useSell(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isPending).toBe(false);
    expect(result.current.data).toBeUndefined();
  });

  it('accepts sell request parameters', async () => {
    const { result } = renderHook(() => useSell(), {
      wrapper: createWrapper(),
    });

    const sellRequest = { holdingID: 1 };
    result.current.mutate(sellRequest);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
  });
});

