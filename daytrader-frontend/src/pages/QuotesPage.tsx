import React, { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Divider,
  Grid
} from '@mui/material';
import { Search as SearchIcon, TrendingUp as TrendingUpIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useQuotes, useQuote } from '../hooks/useQuotes';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { PriceDisplay } from '../components/PriceDisplay';
import { GainLossDisplay } from '../components/GainLossDisplay';
import { DataTable, type Column } from '../components/DataTable';
import type { Quote } from '../types';

export const QuotesPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchSymbol, setSearchSymbol] = useState('');
  const [selectedSymbol, setSelectedSymbol] = useState('');

  const { data: allQuotes, isLoading: quotesLoading, error: quotesError, refetch: refetchQuotes } = useQuotes();
  const { data: selectedQuote, isLoading: quoteLoading, error: quoteError } = useQuote(selectedSymbol);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchSymbol.trim()) {
      setSelectedSymbol(searchSymbol.trim().toUpperCase());
    }
  };

  const handleBuy = (symbol: string) => {
    navigate(`/trade?symbol=${symbol}&action=buy`);
  };

  const quoteColumns: Column<Quote>[] = [
    { id: 'symbol', label: 'Symbol' },
    { id: 'companyName', label: 'Company Name' },
    {
      id: 'price',
      label: 'Price',
      align: 'right',
      render: (row) => <PriceDisplay price={row.price} change={row.change} showIcon />
    },
    {
      id: 'change',
      label: 'Change',
      align: 'right',
      render: (row) => <GainLossDisplay gain={row.change} />
    },
    {
      id: 'volume',
      label: 'Volume',
      align: 'right',
      render: (row) => row.volume.toLocaleString()
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'center',
      sortable: false,
      render: (row) => (
        <Button
          variant="contained"
          size="small"
          onClick={(e) => {
            e.stopPropagation();
            handleBuy(row.symbol);
          }}
        >
          Buy
        </Button>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Stock Quotes
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Look up stock quotes and market prices
      </Typography>

      {/* Search Form */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <form onSubmit={handleSearch}>
            <Grid container spacing={2} alignItems="center">
              <Grid size={{ xs: 12, sm: 8, md: 6 }}>
                <TextField
                  fullWidth
                  label="Stock Symbol"
                  placeholder="Enter symbol (e.g., s:0)"
                  value={searchSymbol}
                  onChange={(e) => setSearchSymbol(e.target.value.toUpperCase())}
                  variant="outlined"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4, md: 3 }}>
                <Button
                  fullWidth
                  type="submit"
                  variant="contained"
                  size="large"
                  startIcon={<SearchIcon />}
                  disabled={!searchSymbol.trim()}
                >
                  Search
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>

      {/* Selected Quote Details */}
      {selectedSymbol && (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            {quoteLoading && <LoadingSpinner message="Loading quote..." />}
            {quoteError && (
              <ErrorAlert
                message={`Failed to load quote for ${selectedSymbol}`}
                onRetry={() => setSelectedSymbol(selectedSymbol)}
              />
            )}
            {!quoteLoading && !quoteError && selectedQuote && (
              <Box>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={3}>
                  <Box>
                    <Typography variant="h5" fontWeight={600} gutterBottom>
                      {selectedQuote.symbol}
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      {selectedQuote.companyName}
                    </Typography>
                  </Box>
                  <Button
                    variant="contained"
                    startIcon={<TrendingUpIcon />}
                    onClick={() => handleBuy(selectedQuote.symbol)}
                  >
                    Buy Stock
                  </Button>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Grid container spacing={3}>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Current Price
                    </Typography>
                    <PriceDisplay price={selectedQuote.price} change={selectedQuote.change} variant="h5" showIcon />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Change
                    </Typography>
                    <GainLossDisplay gain={selectedQuote.change} variant="h6" />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Volume
                    </Typography>
                    <Typography variant="h6">
                      {selectedQuote.volume.toLocaleString()}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Open
                    </Typography>
                    <Typography variant="h6">
                      ${selectedQuote.open.toFixed(2)}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      High
                    </Typography>
                    <Typography variant="h6" color="success.main">
                      ${selectedQuote.high.toFixed(2)}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      Low
                    </Typography>
                    <Typography variant="h6" color="error.main">
                      ${selectedQuote.low.toFixed(2)}
                    </Typography>
                  </Grid>
                </Grid>
              </Box>
            )}
          </CardContent>
        </Card>
      )}

      {/* All Quotes Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" fontWeight={600} gutterBottom>
            All Available Quotes
          </Typography>
          {quotesLoading && <LoadingSpinner message="Loading quotes..." />}
          {quotesError && <ErrorAlert message="Failed to load quotes" onRetry={refetchQuotes} />}
          {!quotesLoading && !quotesError && allQuotes && (
            <DataTable
              columns={quoteColumns}
              data={allQuotes}
              emptyMessage="No quotes available"
              onRowClick={(quote) => setSelectedSymbol(quote.symbol)}
            />
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

