import { describe, it, expect } from 'vitest';
import { render, screen } from '../test/test-utils';
import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders spinner with default size', () => {
    render(<LoadingSpinner />);

    const spinner = screen.getByRole('status', { name: /loading/i });
    expect(spinner).toBeInTheDocument();
  });

  it('renders spinner with custom message', () => {
    render(<LoadingSpinner message="Please wait..." />);

    expect(screen.getByText('Please wait...')).toBeInTheDocument();
  });

  it('renders without message when message prop is not provided', () => {
    render(<LoadingSpinner />);

    expect(screen.queryByText(/please wait/i)).not.toBeInTheDocument();
  });

  it('applies correct size class for small spinner', () => {
    render(<LoadingSpinner size="sm" />);

    const spinner = screen.getByRole('status');
    expect(spinner).toHaveClass('h-6', 'w-6');
  });

  it('applies correct size class for medium spinner', () => {
    render(<LoadingSpinner size="md" />);

    const spinner = screen.getByRole('status');
    expect(spinner).toHaveClass('h-10', 'w-10');
  });

  it('applies correct size class for large spinner', () => {
    render(<LoadingSpinner size="lg" />);

    const spinner = screen.getByRole('status');
    expect(spinner).toHaveClass('h-16', 'w-16');
  });

  it('has animation class', () => {
    render(<LoadingSpinner />);

    const spinner = screen.getByRole('status');
    expect(spinner).toHaveClass('animate-spin');
  });
});

