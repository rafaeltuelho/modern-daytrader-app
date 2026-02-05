import { describe, it, expect, beforeEach, vi, type Mock } from 'vitest'
import axios from 'axios'

// Mock axios before importing authStore
vi.mock('axios', () => {
  const mockAxiosInstance = {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    interceptors: {
      request: { use: vi.fn(), eject: vi.fn() },
      response: { use: vi.fn(), eject: vi.fn() },
    },
  }
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance),
      post: vi.fn(),
      get: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn(), eject: vi.fn() },
        response: { use: vi.fn(), eject: vi.fn() },
      },
    },
  }
})

// Import authStore after mocking axios
import { useAuthStore } from '../authStore'

// Get the mocked axios instance that authStore uses
const mockAxiosInstance = axios.create() as unknown as {
  post: Mock
  get: Mock
  put: Mock
  delete: Mock
}

describe('authStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset store state before each test
    useAuthStore.setState({
      accessToken: null,
      user: null,
      isAuthenticated: false,
      isLoading: true,
    })
    localStorage.clear()
  })

  describe('login', () => {
    it('should successfully login with valid credentials', async () => {
      // Setup mock response
      mockAxiosInstance.post.mockResolvedValueOnce({
        data: { token: 'mock-jwt-token', userId: 'testuser' }
      })

      const { login } = useAuthStore.getState()
      await login({ userId: 'testuser', password: 'password123' })

      const state = useAuthStore.getState()
      expect(state.accessToken).toBe('mock-jwt-token')
      expect(state.user).toEqual({ userId: 'testuser' })
      expect(state.isAuthenticated).toBe(true)
      expect(state.isLoading).toBe(false)
    })

    it('should store auth data in localStorage', async () => {
      // Setup mock response
      mockAxiosInstance.post.mockResolvedValueOnce({
        data: { token: 'mock-jwt-token', userId: 'testuser' }
      })

      const { login } = useAuthStore.getState()
      await login({ userId: 'testuser', password: 'password123' })

      const stored = localStorage.getItem('daytrader_auth')
      expect(stored).toBeTruthy()

      const parsed = JSON.parse(stored!)
      expect(parsed.accessToken).toBe('mock-jwt-token')
      expect(parsed.user.userId).toBe('testuser')
    })

    it('should throw error with invalid credentials', async () => {
      // Setup mock to reject
      mockAxiosInstance.post.mockRejectedValueOnce(new Error('Invalid credentials'))

      const { login } = useAuthStore.getState()

      await expect(
        login({ userId: 'wronguser', password: 'wrongpass' })
      ).rejects.toThrow()

      const state = useAuthStore.getState()
      expect(state.accessToken).toBeNull()
      expect(state.isAuthenticated).toBe(false)
    })
  })

  describe('logout', () => {
    it('should clear auth state on logout', async () => {
      // Setup mock for login
      mockAxiosInstance.post.mockResolvedValueOnce({
        data: { token: 'mock-jwt-token', userId: 'testuser' }
      })

      const { login, logout } = useAuthStore.getState()

      // First login
      await login({ userId: 'testuser', password: 'password123' })
      expect(useAuthStore.getState().isAuthenticated).toBe(true)

      // Setup mock for logout (fire and forget)
      mockAxiosInstance.post.mockResolvedValueOnce({})

      // Then logout
      logout()

      const state = useAuthStore.getState()
      expect(state.accessToken).toBeNull()
      expect(state.user).toBeNull()
      expect(state.isAuthenticated).toBe(false)
    })

    it('should clear localStorage on logout', async () => {
      // Setup mock for login
      mockAxiosInstance.post.mockResolvedValueOnce({
        data: { token: 'mock-jwt-token', userId: 'testuser' }
      })

      const { login, logout } = useAuthStore.getState()

      await login({ userId: 'testuser', password: 'password123' })
      expect(localStorage.getItem('daytrader_auth')).toBeTruthy()

      // Setup mock for logout
      mockAxiosInstance.post.mockResolvedValueOnce({})

      logout()

      expect(localStorage.getItem('daytrader_auth')).toBeNull()
    })

    it('should call logout endpoint', async () => {
      // Setup mock for login
      mockAxiosInstance.post.mockResolvedValueOnce({
        data: { token: 'mock-jwt-token', userId: 'testuser' }
      })

      const { login, logout } = useAuthStore.getState()

      await login({ userId: 'testuser', password: 'password123' })

      // Setup mock for logout
      mockAxiosInstance.post.mockResolvedValueOnce({})

      logout()

      // Wait for async logout call
      await new Promise(resolve => setTimeout(resolve, 100))

      // Second call should be logout
      expect(mockAxiosInstance.post).toHaveBeenCalledWith(
        '/api/auth/logout',
        {},
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer mock-jwt-token'
          })
        })
      )
    })
  })

  describe('setAuth', () => {
    it('should set authentication state', () => {
      const { setAuth } = useAuthStore.getState()

      setAuth('test-token', 'testuser')

      const state = useAuthStore.getState()
      expect(state.accessToken).toBe('test-token')
      expect(state.user).toEqual({ userId: 'testuser' })
      expect(state.isAuthenticated).toBe(true)
      expect(state.isLoading).toBe(false)
    })
  })

  describe('hydrateFromStorage', () => {
    it('should restore auth state from localStorage', () => {
      // Manually set localStorage
      const authData = {
        accessToken: 'stored-token',
        user: { userId: 'storeduser' }
      }
      localStorage.setItem('daytrader_auth', JSON.stringify(authData))

      const { hydrateFromStorage } = useAuthStore.getState()
      hydrateFromStorage()

      const state = useAuthStore.getState()
      expect(state.accessToken).toBe('stored-token')
      expect(state.user).toEqual({ userId: 'storeduser' })
      expect(state.isAuthenticated).toBe(true)
    })

    it('should handle missing localStorage data', () => {
      const { hydrateFromStorage } = useAuthStore.getState()
      hydrateFromStorage()

      const state = useAuthStore.getState()
      expect(state.accessToken).toBeNull()
      expect(state.isAuthenticated).toBe(false)
      expect(state.isLoading).toBe(false)
    })
  })

  describe('isAuthenticated', () => {
    it('should be false initially', () => {
      const state = useAuthStore.getState()
      expect(state.isAuthenticated).toBe(false)
    })

    it('should be true after successful login', async () => {
      // Setup mock response
      mockAxiosInstance.post.mockResolvedValueOnce({
        data: { token: 'mock-jwt-token', userId: 'testuser' }
      })

      const { login } = useAuthStore.getState()
      await login({ userId: 'testuser', password: 'password123' })

      expect(useAuthStore.getState().isAuthenticated).toBe(true)
    })
  })
})

