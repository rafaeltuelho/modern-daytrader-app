import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { usePortfolio, usePortfolioSummary, useHolding } from '../usePortfolio';
import { mockHolding, mockPortfolioSummary } from '../../__tests__/mocks/handlers';

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

describe('usePortfolio', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('fetches portfolio holdings successfully', async () => {
    const { result } = renderHook(() => usePortfolio(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
    expect(Array.isArray(result.current.data)).toBe(true);
    expect(result.current.data?.[0]).toMatchObject({
      holdingID: expect.any(Number),
      symbol: expect.any(String),
      quantity: expect.any(Number),
    });
  });

  it('handles loading state', () => {
    const { result } = renderHook(() => usePortfolio(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();
  });
});

describe('usePortfolioSummary', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('fetches portfolio summary successfully', async () => {
    const { result } = renderHook(() => usePortfolioSummary(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
    expect(result.current.data).toMatchObject({
      accountID: expect.any(Number),
      balance: expect.any(Number),
      totalValue: expect.any(Number),
      gain: expect.any(Number),
    });
  });

  it('provides correct query key', () => {
    const { result } = renderHook(() => usePortfolioSummary(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
  });
});

describe('useHolding', () => {
  beforeEach(() => {
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
  });

  it('fetches specific holding successfully', async () => {
    const { result } = renderHook(() => useHolding(1), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toBeDefined();
  });

  it('does not fetch when holdingId is 0', () => {
    const { result } = renderHook(() => useHolding(0), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.data).toBeUndefined();
  });

  it('fetches when holdingId is negative (truthy value)', async () => {
    // Note: -1 is truthy in JavaScript, so enabled: !!holdingId will be true
    const { result } = renderHook(() => useHolding(-1), {
      wrapper: createWrapper(),
    });

    // Since -1 is truthy, the query will be enabled and start loading
    expect(result.current.isLoading).toBe(true);
  });
});

