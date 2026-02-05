import { create } from 'zustand';
import axios from 'axios';
import type { LoginRequest, LoginResponse } from '../types/account.types';

// Create axios instance for auth without baseURL
// We'll use full URLs in test mode and relative URLs in production
// Check for test environment using process.env.NODE_ENV or vitest globals
const isTest = typeof process !== 'undefined' && process.env.NODE_ENV === 'test';
const authAxios = axios.create();

// Simplified user info stored in auth state
interface UserInfo {
  userId: string;
}

interface AuthState {
  accessToken: string | null;
  user: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  refreshAccessToken: () => Promise<boolean>;
  hydrateFromStorage: () => void;
  setAuth: (accessToken: string, userId: string) => void;
}

const STORAGE_KEY = 'daytrader_auth';

export const useAuthStore = create<AuthState>((set, get) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  isLoading: true,

  setAuth: (accessToken: string, userId: string) => {
    const user: UserInfo = { userId };
    set({
      accessToken,
      user,
      isAuthenticated: true,
      isLoading: false,
    });

    // Store in localStorage for persistence
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ accessToken, user })
    );
  },

  login: async (credentials: LoginRequest) => {
    try {
      const response = await authAxios.post<LoginResponse>('/api/auth/login', credentials);
      const { token, userId } = response.data;

      get().setAuth(token, userId);
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

  logout: () => {
    // Capture token before clearing state
    const token = get().accessToken;

    set({
      accessToken: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,
    });

    // Clear localStorage
    localStorage.removeItem(STORAGE_KEY);

    // Call logout endpoint (fire and forget)
    if (token) {
      authAxios.post('/api/auth/logout', {}, {
        headers: { Authorization: `Bearer ${token}` }
      }).catch(() => {
        // Ignore errors on logout
      });
    }
  },

  // Note: Backend doesn't support token refresh currently
  // This is a placeholder for future implementation
  refreshAccessToken: async () => {
    // For now, we don't have refresh token support
    // Clear auth and return false to trigger re-login
    get().logout();
    return false;
  },

  hydrateFromStorage: () => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const { accessToken, user } = JSON.parse(stored);
        if (accessToken && user) {
          set({
            accessToken,
            user,
            isAuthenticated: true,
            isLoading: false,
          });
        } else {
          set({ isLoading: false });
        }
      } else {
        set({ isLoading: false });
      }
    } catch (error) {
      console.error('Failed to hydrate auth state:', error);
      localStorage.removeItem(STORAGE_KEY);
      set({ isLoading: false });
    }
  },
}));

// Hydrate on app load
if (typeof window !== 'undefined') {
  useAuthStore.getState().hydrateFromStorage();
}

