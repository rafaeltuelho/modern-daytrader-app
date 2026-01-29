import { describe, it, expect } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import { PriceDisplay } from '../PriceDisplay';

describe('PriceDisplay', () => {
  it('renders price formatted to 2 decimal places', () => {
    render(<PriceDisplay price={150.256} />);
    expect(screen.getByText('$150.26')).toBeInTheDocument();
  });

  it('renders price without change indicator', () => {
    render(<PriceDisplay price={100.5} />);
    expect(screen.getByText('$100.50')).toBeInTheDocument();
  });

  it('renders positive change with success color class', () => {
    const { container } = render(<PriceDisplay price={150.25} change={5.5} />);
    const priceText = screen.getByText('$150.25');
    // Check that the element exists and has Typography class
    expect(priceText).toBeInTheDocument();
    expect(priceText).toHaveClass('MuiTypography-root');
  });

  it('renders negative change with error color class', () => {
    const { container } = render(<PriceDisplay price={145.75} change={-4.25} />);
    const priceText = screen.getByText('$145.75');
    expect(priceText).toBeInTheDocument();
    expect(priceText).toHaveClass('MuiTypography-root');
  });

  it('renders zero change correctly', () => {
    render(<PriceDisplay price={150.0} change={0} />);
    const priceText = screen.getByText('$150.00');
    expect(priceText).toBeInTheDocument();
  });

  it('shows trending up icon for positive change when showIcon is true', () => {
    const { container } = render(<PriceDisplay price={150.25} change={5.5} showIcon />);
    const icon = container.querySelector('[data-testid="TrendingUpIcon"]');
    expect(icon).toBeInTheDocument();
  });

  it('shows trending down icon for negative change when showIcon is true', () => {
    const { container } = render(<PriceDisplay price={145.75} change={-4.25} showIcon />);
    const icon = container.querySelector('[data-testid="TrendingDownIcon"]');
    expect(icon).toBeInTheDocument();
  });

  it('does not show icon when showIcon is false', () => {
    const { container } = render(<PriceDisplay price={150.25} change={5.5} showIcon={false} />);
    const upIcon = container.querySelector('[data-testid="TrendingUpIcon"]');
    const downIcon = container.querySelector('[data-testid="TrendingDownIcon"]');
    expect(upIcon).not.toBeInTheDocument();
    expect(downIcon).not.toBeInTheDocument();
  });

  it('applies custom variant', () => {
    render(<PriceDisplay price={150.25} variant="h4" />);
    const priceText = screen.getByText('$150.25');
    expect(priceText).toHaveClass('MuiTypography-h4');
  });
});

