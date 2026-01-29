import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Chip,
  CardHeader,
  Divider,
  IconButton,
  Tooltip,
  Grid
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  ShowChart,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { useMarketSummary } from '../hooks/useMarketSummary';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { StatCard } from '../components/StatCard';
import { PriceDisplay } from '../components/PriceDisplay';
import { GainLossDisplay } from '../components/GainLossDisplay';
import { DataTable, type Column } from '../components/DataTable';
import type { Quote } from '../types';

const AUTO_REFRESH_INTERVAL = 30000; // 30 seconds

export const MarketPage: React.FC = () => {
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  const {
    data: marketSummary,
    isLoading,
    error,
    refetch
  } = useMarketSummary(AUTO_REFRESH_INTERVAL);

  // Update last updated timestamp when data changes
  useEffect(() => {
    if (marketSummary) {
      setLastUpdated(new Date());
    }
  }, [marketSummary]);

  const handleManualRefresh = () => {
    refetch();
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
  ];

  const tsiaChange = marketSummary ? marketSummary.tsia - marketSummary.openTSIA : 0;
  const tsiaChangePercent = marketSummary && marketSummary.openTSIA !== 0
    ? ((tsiaChange / marketSummary.openTSIA) * 100)
    : 0;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Box>
          <Typography variant="h4" gutterBottom fontWeight={600}>
            Market Summary
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Real-time market overview and top movers
          </Typography>
        </Box>
        <Tooltip title="Refresh market data">
          <IconButton onClick={handleManualRefresh} disabled={isLoading}>
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      <Box display="flex" alignItems="center" gap={1} mb={4}>
        <Chip
          label={`Last updated: ${lastUpdated.toLocaleTimeString()}`}
          size="small"
          variant="outlined"
        />
        <Chip
          label="Auto-refresh: 30s"
          size="small"
          color="primary"
          variant="outlined"
        />
      </Box>

      {error && (
        <Box sx={{ mb: 3 }}>
          <ErrorAlert message="Failed to load market summary" onRetry={refetch} />
        </Box>
      )}

      {/* Market Index Stats */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="TSIA Index"
            value={marketSummary ? marketSummary.tsia.toFixed(2) : '0.00'}
            subtitle={marketSummary ? `Open: ${marketSummary.openTSIA.toFixed(2)}` : 'Loading...'}
            icon={<ShowChart />}
            color={tsiaChange >= 0 ? 'success.main' : 'error.main'}
            isLoading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Index Change"
            value={marketSummary ? `${tsiaChange >= 0 ? '+' : ''}${tsiaChange.toFixed(2)}` : '0.00'}
            subtitle={marketSummary ? `${tsiaChangePercent >= 0 ? '+' : ''}${tsiaChangePercent.toFixed(2)}%` : '0.00%'}
            icon={tsiaChange >= 0 ? <TrendingUp /> : <TrendingDown />}
            color={tsiaChange >= 0 ? 'success.main' : 'error.main'}
            isLoading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Total Volume"
            value={marketSummary ? marketSummary.volume.toLocaleString() : '0'}
            subtitle="Shares traded"
            icon={<ShowChart />}
            isLoading={isLoading}
          />
        </Grid>
      </Grid>

      {/* Top Gainers and Losers */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardHeader
              title="Top Gainers"
              titleTypographyProps={{ variant: 'h6', fontWeight: 600 }}
              avatar={<TrendingUp color="success" />}
            />
            <Divider />
            <CardContent>
              {isLoading && <LoadingSpinner message="Loading top gainers..." />}
              {!isLoading && !error && marketSummary && (
                <DataTable
                  columns={quoteColumns}
                  data={marketSummary.topGainers}
                  emptyMessage="No gainers data available"
                />
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardHeader
              title="Top Losers"
              titleTypographyProps={{ variant: 'h6', fontWeight: 600 }}
              avatar={<TrendingDown color="error" />}
            />
            <Divider />
            <CardContent>
              {isLoading && <LoadingSpinner message="Loading top losers..." />}
              {!isLoading && !error && marketSummary && (
                <DataTable
                  columns={quoteColumns}
                  data={marketSummary.topLosers}
                  emptyMessage="No losers data available"
                />
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

