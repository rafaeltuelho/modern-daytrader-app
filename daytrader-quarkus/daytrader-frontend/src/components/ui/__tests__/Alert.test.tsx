import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Alert } from '../Alert'

describe('Alert', () => {
  it('should render children', () => {
    render(<Alert>Alert message</Alert>)
    expect(screen.getByText('Alert message')).toBeInTheDocument()
  })

  it('should render with title', () => {
    render(<Alert title="Alert Title">Message</Alert>)
    expect(screen.getByText('Alert Title')).toBeInTheDocument()
    expect(screen.getByText('Message')).toBeInTheDocument()
  })

  it('should apply info variant by default', () => {
    const { container } = render(<Alert>Info message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('bg-blue-50')
    expect(alert.className).toContain('border-blue-200')
    expect(alert.className).toContain('text-blue-800')
  })

  it('should apply success variant', () => {
    const { container } = render(<Alert variant="success">Success message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('bg-green-50')
    expect(alert.className).toContain('border-green-200')
    expect(alert.className).toContain('text-green-800')
  })

  it('should apply warning variant', () => {
    const { container } = render(<Alert variant="warning">Warning message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('bg-yellow-50')
    expect(alert.className).toContain('border-yellow-200')
    expect(alert.className).toContain('text-yellow-800')
  })

  it('should apply error variant', () => {
    const { container } = render(<Alert variant="error">Error message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('bg-red-50')
    expect(alert.className).toContain('border-red-200')
    expect(alert.className).toContain('text-red-800')
  })

  it('should render dismiss button when onDismiss is provided', () => {
    render(<Alert onDismiss={() => {}}>Dismissible alert</Alert>)
    const dismissButton = screen.getByRole('button')
    expect(dismissButton).toBeInTheDocument()
  })

  it('should not render dismiss button when onDismiss is not provided', () => {
    render(<Alert>Non-dismissible alert</Alert>)
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })

  it('should call onDismiss when dismiss button is clicked', async () => {
    const handleDismiss = vi.fn()
    const user = userEvent.setup()
    
    render(<Alert onDismiss={handleDismiss}>Dismissible alert</Alert>)
    
    const dismissButton = screen.getByRole('button')
    await user.click(dismissButton)
    
    expect(handleDismiss).toHaveBeenCalledTimes(1)
  })

  it('should render info icon for info variant', () => {
    const { container } = render(<Alert variant="info">Info</Alert>)
    const icon = container.querySelector('svg')
    expect(icon).toBeInTheDocument()
  })

  it('should render success icon for success variant', () => {
    const { container } = render(<Alert variant="success">Success</Alert>)
    const icon = container.querySelector('svg')
    expect(icon).toBeInTheDocument()
  })

  it('should render warning icon for warning variant', () => {
    const { container } = render(<Alert variant="warning">Warning</Alert>)
    const icon = container.querySelector('svg')
    expect(icon).toBeInTheDocument()
  })

  it('should render error icon for error variant', () => {
    const { container } = render(<Alert variant="error">Error</Alert>)
    const icon = container.querySelector('svg')
    expect(icon).toBeInTheDocument()
  })

  it('should apply custom className', () => {
    const { container } = render(<Alert className="custom-alert">Message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('custom-alert')
  })

  it('should render title with proper styling', () => {
    render(<Alert title="Important">Message</Alert>)
    const title = screen.getByText('Important')
    expect(title.className).toContain('text-sm')
    expect(title.className).toContain('font-medium')
  })

  it('should render complex children', () => {
    render(
      <Alert>
        <div>
          <p>First paragraph</p>
          <p>Second paragraph</p>
        </div>
      </Alert>
    )
    expect(screen.getByText('First paragraph')).toBeInTheDocument()
    expect(screen.getByText('Second paragraph')).toBeInTheDocument()
  })

  it('should have proper border styling', () => {
    const { container } = render(<Alert>Message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('border')
    expect(alert.className).toContain('rounded-md')
  })

  it('should have proper padding', () => {
    const { container } = render(<Alert>Message</Alert>)
    const alert = container.firstChild as HTMLElement
    expect(alert.className).toContain('p-4')
  })
})

