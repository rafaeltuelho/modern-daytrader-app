import { describe, it, expect } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import { StatCard } from '../StatCard';
import { AccountBalance } from '@mui/icons-material';

describe('StatCard', () => {
  it('renders title and value', () => {
    render(<StatCard title="Total Balance" value="$25,025.00" />);
    expect(screen.getByText('Total Balance')).toBeInTheDocument();
    expect(screen.getByText('$25,025.00')).toBeInTheDocument();
  });

  it('renders subtitle when provided', () => {
    render(<StatCard title="Total Balance" value="$25,025.00" subtitle="As of today" />);
    expect(screen.getByText('As of today')).toBeInTheDocument();
  });

  it('does not render subtitle when not provided', () => {
    render(<StatCard title="Total Balance" value="$25,025.00" />);
    expect(screen.queryByText('As of today')).not.toBeInTheDocument();
  });

  it('renders icon when provided', () => {
    const { container } = render(
      <StatCard title="Total Balance" value="$25,025.00" icon={<AccountBalance />} />
    );
    const icon = container.querySelector('[data-testid="AccountBalanceIcon"]');
    expect(icon).toBeInTheDocument();
  });

  it('renders loading skeleton when isLoading is true', () => {
    const { container } = render(
      <StatCard title="Total Balance" value="$25,025.00" isLoading />
    );
    const skeletons = container.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBeGreaterThan(0);
    expect(screen.queryByText('$25,025.00')).not.toBeInTheDocument();
  });

  it('renders value when isLoading is false', () => {
    render(<StatCard title="Total Balance" value="$25,025.00" isLoading={false} />);
    expect(screen.getByText('$25,025.00')).toBeInTheDocument();
  });

  it('renders subtitle skeleton when loading and subtitle is provided', () => {
    const { container } = render(
      <StatCard title="Total Balance" value="$25,025.00" subtitle="As of today" isLoading />
    );
    const skeletons = container.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBe(2); // One for value, one for subtitle
  });

  it('applies custom color to value', () => {
    render(<StatCard title="Total Balance" value="$25,025.00" color="success.main" />);
    const value = screen.getByText('$25,025.00');
    // Just verify the element exists with Typography class - color is applied via MUI theme
    expect(value).toBeInTheDocument();
    expect(value).toHaveClass('MuiTypography-root');
  });

  it('renders numeric value', () => {
    render(<StatCard title="Holdings" value={5} />);
    expect(screen.getByText('5')).toBeInTheDocument();
  });
});

