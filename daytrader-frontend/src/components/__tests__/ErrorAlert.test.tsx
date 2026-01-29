import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import userEvent from '@testing-library/user-event';
import { ErrorAlert } from '../ErrorAlert';

describe('ErrorAlert', () => {
  it('renders with default message and title', () => {
    render(<ErrorAlert />);
    expect(screen.getByText('Error')).toBeInTheDocument();
    expect(screen.getByText('An error occurred while loading data')).toBeInTheDocument();
  });

  it('renders with custom message and title', () => {
    render(<ErrorAlert message="Custom error message" title="Custom Error" />);
    expect(screen.getByText('Custom Error')).toBeInTheDocument();
    expect(screen.getByText('Custom error message')).toBeInTheDocument();
  });

  it('does not render retry button when onRetry is not provided', () => {
    render(<ErrorAlert />);
    expect(screen.queryByRole('button', { name: /retry/i })).not.toBeInTheDocument();
  });

  it('renders retry button when onRetry is provided', () => {
    const onRetry = vi.fn();
    render(<ErrorAlert onRetry={onRetry} />);
    expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument();
  });

  it('calls onRetry when retry button is clicked', async () => {
    const user = userEvent.setup();
    const onRetry = vi.fn();
    render(<ErrorAlert onRetry={onRetry} />);
    
    const retryButton = screen.getByRole('button', { name: /retry/i });
    await user.click(retryButton);
    
    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it('renders with error severity', () => {
    const { container } = render(<ErrorAlert />);
    const alert = container.querySelector('.MuiAlert-standardError');
    expect(alert).toBeInTheDocument();
  });
});

