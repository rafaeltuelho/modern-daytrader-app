import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Input } from '../Input'

describe('Input', () => {
  it('should render input field', () => {
    render(<Input placeholder="Enter text" />)
    expect(screen.getByPlaceholderText('Enter text')).toBeInTheDocument()
  })

  it('should render with label', () => {
    render(<Input label="Username" placeholder="Enter username" />)
    expect(screen.getByText('Username')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Enter username')).toBeInTheDocument()
  })

  it('should display error message', () => {
    render(<Input error="This field is required" />)
    expect(screen.getByText('This field is required')).toBeInTheDocument()
  })

  it('should display helper text', () => {
    render(<Input helperText="Enter your username" />)
    expect(screen.getByText('Enter your username')).toBeInTheDocument()
  })

  it('should prioritize error over helper text', () => {
    render(
      <Input 
        error="This field is required" 
        helperText="Enter your username" 
      />
    )
    expect(screen.getByText('This field is required')).toBeInTheDocument()
    expect(screen.queryByText('Enter your username')).not.toBeInTheDocument()
  })

  it('should apply error styling when error exists', () => {
    render(<Input error="Error message" />)
    const input = screen.getByRole('textbox')
    expect(input.className).toContain('border-red-500')
  })

  it('should apply normal styling when no error', () => {
    render(<Input />)
    const input = screen.getByRole('textbox')
    expect(input.className).toContain('border-gray-300')
  })

  it('should handle user input', async () => {
    const user = userEvent.setup()
    render(<Input placeholder="Type here" />)
    
    const input = screen.getByPlaceholderText('Type here')
    await user.type(input, 'Hello World')
    
    expect(input).toHaveValue('Hello World')
  })

  it('should handle onChange event', async () => {
    const handleChange = vi.fn()
    const user = userEvent.setup()
    
    render(<Input onChange={handleChange} />)
    
    const input = screen.getByRole('textbox')
    await user.type(input, 'test')
    
    expect(handleChange).toHaveBeenCalled()
  })

  it('should support different input types', () => {
    const { rerender } = render(<Input type="text" />)
    expect(screen.getByRole('textbox')).toHaveAttribute('type', 'text')
    
    rerender(<Input type="password" />)
    const passwordInput = document.querySelector('input[type="password"]')
    expect(passwordInput).toBeInTheDocument()
    
    rerender(<Input type="email" />)
    expect(screen.getByRole('textbox')).toHaveAttribute('type', 'email')
  })

  it('should be disabled when disabled prop is true', () => {
    render(<Input disabled />)
    const input = screen.getByRole('textbox')
    expect(input).toBeDisabled()
  })

  it('should apply custom className', () => {
    render(<Input className="custom-input" />)
    const input = screen.getByRole('textbox')
    expect(input.className).toContain('custom-input')
  })

  it('should support ref forwarding', () => {
    const ref = { current: null as HTMLInputElement | null }
    render(<Input ref={ref} />)
    expect(ref.current).toBeInstanceOf(HTMLInputElement)
  })

  it('should support placeholder', () => {
    render(<Input placeholder="Enter your name" />)
    expect(screen.getByPlaceholderText('Enter your name')).toBeInTheDocument()
  })

  it('should support default value', () => {
    render(<Input defaultValue="Default text" />)
    const input = screen.getByRole('textbox') as HTMLInputElement
    expect(input.value).toBe('Default text')
  })

  it('should support controlled value', () => {
    const { rerender } = render(<Input value="Initial" onChange={() => {}} />)
    const input = screen.getByRole('textbox') as HTMLInputElement
    expect(input.value).toBe('Initial')
    
    rerender(<Input value="Updated" onChange={() => {}} />)
    expect(input.value).toBe('Updated')
  })

  it('should render error message in red', () => {
    render(<Input error="Error message" />)
    const errorText = screen.getByText('Error message')
    expect(errorText.className).toContain('text-red-600')
  })

  it('should render helper text in gray', () => {
    render(<Input helperText="Helper text" />)
    const helperText = screen.getByText('Helper text')
    expect(helperText.className).toContain('text-gray-500')
  })
})

