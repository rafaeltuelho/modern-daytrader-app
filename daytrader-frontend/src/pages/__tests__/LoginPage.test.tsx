import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../../__tests__/test-utils';
import userEvent from '@testing-library/user-event';
import { LoginPage } from '../LoginPage';
import * as AuthContext from '../../store/AuthContext';

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({ state: null }),
  };
});

// Mock useAuth
vi.mock('../../store/AuthContext', async () => {
  const actual = await vi.importActual('../../store/AuthContext');
  return {
    ...actual,
    useAuth: vi.fn(),
  };
});

describe('LoginPage', () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: mockLogin,
      logout: vi.fn(),
      register: vi.fn(),
    });
  });

  it('renders login form', () => {
    render(<LoginPage />);

    expect(screen.getByText('DayTrader')).toBeInTheDocument();
    // "Sign In" appears as both heading and button, use getAllByText
    expect(screen.getAllByText('Sign In').length).toBeGreaterThan(0);
    expect(screen.getByLabelText(/user id/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('renders register link', () => {
    render(<LoginPage />);
    const registerLink = screen.getByText(/register here/i);
    expect(registerLink).toBeInTheDocument();
    expect(registerLink.closest('a')).toHaveAttribute('href', '/register');
  });

  it('updates userID field on input', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);
    
    const userIDInput = screen.getByLabelText(/user id/i);
    await user.type(userIDInput, 'testuser');
    
    expect(userIDInput).toHaveValue('testuser');
  });

  it('updates password field on input', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);
    
    const passwordInput = screen.getByLabelText(/password/i);
    await user.type(passwordInput, 'password123');
    
    expect(passwordInput).toHaveValue('password123');
  });

  it('calls login function on form submit', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/user id/i), 'testuser');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('testuser', 'password123');
    });
  });

  it('navigates to home page on successful login', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/user id/i), 'testuser');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true });
    });
  });

  it('displays error message on login failure', async () => {
    const user = userEvent.setup();
    mockLogin.mockRejectedValue(new Error('Invalid credentials'));
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/user id/i), 'testuser');
    await user.type(screen.getByLabelText(/password/i), 'wrongpassword');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });
  });

  it('disables form fields while loading', async () => {
    const user = userEvent.setup();
    mockLogin.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 1000)));
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/user id/i), 'testuser');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByLabelText(/user id/i)).toBeDisabled();
      expect(screen.getByLabelText(/password/i)).toBeDisabled();
      expect(submitButton).toBeDisabled();
      expect(screen.getByText('Signing in...')).toBeInTheDocument();
    });
  });

  it('requires both fields to be filled', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);
    
    const userIDInput = screen.getByLabelText(/user id/i);
    const passwordInput = screen.getByLabelText(/password/i);
    
    expect(userIDInput).toBeRequired();
    expect(passwordInput).toBeRequired();
  });
});

