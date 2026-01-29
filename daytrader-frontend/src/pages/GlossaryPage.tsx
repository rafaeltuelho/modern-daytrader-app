import React, { useState, useMemo } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  InputAdornment,
  Chip,
  Grid,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
} from '@mui/material';
import {
  Search as SearchIcon,
  ExpandMore as ExpandMoreIcon,
  MenuBook as GlossaryIcon,
} from '@mui/icons-material';

interface GlossaryTerm {
  term: string;
  definition: string;
  category: 'account' | 'trading' | 'market' | 'order' | 'portfolio';
}

const glossaryTerms: GlossaryTerm[] = [
  // Account Terms
  { term: 'Account ID', definition: 'A unique integer-based key. Each user is assigned an account ID at account creation time.', category: 'account' },
  { term: 'Account Created', definition: 'The time and date the user\'s account was first created.', category: 'account' },
  { term: 'Cash Balance', definition: 'The current cash balance in the user\'s account. This does not include current stock holdings.', category: 'account' },
  { term: 'Opening Balance', definition: 'The initial cash balance in this account when it was opened.', category: 'account' },
  { term: 'User ID', definition: 'The unique user ID for the account chosen by the user at account registration.', category: 'account' },
  { term: 'Last Login', definition: 'The date and time this user last logged in to DayTrader.', category: 'account' },
  { term: 'Total Logins', definition: 'The total number of logins performed by this user since the last system reset.', category: 'account' },
  
  // Trading Terms
  { term: 'Buy Order', definition: 'An order to purchase shares of a stock at the current market price.', category: 'trading' },
  { term: 'Sell Order', definition: 'An order to sell shares of a stock that you currently own.', category: 'trading' },
  { term: 'Transaction Fee', definition: 'The fee charged by the brokerage to process an order. Also known as "txn fee".', category: 'trading' },
  { term: 'Order Type', definition: 'The type of order being placed - either "buy" or "sell".', category: 'trading' },
  
  // Market Terms
  { term: 'Symbol', definition: 'The ticker symbol for a stock (e.g., AAPL for Apple Inc., IBM for International Business Machines).', category: 'market' },
  { term: 'Company', definition: 'The full company name for an individual stock.', category: 'market' },
  { term: 'Current Price', definition: 'The current trading price for a given stock symbol.', category: 'market' },
  { term: 'Open Price', definition: 'The price of a given stock at the open of the trading session.', category: 'market' },
  { term: 'Price Range', definition: 'The low and high prices for this stock during the current trading session.', category: 'market' },
  { term: 'Volume', definition: 'The total number of shares traded for this stock during the trading session.', category: 'market' },
  { term: 'Trade Stock Index (TSIA)', definition: 'A computed index of the top stocks in DayTrader, representing overall market performance.', category: 'market' },
  { term: 'Trading Volume', definition: 'The total number of shares traded for all stocks during this trading session.', category: 'market' },
  { term: 'Top Gainers', definition: 'The list of stocks gaining the most in price during the current trading session.', category: 'market' },
  { term: 'Top Losers', definition: 'The list of stocks falling the most in price during the current trading session.', category: 'market' },
  
  // Order Terms
  { term: 'Order ID', definition: 'A unique integer-based key. Each order is assigned an order ID at order creation time.', category: 'order' },
  { term: 'Order Status', definition: 'Orders progress through states: open, processing, closed, and completed. Order status shows the current state for this order.', category: 'order' },
  { term: 'Quantity', definition: 'The number of stock shares in the order or user holding.', category: 'order' },
  { term: 'Purchase Date', definition: 'The date and time a stock was purchased.', category: 'order' },
  { term: 'Purchase Price', definition: 'The price per share used when purchasing the stock.', category: 'order' },
  
  // Portfolio Terms
  { term: 'Holding', definition: 'A stock position owned by the user, including the quantity of shares and purchase information.', category: 'portfolio' },
  { term: 'Number of Holdings', definition: 'The total number of different stocks currently owned by this account.', category: 'portfolio' },
  { term: 'Purchase Basis', definition: 'The total cost to purchase a holding. Computed as (quantity × purchase price).', category: 'portfolio' },
  { term: 'Market Value', definition: 'The current total value of a stock holding. Computed as (quantity × current price).', category: 'portfolio' },
  { term: 'Gain/Loss', definition: 'The current gain or loss of an individual stock holding. Computed as (current market value - purchase basis).', category: 'portfolio' },
  { term: 'Current Gain/Loss', definition: 'The total gain or loss of this account. Computed by subtracting the opening balance from the current sum of cash and holdings.', category: 'portfolio' },
  { term: 'Total of Holdings', definition: 'The current total value of all stock holdings in this account given the current valuation of each stock held.', category: 'portfolio' },
  { term: 'Sum of Cash/Holdings', definition: 'The total current value of this account. This is the sum of the cash balance along with the value of current stock holdings.', category: 'portfolio' },
];

