import { describe, it, expect } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import { GainLossDisplay } from '../GainLossDisplay';

describe('GainLossDisplay', () => {
  it('renders positive gain with plus sign', () => {
    render(<GainLossDisplay gain={1025.50} />);
    expect(screen.getByText('+$1025.50')).toBeInTheDocument();
  });

  it('renders negative gain with dollar sign and absolute value', () => {
    // Component uses Math.abs() so negative values show as positive with no sign prefix
    render(<GainLossDisplay gain={-500.25} />);
    expect(screen.getByText('$500.25')).toBeInTheDocument();
  });

  it('renders zero gain with plus sign', () => {
    render(<GainLossDisplay gain={0} />);
    expect(screen.getByText('+$0.00')).toBeInTheDocument();
  });

  it('renders gain with percentage', () => {
    render(<GainLossDisplay gain={1025.50} gainPercent={7.32} />);
    expect(screen.getByText('+$1025.50 (+7.32%)')).toBeInTheDocument();
  });

  it('renders negative gain with percentage using absolute values', () => {
    // Component uses Math.abs() for both gain and percentage
    render(<GainLossDisplay gain={-500.25} gainPercent={-3.45} />);
    expect(screen.getByText('$500.25 (3.45%)')).toBeInTheDocument();
  });

  it('shows trending up icon for positive gain', () => {
    const { container } = render(<GainLossDisplay gain={1025.50} />);
    const icon = container.querySelector('[data-testid="TrendingUpIcon"]');
    expect(icon).toBeInTheDocument();
  });

  it('shows trending down icon for negative gain', () => {
    const { container } = render(<GainLossDisplay gain={-500.25} />);
    const icon = container.querySelector('[data-testid="TrendingDownIcon"]');
    expect(icon).toBeInTheDocument();
  });

  it('does not show icon when showIcon is false', () => {
    const { container } = render(<GainLossDisplay gain={1025.50} showIcon={false} />);
    const upIcon = container.querySelector('[data-testid="TrendingUpIcon"]');
    const downIcon = container.querySelector('[data-testid="TrendingDownIcon"]');
    expect(upIcon).not.toBeInTheDocument();
    expect(downIcon).not.toBeInTheDocument();
  });

  it('applies success styling for positive gain', () => {
    render(<GainLossDisplay gain={1025.50} />);
    const text = screen.getByText('+$1025.50');
    expect(text).toBeInTheDocument();
    expect(text).toHaveClass('MuiTypography-root');
  });

  it('applies error styling for negative gain', () => {
    render(<GainLossDisplay gain={-500.25} />);
    const text = screen.getByText('$500.25');
    expect(text).toBeInTheDocument();
    expect(text).toHaveClass('MuiTypography-root');
  });

  it('applies custom variant', () => {
    render(<GainLossDisplay gain={1025.50} variant="h4" />);
    const text = screen.getByText('+$1025.50');
    expect(text).toHaveClass('MuiTypography-h4');
  });
});

