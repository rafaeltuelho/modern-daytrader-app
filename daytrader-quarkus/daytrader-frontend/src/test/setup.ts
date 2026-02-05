// Setup localStorage mock FIRST before any other imports
// This is critical because authStore runs initialization on module load
import { vi, afterEach, beforeAll, afterAll, beforeEach } from 'vitest'

// Create a complete localStorage mock
class LocalStorageMock {
  private store: Record<string, string> = {}

  getItem(key: string): string | null {
    return this.store[key] ?? null
  }

  setItem(key: string, value: string): void {
    this.store[key] = String(value)
  }

  removeItem(key: string): void {
    delete this.store[key]
  }

  clear(): void {
    this.store = {}
  }

  get length(): number {
    return Object.keys(this.store).length
  }

  key(index: number): string | null {
    return Object.keys(this.store)[index] ?? null
  }
}

// Stub localStorage globally before any modules load
const localStorageMock = new LocalStorageMock()
vi.stubGlobal('localStorage', localStorageMock)

// Mock window.location BEFORE any other imports
// This is needed for axios and MSW to work properly in jsdom
if (typeof window !== 'undefined') {
  // Save original location
  const originalLocation = window.location

  // Create proper location object with full URL support
  delete (window as any).location
  window.location = {
    href: 'http://localhost:3000/',
    pathname: '/',
    search: '',
    hash: '',
    origin: 'http://localhost:3000',
    protocol: 'http:',
    host: 'localhost:3000',
    hostname: 'localhost',
    port: '3000',
    assign: vi.fn(),
    reload: vi.fn(),
    replace: vi.fn(),
    toString: () => 'http://localhost:3000/',
  } as any
}

// Now import other dependencies
import '@testing-library/jest-dom'
import { cleanup } from '@testing-library/react'
import { server } from './mocks/server'

// Start MSW server before all tests
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'warn' })
})

// Reset state before each test
beforeEach(() => {
  localStorageMock.clear()
  // Reset window.location.href
  if (typeof window !== 'undefined') {
    window.location.href = 'http://localhost:3000/'
  }
})

// Reset handlers after each test
afterEach(() => {
  server.resetHandlers()
  cleanup()
  localStorageMock.clear()
})

// Stop MSW server after all tests
afterAll(() => server.close())

// Mock matchMedia
if (typeof window !== 'undefined') {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation(query => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  })
}

