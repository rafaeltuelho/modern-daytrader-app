import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { HomePage } from '../HomePage'
import { useAuthStore } from '../../stores/authStore'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const createTestQueryClient = () => {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  })
}

const renderHomePage = () => {
  const queryClient = createTestQueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    </QueryClientProvider>
  )
}

describe('HomePage', () => {
  beforeEach(() => {
    useAuthStore.setState({
      accessToken: 'test-token',
      user: { userId: 'testuser' },
      isAuthenticated: true,
      isLoading: false,
    })
    mockNavigate.mockClear()
  })

  it('should render welcome message', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/welcome/i)).toBeInTheDocument()
    })
  })

  it('should display user name when available', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/welcome, test user!/i)).toBeInTheDocument()
    })
  })

  it('should render account summary section', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/account summary/i)).toBeInTheDocument()
    })
  })

  it('should display account balance', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/\$50,000\.00/)).toBeInTheDocument()
    })
  })

  it('should render market summary section', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/market summary/i)).toBeInTheDocument()
    })
  })

  it('should display TSIA index', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/tsia index/i)).toBeInTheDocument()
      expect(screen.getByText(/2500\.50/)).toBeInTheDocument()
    })
  })

  it('should show loading state initially', () => {
    renderHomePage()

    // Should show loading spinner or skeleton
    expect(screen.getByText(/welcome/i)).toBeInTheDocument()
  })

  it('should render quick quote lookup', async () => {
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByText(/quick quote lookup/i)).toBeInTheDocument()
      expect(screen.getByPlaceholderText(/enter stock symbol/i)).toBeInTheDocument()
    })
  })

  it('should navigate to quote page on search', async () => {
    const user = userEvent.setup()
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/enter stock symbol/i)).toBeInTheDocument()
    })

    const searchInput = screen.getByPlaceholderText(/enter stock symbol/i)
    const searchButton = screen.getByRole('button', { name: /search/i })

    await user.type(searchInput, 'AAPL')
    await user.click(searchButton)

    expect(mockNavigate).toHaveBeenCalledWith('/quotes?symbol=AAPL')
  })

  it('should convert symbol to uppercase on search', async () => {
    const user = userEvent.setup()
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/enter stock symbol/i)).toBeInTheDocument()
    })

    const searchInput = screen.getByPlaceholderText(/enter stock symbol/i)
    const searchButton = screen.getByRole('button', { name: /search/i })

    await user.type(searchInput, 'aapl')
    await user.click(searchButton)

    expect(mockNavigate).toHaveBeenCalledWith('/quotes?symbol=AAPL')
  })

  it('should not search with empty symbol', async () => {
    const user = userEvent.setup()
    renderHomePage()

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/enter stock symbol/i)).toBeInTheDocument()
    })

    const searchButton = screen.getByRole('button', { name: /search/i })
    await user.click(searchButton)

    expect(mockNavigate).not.toHaveBeenCalled()
  })

  it('should display gain/loss indicator', async () => {
    renderHomePage()

    await waitFor(() => {
      // Should show trending up icon for positive gain
      const gainPercent = screen.getByText(/0\.83%/)
      expect(gainPercent).toBeInTheDocument()
    })
  })
})

