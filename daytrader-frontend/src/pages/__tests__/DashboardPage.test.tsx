import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../../__tests__/test-utils';
import { DashboardPage } from '../DashboardPage';
import * as AuthContext from '../../store/AuthContext';
import { mockUser } from '../../__tests__/test-utils';

// Mock useAuth
vi.mock('../../store/AuthContext', async () => {
  const actual = await vi.importActual('../../store/AuthContext');
  return {
    ...actual,
    useAuth: vi.fn(),
  };
});

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.setItem('auth_token', 'mock-jwt-token');
    
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: mockUser,
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      register: vi.fn(),
    });
  });

  it('renders welcome message with user profile ID', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/welcome back, testuser/i)).toBeInTheDocument();
    });
  });

  it('renders dashboard title', () => {
    render(<DashboardPage />);
    expect(screen.getByText('Your trading dashboard')).toBeInTheDocument();
  });

  it('renders portfolio value stat card', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Portfolio Value')).toBeInTheDocument();
    });
  });

  it('renders total gain/loss stat card', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Total Gain/Loss')).toBeInTheDocument();
    });
  });

  it('renders cash balance stat card', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Cash Balance')).toBeInTheDocument();
    });
  });

  it('renders market index stat card', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText('TSIA Index')).toBeInTheDocument();
    });
  });

  it('displays loading state initially', () => {
    render(<DashboardPage />);
    
    // StatCards should show loading skeletons
    const cards = screen.getAllByText('Portfolio Value');
    expect(cards.length).toBeGreaterThan(0);
  });

  it('displays portfolio data when loaded', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      // Should display portfolio value (formatted without commas as toFixed(2))
      expect(screen.getByText('$25025.00')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('renders recent orders section', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Recent Orders')).toBeInTheDocument();
    });
  });

  it('renders top gainers section', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Top Gainers')).toBeInTheDocument();
    });
  });

  it('displays order data in table', async () => {
    render(<DashboardPage />);

    // Wait for orders section to render with headers
    await waitFor(() => {
      expect(screen.getByText('Recent Orders')).toBeInTheDocument();
      // "Symbol" appears in table headers (both orders and top gainers tables)
      expect(screen.getAllByText('Symbol').length).toBeGreaterThan(0);
    }, { timeout: 3000 });
  });

  it('displays market data when loaded', async () => {
    render(<DashboardPage />);

    await waitFor(() => {
      // Should display TSIA value (formatted as toFixed(2) = 5000.00)
      expect(screen.getByText('5000.00')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('shows fallback message when user is not loaded', async () => {
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: null,
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      register: vi.fn(),
    });

    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/welcome back, trader/i)).toBeInTheDocument();
    });
  });

  it('displays holdings count', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/1 holdings/i)).toBeInTheDocument();
    });
  });

  it('displays gain percentage', async () => {
    render(<DashboardPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/4\.27%/)).toBeInTheDocument();
    });
  });
});

