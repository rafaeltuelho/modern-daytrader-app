import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { LoginPage } from '../LoginPage'
import { useAuthStore } from '../../stores/authStore'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const renderLoginPage = () => {
  return render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    useAuthStore.setState({
      accessToken: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,
    })
    mockNavigate.mockClear()
  })

  it('should render login form', () => {
    renderLoginPage()

    expect(screen.getByRole('heading', { name: 'Sign In' })).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/enter your user id/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/enter your password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('should show validation errors for empty fields', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    const submitButton = screen.getByRole('button', { name: /sign in/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/user id is required/i)).toBeInTheDocument()
      expect(screen.getByText(/password is required/i)).toBeInTheDocument()
    })
  })

  it('should submit form with valid credentials', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    const userIdInput = screen.getByPlaceholderText(/enter your user id/i)
    const passwordInput = screen.getByPlaceholderText(/enter your password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    await user.type(userIdInput, 'testuser')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true })
    })
  })

  it('should show error message on login failure', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    const userIdInput = screen.getByPlaceholderText(/enter your user id/i)
    const passwordInput = screen.getByPlaceholderText(/enter your password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    await user.type(userIdInput, 'wronguser')
    await user.type(passwordInput, 'wrongpass')
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument()
    })
  })

  it('should disable submit button while loading', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    const userIdInput = screen.getByPlaceholderText(/enter your user id/i)
    const passwordInput = screen.getByPlaceholderText(/enter your password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    await user.type(userIdInput, 'testuser')
    await user.type(passwordInput, 'password123')

    // Click and immediately check if button is disabled
    const clickPromise = user.click(submitButton)

    // Button should be disabled during submission
    await waitFor(() => {
      expect(submitButton).toBeDisabled()
    })

    await clickPromise
  })

  it('should update auth store on successful login', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    const userIdInput = screen.getByPlaceholderText(/enter your user id/i)
    const passwordInput = screen.getByPlaceholderText(/enter your password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    await user.type(userIdInput, 'testuser')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)

    await waitFor(() => {
      const state = useAuthStore.getState()
      expect(state.isAuthenticated).toBe(true)
      expect(state.user?.userId).toBe('testuser')
    })
  })

  it('should have link to register page', () => {
    renderLoginPage()

    const registerLink = screen.getByText(/register here/i)
    expect(registerLink).toBeInTheDocument()
    expect(registerLink.closest('a')).toHaveAttribute('href', '/register')
  })

  it('should clear error message on new submission', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    const userIdInput = screen.getByPlaceholderText(/enter your user id/i)
    const passwordInput = screen.getByPlaceholderText(/enter your password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    // First attempt with wrong credentials
    await user.type(userIdInput, 'wronguser')
    await user.type(passwordInput, 'wrongpass')
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument()
    })

    // Clear and try again
    await user.clear(userIdInput)
    await user.clear(passwordInput)
    await user.type(userIdInput, 'testuser')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)

    // Error should be cleared
    await waitFor(() => {
      expect(screen.queryByText(/invalid credentials/i)).not.toBeInTheDocument()
    })
  })
})

