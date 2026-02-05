import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card, Table, Button, Alert, ConfirmDialog, type Column } from '../components/ui';
import { tradingApi } from '../api/trading.api';
import type { HoldingResponse } from '../types/trading.types';
import { formatCurrency, formatPercent, formatDate, getGainLossColor } from '../utils/formatters';

export const PortfolioPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [sellError, setSellError] = useState<string>('');
  const [sellSuccess, setSellSuccess] = useState<string>('');
  const [sellConfirmOpen, setSellConfirmOpen] = useState(false);
  const [holdingToSell, setHoldingToSell] = useState<HoldingResponse | null>(null);

  // Fetch holdings
  const { data: holdingsData, isLoading } = useQuery({
    queryKey: ['holdings'],
    queryFn: () => tradingApi.getHoldings(),
  });

  // Fetch portfolio summary
  const { data: portfolioSummary } = useQuery({
    queryKey: ['portfolioSummary'],
    queryFn: () => tradingApi.getPortfolioSummary(),
  });

  // Sell mutation
  const sellMutation = useMutation({
    mutationFn: (holdingId: number) =>
      tradingApi.createOrder({
        orderType: 'sell',
        holdingId,
      }),
    onSuccess: () => {
      setSellSuccess('Sell order placed successfully!');
      setSellError('');
      setSellConfirmOpen(false);
      setHoldingToSell(null);
      queryClient.invalidateQueries({ queryKey: ['holdings'] });
      queryClient.invalidateQueries({ queryKey: ['portfolioSummary'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
    onError: (error: any) => {
      setSellError(error.response?.data?.message || 'Failed to place sell order');
      setSellSuccess('');
      setSellConfirmOpen(false);
      setHoldingToSell(null);
    },
  });

  const handleSellClick = (holding: HoldingResponse) => {
    setHoldingToSell(holding);
    setSellConfirmOpen(true);
  };

  const handleSellConfirm = () => {
    if (holdingToSell) {
      sellMutation.mutate(holdingToSell.id);
    }
  };

  const handleSellCancel = () => {
    setSellConfirmOpen(false);
    setHoldingToSell(null);
  };

  const columns: Column<HoldingResponse>[] = [
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
      header: 'Quantity',
      accessor: (row) => row.quantity.toFixed(2),
    },
    {
      header: 'Purchase Price',
      accessor: (row) => formatCurrency(row.purchasePrice),
    },
    {
      header: 'Current Price',
      accessor: (row) => formatCurrency(row.currentPrice),
    },
    {
      header: 'Purchase Value',
      accessor: (row) => formatCurrency(row.purchaseValue),
    },
    {
      header: 'Current Value',
      accessor: (row) => formatCurrency(row.currentValue),
    },
    {
      header: 'Gain/Loss',
      accessor: (row) => (
        <div className={getGainLossColor(row.gain)}>
          <div>{formatCurrency(row.gain)}</div>
          <div className="text-xs">{formatPercent(row.gainPercent)}</div>
        </div>
      ),
    },
    {
      header: 'Purchase Date',
      accessor: (row) => formatDate(row.purchaseDate),
    },
    {
      header: 'Action',
      accessor: (row) => (
        <Button
          size="sm"
          variant="danger"
          onClick={() => handleSellClick(row)}
          isLoading={sellMutation.isPending && holdingToSell?.id === row.id}
        >
          Sell
        </Button>
      ),
    },
  ];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold text-gray-900">Portfolio</h1>

        {/* Portfolio Summary */}
        {portfolioSummary && (
          <Card title="Portfolio Summary">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <p className="text-sm text-gray-600">Cash Balance</p>
                <p className="text-2xl font-bold text-gray-900">
                  {formatCurrency(portfolioSummary.cashBalance)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Holdings Value</p>
                <p className="text-2xl font-bold text-gray-900">
                  {formatCurrency(portfolioSummary.holdingsValue)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Total Value</p>
                <p className="text-2xl font-bold text-gray-900">
                  {formatCurrency(portfolioSummary.totalValue)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Total Gain/Loss</p>
                <p className={`text-2xl font-bold ${getGainLossColor(portfolioSummary.totalGain)}`}>
                  {formatCurrency(portfolioSummary.totalGain)}
                </p>
                <p className={`text-sm ${getGainLossColor(portfolioSummary.totalGainPercent)}`}>
                  {formatPercent(portfolioSummary.totalGainPercent)}
                </p>
              </div>
            </div>
          </Card>
        )}

        {/* Alerts */}
        {sellSuccess && (
          <Alert variant="success" onDismiss={() => setSellSuccess('')}>
            {sellSuccess}
          </Alert>
        )}
        {sellError && (
          <Alert variant="error" onDismiss={() => setSellError('')}>
            {sellError}
          </Alert>
        )}

        {/* Holdings Table */}
        <Card title="Holdings">
          <Table
            columns={columns}
            data={Array.isArray(holdingsData) ? holdingsData : (holdingsData?.content || [])}
            isLoading={isLoading}
            emptyMessage="No holdings found. Start trading to build your portfolio!"
          />
        </Card>
      </div>

      {/* Sell Confirmation Dialog */}
      <ConfirmDialog
        isOpen={sellConfirmOpen}
        title="Confirm Sell Order"
        message={`Are you sure you want to sell your entire position in ${holdingToSell?.symbol || ''}?`}
        details={holdingToSell ? `${holdingToSell.quantity.toFixed(2)} shares at current market price` : undefined}
        confirmLabel="Sell"
        cancelLabel="Cancel"
        variant="danger"
        onConfirm={handleSellConfirm}
        onCancel={handleSellCancel}
        isLoading={sellMutation.isPending}
      />
    </DashboardLayout>
  );
};

