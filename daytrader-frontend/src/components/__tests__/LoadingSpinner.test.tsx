import { describe, it, expect } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import { LoadingSpinner } from '../LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders with default message', () => {
    render(<LoadingSpinner />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingSpinner message="Please wait..." />);
    expect(screen.getByText('Please wait...')).toBeInTheDocument();
  });

  it('renders without message when message is empty', () => {
    render(<LoadingSpinner message="" />);
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('renders CircularProgress component', () => {
    const { container } = render(<LoadingSpinner />);
    const spinner = container.querySelector('.MuiCircularProgress-root');
    expect(spinner).toBeInTheDocument();
  });

  it('applies custom size to CircularProgress', () => {
    const { container } = render(<LoadingSpinner size={60} />);
    const spinner = container.querySelector('.MuiCircularProgress-root');
    expect(spinner).toHaveStyle({ width: '60px', height: '60px' });
  });
});

