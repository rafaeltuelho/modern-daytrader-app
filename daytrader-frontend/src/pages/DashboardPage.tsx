import React from 'react';
import { Box, Typography, Card, CardContent, CardHeader, Divider, Grid } from '@mui/material';
import {
  AccountBalance,
  TrendingUp,
  ShowChart
} from '@mui/icons-material';
import { useAuth } from '../store/AuthContext';
import { usePortfolioSummary } from '../hooks/usePortfolio';
import { useOrders } from '../hooks/useOrders';
import { useMarketSummary } from '../hooks/useMarketSummary';
import { StatCard } from '../components/StatCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { GainLossDisplay } from '../components/GainLossDisplay';
import { PriceDisplay } from '../components/PriceDisplay';
import { DataTable, type Column } from '../components/DataTable';
import type { Order, Quote } from '../types';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const { data: portfolioSummary, isLoading: portfolioLoading, error: portfolioError, refetch: refetchPortfolio } = usePortfolioSummary();
  const { data: orders, isLoading: ordersLoading, error: ordersError, refetch: refetchOrders } = useOrders();
  const { data: marketSummary, isLoading: marketLoading, error: marketError, refetch: refetchMarket } = useMarketSummary();

  // Get recent orders (last 5)
  const recentOrders = React.useMemo(() => {
    if (!orders) return [];
    return [...orders]
      .sort((a, b) => new Date(b.openDate).getTime() - new Date(a.openDate).getTime())
      .slice(0, 5);
  }, [orders]);

  const orderColumns: Column<Order>[] = [
    { id: 'orderType', label: 'Type', render: (row) => row.orderType.toUpperCase() },
    { id: 'symbol', label: 'Symbol' },
    { id: 'quantity', label: 'Quantity', align: 'right' },
    { id: 'price', label: 'Price', align: 'right', render: (row) => <PriceDisplay price={row.price} /> },
    { id: 'orderStatus', label: 'Status', render: (row) => row.orderStatus.toUpperCase() },
  ];

  const topGainerColumns: Column<Quote>[] = [
    { id: 'symbol', label: 'Symbol' },
    { id: 'companyName', label: 'Company' },
    { id: 'price', label: 'Price', align: 'right', render: (row) => <PriceDisplay price={row.price} change={row.change} /> },
    { id: 'change', label: 'Change', align: 'right', render: (row) => <GainLossDisplay gain={row.change} /> },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Welcome back, {user?.profileID || 'Trader'}!
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Your trading dashboard
      </Typography>

      {/* Summary Stats */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Portfolio Value"
            value={portfolioSummary ? `$${portfolioSummary.totalValue.toFixed(2)}` : '$0.00'}
            subtitle={portfolioSummary ? `${portfolioSummary.numberOfHoldings} holdings` : 'No holdings'}
            icon={<ShowChart />}
            isLoading={portfolioLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Total Gain/Loss"
            value={portfolioSummary ? `$${portfolioSummary.gain.toFixed(2)}` : '$0.00'}
            subtitle={portfolioSummary ? `${portfolioSummary.gainPercent.toFixed(2)}%` : '0.00%'}
            icon={<TrendingUp />}
            color={portfolioSummary && portfolioSummary.gain >= 0 ? 'success.main' : 'error.main'}
            isLoading={portfolioLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Cash Balance"
            value={`$${user?.balance?.toFixed(2) || '0.00'}`}
            subtitle="Available to trade"
            icon={<AccountBalance />}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="TSIA Index"
            value={marketSummary ? marketSummary.tsia.toFixed(2) : '0.00'}
            subtitle={marketSummary ? `Vol: ${marketSummary.volume.toLocaleString()}` : 'Loading...'}
            icon={<ShowChart />}
            isLoading={marketLoading}
          />
        </Grid>
      </Grid>

      {/* Portfolio Error */}
      {portfolioError && (
        <Box sx={{ mb: 3 }}>
          <ErrorAlert
            message="Failed to load portfolio summary"
            onRetry={refetchPortfolio}
          />
        </Box>
      )}

      {/* Recent Orders */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardHeader
              title="Recent Orders"
              titleTypographyProps={{ variant: 'h6', fontWeight: 600 }}
            />
            <Divider />
            <CardContent>
              {ordersLoading && <LoadingSpinner message="Loading orders..." />}
              {ordersError && <ErrorAlert message="Failed to load orders" onRetry={refetchOrders} />}
              {!ordersLoading && !ordersError && (
                <DataTable
                  columns={orderColumns}
                  data={recentOrders}
                  emptyMessage="No orders yet"
                />
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Market Summary - Top Gainers */}
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardHeader
              title="Top Gainers"
              titleTypographyProps={{ variant: 'h6', fontWeight: 600 }}
            />
            <Divider />
            <CardContent>
              {marketLoading && <LoadingSpinner message="Loading market data..." />}
              {marketError && <ErrorAlert message="Failed to load market summary" onRetry={refetchMarket} />}
              {!marketLoading && !marketError && marketSummary && (
                <DataTable
                  columns={topGainerColumns}
                  data={marketSummary.topGainers}
                  emptyMessage="No gainers data"
                />
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

