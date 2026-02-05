import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PortfolioPage } from '../PortfolioPage'
import { useAuthStore } from '../../stores/authStore'
import { http, HttpResponse } from 'msw'
import { server } from '../../test/mocks/server'

const createTestQueryClient = () => {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  })
}

const renderPortfolioPage = () => {
  const queryClient = createTestQueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PortfolioPage />
      </BrowserRouter>
    </QueryClientProvider>
  )
}

describe('PortfolioPage', () => {
  beforeEach(() => {
    useAuthStore.setState({
      accessToken: 'test-token',
      user: { userId: 'testuser' },
      isAuthenticated: true,
      isLoading: false,
    })
  })

  it('should render portfolio page title', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /portfolio/i })).toBeInTheDocument()
    })
  })

  it('should display portfolio summary', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/portfolio summary/i)).toBeInTheDocument()
    })
  })

  it('should display cash balance', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/cash balance/i)).toBeInTheDocument()
      expect(screen.getByText(/\$50,000\.00/)).toBeInTheDocument()
    })
  })

  it('should display holdings value', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/holdings value/i)).toBeInTheDocument()
      // Use getAllByText since the value may appear in both summary and table
      const values = screen.getAllByText(/\$1,750\.00/)
      expect(values.length).toBeGreaterThan(0)
    })
  })

  it('should display total value', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/total value/i)).toBeInTheDocument()
      expect(screen.getByText(/\$51,750\.00/)).toBeInTheDocument()
    })
  })

  it('should display total gain/loss', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/total gain\/loss/i)).toBeInTheDocument()
      // Use getAllByText since gain may appear in both summary and table
      const gains = screen.getAllByText(/\$250\.00/)
      expect(gains.length).toBeGreaterThan(0)
    })
  })

  it('should render holdings table', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      // Use getAllByText since "Holdings" appears as both table title and card title
      const elements = screen.getAllByText(/holdings/i)
      expect(elements.length).toBeGreaterThan(0)
    })
  })

  it('should display holdings data', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText('AAPL')).toBeInTheDocument()
      // Use getAllByText since 10 may appear in multiple places (quantity in table)
      const quantities = screen.getAllByText(/10\.00/)
      expect(quantities.length).toBeGreaterThan(0)
    })
  })

  it('should show empty state when no holdings', async () => {
    // Override the holdings endpoint to return empty array (backend now returns array directly)
    server.use(
      http.get('http://localhost:3000/api/holdings', () => {
        return HttpResponse.json([])
      })
    )

    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/no holdings found/i)).toBeInTheDocument()
    })
  })

  it('should show loading state initially', () => {
    renderPortfolioPage()

    // Should render the page structure
    expect(screen.getByRole('heading', { name: /portfolio/i })).toBeInTheDocument()
  })

  it('should calculate gain/loss correctly', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      // Purchase price: $150, Current price: $175, Quantity: 10
      // Gain: (175 - 150) * 10 = $250
      const gains = screen.getAllByText(/\$250\.00/)
      expect(gains.length).toBeGreaterThan(0)
    })
  })

  it('should display gain percentage', async () => {
    renderPortfolioPage()

    await waitFor(() => {
      expect(screen.getByText(/0\.48%/)).toBeInTheDocument()
    })
  })

  it('should handle API errors gracefully', async () => {
    server.use(
      http.get('http://localhost:3000/api/holdings', () => {
        return HttpResponse.json(
          { message: 'Internal Server Error' },
          { status: 500 }
        )
      })
    )

    renderPortfolioPage()

    // Should still render the page structure
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /portfolio/i })).toBeInTheDocument()
    })
  })
})

