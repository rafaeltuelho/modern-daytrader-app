import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Grid
} from '@mui/material';
import { TrendingDown as SellIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { usePortfolio, usePortfolioSummary } from '../hooks/usePortfolio';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { PriceDisplay } from '../components/PriceDisplay';
import { GainLossDisplay } from '../components/GainLossDisplay';
import { StatCard } from '../components/StatCard';
import { DataTable, type Column } from '../components/DataTable';
import type { Holding } from '../types';

export const PortfolioPage: React.FC = () => {
  const navigate = useNavigate();
  const { data: holdings, isLoading: holdingsLoading, error: holdingsError, refetch: refetchHoldings } = usePortfolio();
  const { data: summary, isLoading: summaryLoading, error: summaryError, refetch: refetchSummary } = usePortfolioSummary();

  const handleSell = (holdingID: number) => {
    navigate(`/trade?holdingID=${holdingID}&action=sell`);
  };

  const holdingColumns: Column<Holding>[] = [
    { id: 'symbol', label: 'Symbol' },
    { id: 'quantity', label: 'Quantity', align: 'right' },
    {
      id: 'purchasePrice',
      label: 'Purchase Price',
      align: 'right',
      render: (row) => <PriceDisplay price={row.purchasePrice} />
    },
    {
      id: 'currentPrice',
      label: 'Current Price',
      align: 'right',
      render: (row) => row.currentPrice ? <PriceDisplay price={row.currentPrice} /> : 'N/A'
    },
    {
      id: 'marketValue',
      label: 'Market Value',
      align: 'right',
      render: (row) => row.marketValue ? `$${row.marketValue.toFixed(2)}` : 'N/A'
    },
    {
      id: 'gain',
      label: 'Gain/Loss',
      align: 'right',
      render: (row) => row.gain !== undefined && row.gainPercent !== undefined ? (
        <GainLossDisplay gain={row.gain} gainPercent={row.gainPercent} />
      ) : 'N/A'
    },
    {
      id: 'actions',
      label: 'Actions',
      align: 'center',
      sortable: false,
      render: (row) => (
        <Button
          variant="outlined"
          color="error"
          size="small"
          startIcon={<SellIcon />}
          onClick={(e) => {
            e.stopPropagation();
            handleSell(row.holdingID);
          }}
        >
          Sell
        </Button>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Portfolio
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        View and manage your holdings
      </Typography>

      {/* Portfolio Summary Stats */}
      {summaryError && (
        <Box sx={{ mb: 3 }}>
          <ErrorAlert message="Failed to load portfolio summary" onRetry={refetchSummary} />
        </Box>
      )}

      {!summaryError && (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Total Value"
              value={summary ? `$${summary.totalValue.toFixed(2)}` : '$0.00'}
              subtitle="Portfolio + Cash"
              isLoading={summaryLoading}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Holdings Value"
              value={summary ? `$${summary.holdingsValue.toFixed(2)}` : '$0.00'}
              subtitle={summary ? `${summary.numberOfHoldings} holdings` : '0 holdings'}
              isLoading={summaryLoading}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Cash Balance"
              value={summary ? `$${summary.balance.toFixed(2)}` : '$0.00'}
              subtitle="Available to trade"
              isLoading={summaryLoading}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Total Gain/Loss"
              value={summary ? `$${summary.gain.toFixed(2)}` : '$0.00'}
              subtitle={summary ? `${summary.gainPercent.toFixed(2)}%` : '0.00%'}
              color={summary && summary.gain >= 0 ? 'success.main' : 'error.main'}
              isLoading={summaryLoading}
            />
          </Grid>
        </Grid>
      )}

      {/* Holdings Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" fontWeight={600} gutterBottom>
            Your Holdings
          </Typography>
          {holdingsLoading && <LoadingSpinner message="Loading holdings..." />}
          {holdingsError && <ErrorAlert message="Failed to load holdings" onRetry={refetchHoldings} />}
          {!holdingsLoading && !holdingsError && holdings && (
            <DataTable
              columns={holdingColumns}
              data={holdings}
              emptyMessage="You don't have any holdings yet. Start trading to build your portfolio!"
            />
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

