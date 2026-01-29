import { describe, it, expect } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import userEvent from '@testing-library/user-event';
import { GlossaryPage } from '../GlossaryPage';

describe('GlossaryPage', () => {
  it('renders glossary page title', () => {
    render(<GlossaryPage />);
    // The actual title is "Glossary" not "Trading Glossary"
    expect(screen.getByRole('heading', { name: 'Glossary' })).toBeInTheDocument();
  });

  it('renders search input', () => {
    render(<GlossaryPage />);
    expect(screen.getByPlaceholderText(/search terms/i)).toBeInTheDocument();
  });

  it('renders category chips with counts', () => {
    render(<GlossaryPage />);
    // The chips show category name with count, e.g., "Account (7)"
    expect(screen.getByText(/Account \(7\)/)).toBeInTheDocument();
    expect(screen.getByText(/Trading \(4\)/)).toBeInTheDocument();
    expect(screen.getByText(/Market \(10\)/)).toBeInTheDocument();
    expect(screen.getByText(/Orders \(5\)/)).toBeInTheDocument();
    expect(screen.getByText(/Portfolio \(8\)/)).toBeInTheDocument();
  });

  it('displays terms in accordions by category', () => {
    render(<GlossaryPage />);
    // Check for accordion buttons with category names
    expect(screen.getByRole('button', { name: /Account.*Terms.*7 terms/i })).toBeInTheDocument();
  });

  it('filters terms by search query', async () => {
    const user = userEvent.setup();
    render(<GlossaryPage />);

    const searchInput = screen.getByPlaceholderText(/search terms/i);
    await user.type(searchInput, 'balance');

    // After filtering, only matching terms should be visible
    expect(screen.getByText('Cash Balance')).toBeInTheDocument();
    expect(screen.getByText('Opening Balance')).toBeInTheDocument();
  });

  it('shows category accordions with term counts', () => {
    render(<GlossaryPage />);
    // Check that accordions show term counts
    expect(screen.getByText(/7 terms/)).toBeInTheDocument(); // Account
    expect(screen.getByText(/4 terms/)).toBeInTheDocument(); // Trading
  });

  it('updates chip counts when filtering', async () => {
    const user = userEvent.setup();
    render(<GlossaryPage />);

    const searchInput = screen.getByPlaceholderText(/search terms/i);
    await user.type(searchInput, 'TSIA');

    // After filtering for TSIA, only Market category should have matches
    expect(screen.getByText(/Market \(1\)/)).toBeInTheDocument();
    expect(screen.getByText(/Account \(0\)/)).toBeInTheDocument();
  });

  it('displays term count in accordion headers', () => {
    render(<GlossaryPage />);
    // Check for term counts in accordion headers
    expect(screen.getByText('7 terms')).toBeInTheDocument();
  });

  it('shows no results message when search has no matches', async () => {
    const user = userEvent.setup();
    render(<GlossaryPage />);

    const searchInput = screen.getByPlaceholderText(/search terms/i);
    await user.type(searchInput, 'nonexistentterm12345');

    expect(screen.getByText(/no terms found/i)).toBeInTheDocument();
  });

  it('renders terms in accordion format', () => {
    const { container } = render(<GlossaryPage />);
    const accordions = container.querySelectorAll('.MuiAccordion-root');
    expect(accordions.length).toBeGreaterThan(0);
  });

  it('expands accordion when clicked', async () => {
    const user = userEvent.setup();
    render(<GlossaryPage />);

    // Click on Trading accordion to expand it
    const tradingAccordion = screen.getByRole('button', { name: /Trading.*Terms.*4 terms/i });
    await user.click(tradingAccordion);

    // Check if trading terms are visible
    expect(screen.getByText('Buy Order')).toBeInTheDocument();
    expect(screen.getByText('Sell Order')).toBeInTheDocument();
  });

  it('clears search when input is cleared', async () => {
    const user = userEvent.setup();
    render(<GlossaryPage />);

    const searchInput = screen.getByPlaceholderText(/search terms/i);
    await user.type(searchInput, 'TSIA');

    // Only 1 term should match
    expect(screen.getByText(/Market \(1\)/)).toBeInTheDocument();

    await user.clear(searchInput);

    // All terms should be back
    expect(screen.getByText(/Market \(10\)/)).toBeInTheDocument();
  });

  it('shows subtitle text', () => {
    render(<GlossaryPage />);
    expect(screen.getByText('Trading terms and definitions')).toBeInTheDocument();
  });

  it('displays Account accordion expanded by default', async () => {
    render(<GlossaryPage />);
    // Account accordion should be expanded by default
    // The accordion header should be visible
    const accountAccordion = screen.getByRole('button', { name: /Account.*Terms.*7 terms/i });
    expect(accountAccordion).toBeInTheDocument();

    // Since it's expanded by default, Account terms should be visible
    expect(screen.getByText('Account ID')).toBeInTheDocument();
  });

  it('searches in both term names and definitions', async () => {
    const user = userEvent.setup();
    render(<GlossaryPage />);

    const searchInput = screen.getByPlaceholderText(/search terms/i);
    // Search for a word that appears in a definition
    await user.type(searchInput, 'brokerage');

    // Should find Transaction Fee which mentions brokerage in its definition
    expect(screen.getByText('Transaction Fee')).toBeInTheDocument();
  });
});

