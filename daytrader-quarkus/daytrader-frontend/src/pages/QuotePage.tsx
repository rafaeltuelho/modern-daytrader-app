import React, { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card, Input, Button, Alert, Table, type Column } from '../components/ui';
import { marketApi } from '../api/market.api';
import { tradingApi } from '../api/trading.api';
import type { QuoteResponse } from '../types/market.types';
import { formatCurrency, formatPercent, formatVolume, getGainLossColor } from '../utils/formatters';
import { Search } from 'lucide-react';

export const QuotePage: React.FC = () => {
  const queryClient = useQueryClient();
  const [filterText, setFilterText] = useState('');
  const [buyQuantities, setBuyQuantities] = useState<Record<string, number>>({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch all quotes (top 50)
  const { data: allQuotes, isLoading } = useQuery({
    queryKey: ['allQuotes'],
    queryFn: () => marketApi.getAllQuotes(),
  });

  // Client-side filtering as user types
  const filteredQuotes = useMemo(() => {
    if (!allQuotes) return [];
    if (!filterText.trim()) return allQuotes;

    const searchTerms = filterText.toLowerCase().split(',').map(s => s.trim()).filter(Boolean);
    return allQuotes.filter(quote =>
      searchTerms.some(term =>
        quote.symbol.toLowerCase().includes(term) ||
        quote.companyName?.toLowerCase().includes(term)
      )
    );
  }, [allQuotes, filterText]);

  // Buy mutation
  const buyMutation = useMutation({
    mutationFn: ({ symbol, quantity }: { symbol: string; quantity: number }) =>
      tradingApi.createOrder({
        orderType: 'buy',
        symbol,
        quantity,
      }),
    onSuccess: (data) => {
      setSuccess(`Buy order placed successfully! Order ID: ${data.id}`);
      setError('');
      setBuyQuantities({});
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['portfolioSummary'] });
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to place buy order');
      setSuccess('');
    },
  });

  const handleBuy = (symbol: string) => {
    const quantity = buyQuantities[symbol];
    if (!quantity || quantity <= 0) {
      setError('Please enter a valid quantity');
      return;
    }
    buyMutation.mutate({ symbol, quantity });
  };

  const columns: Column<QuoteResponse & { id?: string | number }>[] = [
    {
      header: 'Symbol',
      accessor: 'symbol',
      className: 'font-semibold',
    },
    {
      header: 'Company',
      accessor: 'companyName',
    },
    {
      header: 'Price',
      accessor: (row) => formatCurrency(row.price),
      className: 'font-semibold',
    },
    {
      header: 'Change',
      accessor: (row) => {
        // Calculate change percent from priceChange and openPrice
        const changePercent = row.openPrice && row.openPrice > 0
          ? (row.priceChange / row.openPrice) * 100
          : 0;
        return (
          <div className={getGainLossColor(row.priceChange)}>
            <div>{row.priceChange >= 0 ? '+' : ''}{row.priceChange.toFixed(2)}%</div>
            <div className="text-xs">{formatCurrency(row.price - row.openPrice)}</div>
          </div>
        );
      },
    },
    {
      header: 'Open',
      accessor: (row) => formatCurrency(row.openPrice),
    },
    {
      header: 'High',
      accessor: (row) => formatCurrency(row.highPrice),
    },
    {
      header: 'Low',
      accessor: (row) => formatCurrency(row.lowPrice),
    },
    {
      header: 'Volume',
      accessor: (row) => formatVolume(row.volume),
    },
    {
      header: 'Quantity',
      accessor: (row) => (
        <Input
          type="number"
          min="0.01"
          step="0.01"
          placeholder="Qty"
          value={buyQuantities[row.symbol] || ''}
          onChange={(e) =>
            setBuyQuantities({
              ...buyQuantities,
              [row.symbol]: parseFloat(e.target.value) || 0,
            })
          }
          className="w-24"
        />
      ),
    },
    {
      header: 'Action',
      accessor: (row) => (
        <Button
          size="sm"
          variant="primary"
          onClick={() => handleBuy(row.symbol)}
          isLoading={buyMutation.isPending}
        >
          Buy
        </Button>
      ),
    },
  ];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Stock Quotes</h1>
          <span className="text-sm text-gray-500">
            Showing {filteredQuotes.length} of {allQuotes?.length || 0} quotes
          </span>
        </div>

        {/* Filter Input */}
        <Card title="Filter Quotes">
          <div className="flex items-center space-x-2">
            <Search className="h-5 w-5 text-gray-400" />
            <Input
              type="text"
              placeholder="Type to filter by symbol or company name (e.g., AAPL, Apple, tech)"
              value={filterText}
              onChange={(e) => setFilterText(e.target.value)}
              className="flex-1"
            />
            {filterText && (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => setFilterText('')}
              >
                Clear
              </Button>
            )}
          </div>
        </Card>

        {/* Alerts */}
        {success && (
          <Alert variant="success" onDismiss={() => setSuccess('')}>
            {success}
          </Alert>
        )}
        {error && (
          <Alert variant="error" onDismiss={() => setError('')}>
            {error}
          </Alert>
        )}

        {/* Quotes Table */}
        <Card title="All Quotes">
          <Table
            columns={columns}
            data={filteredQuotes.map((q, idx) => ({ ...q, id: q.symbol || idx }))}
            isLoading={isLoading}
            emptyMessage={filterText ? "No quotes match your filter. Try a different search term." : "No quotes available."}
          />
        </Card>
      </div>
    </DashboardLayout>
  );
};

