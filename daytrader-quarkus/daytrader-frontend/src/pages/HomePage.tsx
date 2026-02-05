import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { Card, Button, Input, Alert, Spinner } from '../components/ui';
import { marketApi } from '../api/market.api';
import { accountApi } from '../api/account.api';
import { formatCurrency, formatPercent, formatVolume, getGainLossColor } from '../utils/formatters';
import { TrendingUp, TrendingDown, Search } from 'lucide-react';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [quoteSymbol, setQuoteSymbol] = useState('');

  // Fetch market summary
  const { data: marketSummary, isLoading: marketLoading } = useQuery({
    queryKey: ['marketSummary'],
    queryFn: () => marketApi.getMarketSummary(),
    refetchInterval: 5000, // Refresh every 5 seconds
  });

  // Fetch account info
  const { data: account } = useQuery({
    queryKey: ['currentAccount'],
    queryFn: () => accountApi.getCurrentAccount(),
  });

  const handleQuoteSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (quoteSymbol.trim()) {
      navigate(`/quotes?symbol=${quoteSymbol.trim().toUpperCase()}`);
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Welcome Section */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome{account?.profile?.fullName ? `, ${account.profile.fullName}` : ''}!
          </h1>
          <p className="text-gray-600 mt-1">
            Your trading dashboard
          </p>
        </div>

        {/* Account Summary */}
        {account && (
          <Card title="Account Summary">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <p className="text-sm text-gray-600">Cash Balance</p>
                <p className="text-2xl font-bold text-gray-900">
                  {formatCurrency(account.balance)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Account ID</p>
                <p className="text-lg font-semibold text-gray-900">{account.id}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Login Count</p>
                <p className="text-lg font-semibold text-gray-900">{account.loginCount}</p>
              </div>
            </div>
          </Card>
        )}

        {/* Market Summary */}
        <Card title="Market Summary">
          {marketLoading ? (
            <Spinner />
          ) : marketSummary ? (
            <div className="space-y-4">
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

              {/* Top Movers Preview */}
              {(marketSummary.topGainers || marketSummary.topLosers) && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                  {marketSummary.topGainers && marketSummary.topGainers.length > 0 && (
                    <div>
                      <h3 className="text-sm font-semibold text-gray-700 mb-2">Top Gainers</h3>
                      <div className="space-y-1">
                        {marketSummary.topGainers.slice(0, 3).map((quote) => (
                          <div key={quote.symbol} className="flex justify-between text-sm">
                            <span className="font-medium">{quote.symbol}</span>
                            <span className="text-green-600">
                              +{quote.priceChange.toFixed(2)}%
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                  {marketSummary.topLosers && marketSummary.topLosers.length > 0 && (
                    <div>
                      <h3 className="text-sm font-semibold text-gray-700 mb-2">Top Losers</h3>
                      <div className="space-y-1">
                        {marketSummary.topLosers.slice(0, 3).map((quote) => (
                          <div key={quote.symbol} className="flex justify-between text-sm">
                            <span className="font-medium">{quote.symbol}</span>
                            <span className="text-red-600">
                              {quote.priceChange.toFixed(2)}%
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          ) : (
            <Alert variant="warning">Market data unavailable</Alert>
          )}
        </Card>

        {/* Quick Quote Lookup */}
        <Card title="Quick Quote Lookup">
          <form onSubmit={handleQuoteSearch} className="flex space-x-2">
            <Input
              type="text"
              placeholder="Enter stock symbol (e.g., AAPL)"
              value={quoteSymbol}
              onChange={(e) => setQuoteSymbol(e.target.value)}
              className="flex-1"
            />
            <Button type="submit" variant="primary">
              <Search className="h-4 w-4 mr-2" />
              Search
            </Button>
          </form>
        </Card>
      </div>
    </DashboardLayout>
  );
};

