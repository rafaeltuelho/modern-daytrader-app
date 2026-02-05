import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card, Table, type Column } from '../components/ui';
import { marketApi } from '../api/market.api';
import type { QuoteSummary } from '../types/market.types';
import { formatCurrency, formatPercent, formatVolume, getGainLossColor } from '../utils/formatters';
import { TrendingUp, TrendingDown } from 'lucide-react';

export const MarketPage: React.FC = () => {
  // Fetch market summary
  const { data: marketSummary } = useQuery({
    queryKey: ['marketSummary'],
    queryFn: () => marketApi.getMarketSummary(),
    refetchInterval: 5000,
  });

  // Fetch top gainers
  const { data: gainersData, isLoading: gainersLoading } = useQuery({
    queryKey: ['topGainers'],
    queryFn: () => marketApi.getTopGainers(20),
    refetchInterval: 10000,
  });

  // Fetch top losers
  const { data: losersData, isLoading: losersLoading } = useQuery({
    queryKey: ['topLosers'],
    queryFn: () => marketApi.getTopLosers(20),
    refetchInterval: 10000,
  });

  const quoteColumns: Column<QuoteSummary & { id?: string | number }>[] = [
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
        // Calculate dollar change from price and openPrice
        const dollarChange = row.price - row.openPrice;
        return (
          <div className={getGainLossColor(row.priceChange)}>
            {formatCurrency(dollarChange)}
          </div>
        );
      },
    },
    {
      header: 'Change %',
      accessor: (row) => (
        <div className={getGainLossColor(row.priceChange)}>
          {row.priceChange >= 0 ? '+' : ''}{row.priceChange.toFixed(2)}%
        </div>
      ),
    },
    {
      header: 'Volume',
      accessor: (row) => formatVolume(row.volume),
    },
  ];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold text-gray-900">Market Overview</h1>

        {/* Market Summary */}
        {marketSummary && (
          <Card title="Market Summary">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <p className="text-sm text-gray-600">TSIA Index</p>
                <div className="flex items-center space-x-2">
                  <p className="text-2xl font-bold text-gray-900">
                    {marketSummary.tsia.toFixed(2)}
                  </p>
                  {marketSummary.gainPercent >= 0 ? (
                    <TrendingUp className="h-6 w-6 text-green-600" />
                  ) : (
                    <TrendingDown className="h-6 w-6 text-red-600" />
                  )}
                </div>
                <p className={`text-sm ${getGainLossColor(marketSummary.gainPercent)}`}>
                  {formatPercent(marketSummary.gainPercent)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Trading Volume</p>
                <p className="text-2xl font-bold text-gray-900">
                  {formatVolume(marketSummary.volume)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Market Status</p>
                <p className="text-lg font-semibold text-gray-900">
                  {marketSummary.marketStatus}
                </p>
              </div>
            </div>
          </Card>
        )}

        {/* Top Gainers */}
        <Card title="Top Gainers">
          <Table
            columns={quoteColumns}
            data={(gainersData || []).map((q, idx) => ({ ...q, id: q.symbol || idx }))}
            isLoading={gainersLoading}
            emptyMessage="No gainers data available"
          />
        </Card>

        {/* Top Losers */}
        <Card title="Top Losers">
          <Table
            columns={quoteColumns}
            data={(losersData || []).map((q, idx) => ({ ...q, id: q.symbol || idx }))}
            isLoading={losersLoading}
            emptyMessage="No losers data available"
          />
        </Card>
      </div>
    </DashboardLayout>
  );
};

