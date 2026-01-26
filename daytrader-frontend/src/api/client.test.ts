import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getToken, setToken, removeToken } from './client';

describe('API Client', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('getToken', () => {
    it('returns null when no token is stored', () => {
      expect(getToken()).toBeNull();
    });

    it('returns stored token', () => {
      localStorage.setItem('daytrader_token', 'test-token-123');
      expect(getToken()).toBe('test-token-123');
    });
  });

  describe('setToken', () => {
    it('stores token in localStorage', () => {
      setToken('new-token-456');
      expect(localStorage.getItem('daytrader_token')).toBe('new-token-456');
    });

    it('overwrites existing token', () => {
      localStorage.setItem('daytrader_token', 'old-token');
      setToken('new-token');
      expect(localStorage.getItem('daytrader_token')).toBe('new-token');
    });
  });

  describe('removeToken', () => {
    it('removes token from localStorage', () => {
      localStorage.setItem('daytrader_token', 'token-to-remove');
      removeToken();
      expect(localStorage.getItem('daytrader_token')).toBeNull();
    });

    it('does nothing when no token exists', () => {
      expect(() => removeToken()).not.toThrow();
      expect(localStorage.getItem('daytrader_token')).toBeNull();
    });
  });
});

