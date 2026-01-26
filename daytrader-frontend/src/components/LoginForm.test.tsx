import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '../test/test-utils';
import { LoginForm } from './LoginForm';

describe('LoginForm', () => {
  const mockOnSubmit = vi.fn();

  beforeEach(() => {
    mockOnSubmit.mockClear();
  });

  it('renders login form with all fields', () => {
    render(<LoginForm onSubmit={mockOnSubmit} />);

    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('checkbox', { name: /remember me/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('renders welcome message', () => {
    render(<LoginForm onSubmit={mockOnSubmit} />);

    expect(screen.getByText(/welcome back/i)).toBeInTheDocument();
    expect(screen.getByText(/sign in to continue trading/i)).toBeInTheDocument();
  });

  it('displays error message when error prop is provided', () => {
    render(<LoginForm onSubmit={mockOnSubmit} error="Invalid credentials" />);

    expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
  });

  it('shows loading state when isLoading is true', () => {
    render(<LoginForm onSubmit={mockOnSubmit} isLoading={true} />);

    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled();
  });

  it('calls onSubmit with form data when submitted', async () => {
    mockOnSubmit.mockResolvedValue(undefined);
    render(<LoginForm onSubmit={mockOnSubmit} />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith('testuser', 'password123', false);
    });
  });

  it('calls onSubmit with rememberMe checked', async () => {
    mockOnSubmit.mockResolvedValue(undefined);
    render(<LoginForm onSubmit={mockOnSubmit} />);

    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const rememberMeCheckbox = screen.getByRole('checkbox', { name: /remember me/i });
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(rememberMeCheckbox);
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith('testuser', 'password123', true);
    });
  });

  it('has link to registration page', () => {
    render(<LoginForm onSubmit={mockOnSubmit} />);

    const registerLink = screen.getByRole('link', { name: /get started/i });
    expect(registerLink).toHaveAttribute('href', '/register');
  });

  it('updates username input value on change', () => {
    render(<LoginForm onSubmit={mockOnSubmit} />);

    const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
    fireEvent.change(usernameInput, { target: { value: 'newuser' } });

    expect(usernameInput.value).toBe('newuser');
  });

  it('updates password input value on change', () => {
    render(<LoginForm onSubmit={mockOnSubmit} />);

    const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;
    fireEvent.change(passwordInput, { target: { value: 'secret' } });

    expect(passwordInput.value).toBe('secret');
  });
});

