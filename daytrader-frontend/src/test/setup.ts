import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, vi } from 'vitest';

// Clean up after each test
afterEach(() => {
  cleanup();
});

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
  };
})();

beforeAll(() => {
  Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
  });
});

// Reset localStorage mock before each test
afterEach(() => {
  localStorageMock.clear();
  vi.clearAllMocks();
});

// Mock window.location
const locationMock = {
  pathname: '/',
  href: 'http://localhost:3000/',
  assign: vi.fn(),
  replace: vi.fn(),
  reload: vi.fn(),
};

Object.defineProperty(window, 'location', {
  value: locationMock,
  writable: true,
});

