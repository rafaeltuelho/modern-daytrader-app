import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '../test/test-utils';
import { ErrorAlert } from './ErrorAlert';

describe('ErrorAlert', () => {
  it('renders error message', () => {
    render(<ErrorAlert message="Something went wrong" />);

    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('renders default title when title prop is not provided', () => {
    render(<ErrorAlert message="Something went wrong" />);

    expect(screen.getByText('Error')).toBeInTheDocument();
  });

  it('renders custom title when title prop is provided', () => {
    render(<ErrorAlert message="Something went wrong" title="Custom Error" />);

    expect(screen.getByText('Custom Error')).toBeInTheDocument();
  });

  it('has role="alert" for accessibility', () => {
    render(<ErrorAlert message="Something went wrong" />);

    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('does not render dismiss button when onDismiss is not provided', () => {
    render(<ErrorAlert message="Something went wrong" />);

    expect(screen.queryByRole('button', { name: /dismiss/i })).not.toBeInTheDocument();
  });

  it('renders dismiss button when onDismiss is provided', () => {
    const mockOnDismiss = vi.fn();
    render(<ErrorAlert message="Something went wrong" onDismiss={mockOnDismiss} />);

    expect(screen.getByRole('button', { name: /dismiss/i })).toBeInTheDocument();
  });

  it('calls onDismiss when dismiss button is clicked', () => {
    const mockOnDismiss = vi.fn();
    render(<ErrorAlert message="Something went wrong" onDismiss={mockOnDismiss} />);

    const dismissButton = screen.getByRole('button', { name: /dismiss/i });
    fireEvent.click(dismissButton);

    expect(mockOnDismiss).toHaveBeenCalledTimes(1);
  });
});

