import React, { useState, useMemo, useEffect } from 'react';
import {
  Alert,
  Box,
  Typography,
  Card,
  CardContent,
  Chip,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Snackbar,
  type SelectChangeEvent,
} from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import { useOrders } from '../hooks/useOrders';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { DataTable, type Column } from '../components/DataTable';
import { StatCard } from '../components/StatCard';
import { PriceDisplay } from '../components/PriceDisplay';
import type { Order } from '../types';

const getStatusColor = (status: Order['orderStatus']): 'success' | 'info' | 'warning' | 'default' => {
  switch (status) {
    case 'completed':
    case 'closed':
      return 'success';
    case 'open':
      return 'info';
    case 'processing':
      return 'warning';
    case 'cancelled':
      return 'default';
    default:
      return 'default';
  }
};

const getTypeColor = (type: Order['orderType']): 'success' | 'error' => {
  return type === 'buy' ? 'success' : 'error';
};

export const OrdersPage: React.FC = () => {
  const { data: orders, isLoading, error, refetch } = useOrders();
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [searchParams, setSearchParams] = useSearchParams();
  const [showSellSuccess, setShowSellSuccess] = useState(false);
  const soldOrderId = searchParams.get('sold');

  // Show success notification when coming from a sell
  useEffect(() => {
    if (soldOrderId) {
      setShowSellSuccess(true);
      // Remove the sold param from URL
      searchParams.delete('sold');
      setSearchParams(searchParams, { replace: true });
    }
  }, [soldOrderId, searchParams, setSearchParams]);

  const handleFilterChange = (event: SelectChangeEvent) => {
    setStatusFilter(event.target.value);
  };

  // Filter orders based on status
  const filteredOrders = useMemo(() => {
    if (!orders) return [];
    if (statusFilter === 'all') return orders;
    return orders.filter(o => o.orderStatus === statusFilter);
  }, [orders, statusFilter]);

  // Sort by date (newest first)
  const sortedOrders = useMemo(() => {
    return [...filteredOrders].sort((a, b) => {
      const dateA = new Date(a.openDate).getTime();
      const dateB = new Date(b.openDate).getTime();
      return dateB - dateA;
    });
  }, [filteredOrders]);

  // Calculate stats
  const stats = useMemo(() => ({
    total: orders?.length || 0,
    buy: orders?.filter(o => o.orderType === 'buy').length || 0,
    sell: orders?.filter(o => o.orderType === 'sell').length || 0,
    completed: orders?.filter(o => o.orderStatus === 'completed' || o.orderStatus === 'closed').length || 0,
  }), [orders]);

  // Define columns
  const orderColumns: Column<Order>[] = [
    {
      id: 'orderID',
      label: 'Order ID',
      align: 'left'
    },
    {
      id: 'orderType',
      label: 'Type',
      align: 'center',
      render: (row) => (
        <Chip
          label={row.orderType.toUpperCase()}
          color={getTypeColor(row.orderType)}
          size="small"
        />
      )
    },
    {
      id: 'symbol',
      label: 'Symbol',
      align: 'left'
    },
    {
      id: 'quantity',
      label: 'Quantity',
      align: 'right'
    },
    {
      id: 'price',
      label: 'Price',
      align: 'right',
      render: (row) => <PriceDisplay price={row.price} />
    },
    {
      id: 'total',
      label: 'Total',
      align: 'right',
      render: (row) => <PriceDisplay price={row.quantity * row.price} />
    },
    {
      id: 'orderFee',
      label: 'Fee',
      align: 'right',
      render: (row) => <PriceDisplay price={row.orderFee} />
    },
    {
      id: 'orderStatus',
      label: 'Status',
      align: 'center',
      render: (row) => (
        <Chip
          label={row.orderStatus.toUpperCase()}
          color={getStatusColor(row.orderStatus)}
          size="small"
        />
      )
    },
    {
      id: 'openDate',
      label: 'Date',
      align: 'right',
      render: (row) => new Date(row.openDate).toLocaleDateString()
    },
  ];

  return (
    <Box>
      {/* Header */}
      <Typography variant="h4" gutterBottom fontWeight={600}>
        Orders
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        View your order history
      </Typography>

      {/* Stats Row */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Total Orders"
            value={stats.total}
            subtitle="All time"
            isLoading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Buy Orders"
            value={stats.buy}
            subtitle="Purchase orders"
            color="success.main"
            isLoading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Sell Orders"
            value={stats.sell}
            subtitle="Sale orders"
            color="error.main"
            isLoading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Completed Orders"
            value={stats.completed}
            subtitle="Finished orders"
            color="info.main"
            isLoading={isLoading}
          />
        </Grid>
      </Grid>

      {/* Orders Table */}
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h6" fontWeight={600}>
              Order History
            </Typography>
            <FormControl size="small" sx={{ minWidth: 150 }}>
              <InputLabel id="status-filter-label">Status</InputLabel>
              <Select
                labelId="status-filter-label"
                id="status-filter"
                value={statusFilter}
                label="Status"
                onChange={handleFilterChange}
              >
                <MenuItem value="all">All</MenuItem>
                <MenuItem value="open">Open</MenuItem>
                <MenuItem value="processing">Processing</MenuItem>
                <MenuItem value="completed">Completed</MenuItem>
                <MenuItem value="cancelled">Cancelled</MenuItem>
                <MenuItem value="closed">Closed</MenuItem>
              </Select>
            </FormControl>
          </Box>

          {isLoading && <LoadingSpinner message="Loading orders..." />}
          {error && <ErrorAlert message="Failed to load orders" onRetry={refetch} />}
          {!isLoading && !error && (
            <DataTable
              columns={orderColumns}
              data={sortedOrders}
              emptyMessage="You don't have any orders yet. Start trading to see your order history!"
            />
          )}
        </CardContent>
      </Card>

      {/* Sell Success Notification */}
      <Snackbar
        open={showSellSuccess}
        autoHideDuration={5000}
        onClose={() => setShowSellSuccess(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={() => setShowSellSuccess(false)} severity="success" sx={{ width: '100%' }}>
          Sell order placed successfully!
        </Alert>
      </Snackbar>
    </Box>
  );
};

