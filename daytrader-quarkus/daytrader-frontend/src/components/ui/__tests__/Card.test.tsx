import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Card } from '../Card'

describe('Card', () => {
  it('should render children', () => {
    render(
      <Card>
        <p>Card content</p>
      </Card>
    )
    expect(screen.getByText('Card content')).toBeInTheDocument()
  })

  it('should render with title', () => {
    render(
      <Card title="Card Title">
        <p>Content</p>
      </Card>
    )
    expect(screen.getByText('Card Title')).toBeInTheDocument()
  })

  it('should render without title', () => {
    render(
      <Card>
        <p>Content</p>
      </Card>
    )
    expect(screen.queryByRole('heading')).not.toBeInTheDocument()
  })

  it('should apply default styling', () => {
    const { container } = render(
      <Card>
        <p>Content</p>
      </Card>
    )
    const card = container.firstChild as HTMLElement
    expect(card.className).toContain('bg-white')
    expect(card.className).toContain('rounded-lg')
    expect(card.className).toContain('shadow-md')
    expect(card.className).toContain('p-6')
  })

  it('should apply custom className', () => {
    const { container } = render(
      <Card className="custom-card">
        <p>Content</p>
      </Card>
    )
    const card = container.firstChild as HTMLElement
    expect(card.className).toContain('custom-card')
  })

  it('should render title as h2', () => {
    render(
      <Card title="Test Title">
        <p>Content</p>
      </Card>
    )
    const title = screen.getByText('Test Title')
    expect(title.tagName).toBe('H2')
  })

  it('should apply title styling', () => {
    render(
      <Card title="Test Title">
        <p>Content</p>
      </Card>
    )
    const title = screen.getByText('Test Title')
    expect(title.className).toContain('text-xl')
    expect(title.className).toContain('font-semibold')
    expect(title.className).toContain('mb-4')
  })

  it('should render multiple children', () => {
    render(
      <Card>
        <p>First paragraph</p>
        <p>Second paragraph</p>
        <button>Click me</button>
      </Card>
    )
    expect(screen.getByText('First paragraph')).toBeInTheDocument()
    expect(screen.getByText('Second paragraph')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Click me' })).toBeInTheDocument()
  })

  it('should render complex children', () => {
    render(
      <Card title="Complex Card">
        <div>
          <h3>Subtitle</h3>
          <ul>
            <li>Item 1</li>
            <li>Item 2</li>
          </ul>
        </div>
      </Card>
    )
    expect(screen.getByText('Complex Card')).toBeInTheDocument()
    expect(screen.getByText('Subtitle')).toBeInTheDocument()
    expect(screen.getByText('Item 1')).toBeInTheDocument()
    expect(screen.getByText('Item 2')).toBeInTheDocument()
  })
})

