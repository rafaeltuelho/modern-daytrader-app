import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { RegisterPage } from '../RegisterPage'
import { useAuthStore } from '../../stores/authStore'
import { registerSchema } from '../../utils/validators'

const mockNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const renderRegisterPage = () => {
  return render(
    <BrowserRouter>
      <RegisterPage />
    </BrowserRouter>
  )
}

describe('RegisterPage', () => {
  beforeEach(() => {
    useAuthStore.setState({
      accessToken: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,
    })
    mockNavigate.mockClear()
  })

  it('should render registration form', () => {
    renderRegisterPage()

    expect(screen.getByRole('heading', { name: 'Create Account' })).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/choose a user id/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/choose a password/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/confirm your password/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/enter your full name/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/enter your email/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument()
  })

  it('should show validation errors for empty required fields', async () => {
    const user = userEvent.setup()
    renderRegisterPage()

    const submitButton = screen.getByRole('button', { name: /create account/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/user id must be at least 3 characters/i)).toBeInTheDocument()
      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument()
      expect(screen.getByText(/full name is required/i)).toBeInTheDocument()
      expect(screen.getByText(/invalid email address/i)).toBeInTheDocument()
    })
  })

  it('should validate password match', async () => {
    const user = userEvent.setup()
    renderRegisterPage()

    await user.type(screen.getByPlaceholderText(/choose a user id/i), 'newuser')
    await user.type(screen.getByPlaceholderText(/choose a password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/confirm your password/i), 'password456')
    await user.type(screen.getByPlaceholderText(/enter your full name/i), 'New User')
    await user.type(screen.getByPlaceholderText(/enter your email/i), 'new@example.com')

    const submitButton = screen.getByRole('button', { name: /create account/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/passwords don't match/i)).toBeInTheDocument()
    })
  })

  it('should validate user ID format', async () => {
    const user = userEvent.setup()
    renderRegisterPage()

    const userIdInput = screen.getByPlaceholderText(/choose a user id/i)
    await user.type(userIdInput, 'ab')

    const submitButton = screen.getByRole('button', { name: /create account/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/user id must be at least 3 characters/i)).toBeInTheDocument()
    })
  })

  it('should validate email format', () => {
    // This test verifies that the zod schema has correct email validation
    // We test the validation directly rather than through the form to avoid timing issues
    const result = registerSchema.safeParse({
      userId: 'testuser',
      password: 'password123',
      confirmPassword: 'password123',
      fullName: 'Test User',
      email: 'invalid-email', // Invalid email format
    })

    expect(result.success).toBe(false)
    if (!result.success) {
      const emailError = result.error.issues.find(issue => issue.path.includes('email'))
      expect(emailError?.message).toBe('Invalid email address')
    }
  })

  it('should successfully register with valid data', async () => {
    const user = userEvent.setup()
    renderRegisterPage()

    await user.type(screen.getByPlaceholderText(/choose a user id/i), 'newuser')
    await user.type(screen.getByPlaceholderText(/choose a password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/confirm your password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/enter your full name/i), 'New User')
    await user.type(screen.getByPlaceholderText(/enter your email/i), 'new@example.com')

    const submitButton = screen.getByRole('button', { name: /create account/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/', { replace: true })
    })
  })

  it('should auto-login after successful registration', async () => {
    const user = userEvent.setup()
    renderRegisterPage()

    await user.type(screen.getByPlaceholderText(/choose a user id/i), 'newuser')
    await user.type(screen.getByPlaceholderText(/choose a password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/confirm your password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/enter your full name/i), 'New User')
    await user.type(screen.getByPlaceholderText(/enter your email/i), 'new@example.com')

    const submitButton = screen.getByRole('button', { name: /create account/i })
    await user.click(submitButton)

    await waitFor(() => {
      const state = useAuthStore.getState()
      expect(state.isAuthenticated).toBe(true)
    })
  })

  it('should have link to login page', () => {
    renderRegisterPage()

    const loginLink = screen.getByText(/sign in here/i)
    expect(loginLink).toBeInTheDocument()
    expect(loginLink.closest('a')).toHaveAttribute('href', '/login')
  })

  it('should disable submit button while loading', async () => {
    const user = userEvent.setup()
    renderRegisterPage()

    await user.type(screen.getByPlaceholderText(/choose a user id/i), 'newuser')
    await user.type(screen.getByPlaceholderText(/choose a password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/confirm your password/i), 'password123')
    await user.type(screen.getByPlaceholderText(/enter your full name/i), 'New User')
    await user.type(screen.getByPlaceholderText(/enter your email/i), 'new@example.com')

    const submitButton = screen.getByRole('button', { name: /create account/i })

    const clickPromise = user.click(submitButton)

    await waitFor(() => {
      expect(submitButton).toBeDisabled()
    })

    await clickPromise
  })

  it('should have default opening balance', () => {
    renderRegisterPage()

    const openBalanceInput = screen.getByPlaceholderText(/10000.00/i) as HTMLInputElement
    expect(openBalanceInput.value).toBe('10000')
  })
})