const categoryLabels: Record<GlossaryTerm['category'], string> = {
  account: 'Account',
  trading: 'Trading',
  market: 'Market',
  order: 'Orders',
  portfolio: 'Portfolio',
};

const categoryColors: Record<GlossaryTerm['category'], 'primary' | 'secondary' | 'success' | 'warning' | 'info'> = {
  account: 'primary',
  trading: 'success',
  market: 'info',
  order: 'warning',
  portfolio: 'secondary',
};

export const GlossaryPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedCategory, setExpandedCategory] = useState<string | false>('account');

  const filteredTerms = useMemo(() => {
    if (!searchQuery.trim()) return glossaryTerms;
    const query = searchQuery.toLowerCase();
    return glossaryTerms.filter(
      (item) =>
        item.term.toLowerCase().includes(query) ||
        item.definition.toLowerCase().includes(query)
    );
  }, [searchQuery]);

  const groupedTerms = useMemo(() => {
    const groups: Record<string, GlossaryTerm[]> = {};
    filteredTerms.forEach((term) => {
      if (!groups[term.category]) {
        groups[term.category] = [];
      }
      groups[term.category].push(term);
    });
    return groups;
  }, [filteredTerms]);

  const handleAccordionChange = (category: string) => (_: React.SyntheticEvent, isExpanded: boolean) => {
    setExpandedCategory(isExpanded ? category : false);
  };

  return (
    <Box>
      {/* Header */}
      <Box display="flex" alignItems="center" gap={2} mb={2}>
        <GlossaryIcon sx={{ fontSize: 40, color: 'primary.main' }} />
        <Box>
          <Typography variant="h4" fontWeight={600}>
            Glossary
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Trading terms and definitions
          </Typography>
        </Box>
      </Box>

      {/* Search */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <TextField
            fullWidth
            placeholder="Search terms..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
          <Box display="flex" gap={1} mt={2} flexWrap="wrap">
            {Object.entries(categoryLabels).map(([key, label]) => (
              <Chip
                key={key}
                label={`${label} (${groupedTerms[key]?.length || 0})`}
                color={categoryColors[key as GlossaryTerm['category']]}
                variant={groupedTerms[key]?.length ? 'filled' : 'outlined'}
                size="small"
              />
            ))}
          </Box>
        </CardContent>
      </Card>

      {/* Terms by Category */}
      {filteredTerms.length === 0 ? (
        <Card>
          <CardContent>
            <Typography color="text.secondary" textAlign="center">
              No terms found matching "{searchQuery}"
            </Typography>
          </CardContent>
        </Card>
      ) : (
        Object.entries(categoryLabels).map(([category, label]) => {
          const terms = groupedTerms[category];
          if (!terms?.length) return null;

          return (
            <Accordion
              key={category}
              expanded={expandedCategory === category}
              onChange={handleAccordionChange(category)}
              sx={{ mb: 1 }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Chip
                  label={label}
                  color={categoryColors[category as GlossaryTerm['category']]}
                  size="small"
                  sx={{ mr: 2 }}
                />
                <Typography fontWeight={500}>{label} Terms</Typography>
                <Typography color="text.secondary" sx={{ ml: 'auto', mr: 2 }}>
                  {terms.length} terms
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  {terms.map((item) => (
                    <Grid key={item.term} size={12}>
                      <Box>
                        <Typography variant="subtitle1" fontWeight={600} color="primary">
                          {item.term}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {item.definition}
                        </Typography>
                      </Box>
                      <Divider sx={{ mt: 1 }} />
                    </Grid>
                  ))}
                </Grid>
              </AccordionDetails>
            </Accordion>
          );
        })
      )}
    </Box>
  );
};

