import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import { ReactNode } from 'react';
import * as clientModule from '../api/client';

// Mock the client module
vi.mock('../api/client', () => ({
  getToken: vi.fn(),
  setToken: vi.fn(),
  removeToken: vi.fn(),
}));

const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('useAuth hook', () => {
    it('throws error when used outside AuthProvider', () => {
      // Suppress console.error for this test
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      expect(() => {
        renderHook(() => useAuth());
      }).toThrow('useAuth must be used within an AuthProvider');

      consoleSpy.mockRestore();
    });

    it('returns isAuthenticated as false initially', () => {
      vi.mocked(clientModule.getToken).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.userID).toBeNull();
      expect(result.current.token).toBeNull();
    });

    it('sets isAuthenticated to true when token exists', () => {
      vi.mocked(clientModule.getToken).mockReturnValue('test-token');
      localStorage.setItem('daytrader_user', 'testuser');

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.token).toBe('test-token');
    });

    it('login updates state correctly', () => {
      vi.mocked(clientModule.getToken).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), { wrapper });

      act(() => {
        result.current.login({
          userID: 'testuser',
          token: 'new-token',
          expiresIn: 3600,
        });
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.userID).toBe('testuser');
      expect(result.current.token).toBe('new-token');
    });

    it('logout clears state correctly', () => {
      vi.mocked(clientModule.getToken).mockReturnValue('existing-token');
      localStorage.setItem('daytrader_user', 'testuser');

      const { result } = renderHook(() => useAuth(), { wrapper });

      act(() => {
        result.current.logout();
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.userID).toBeNull();
      expect(result.current.token).toBeNull();
      expect(clientModule.removeToken).toHaveBeenCalled();
    });
  });
});

