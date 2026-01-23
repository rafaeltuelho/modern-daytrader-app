import { Link } from 'react-router-dom';
import { useMarketSummary, useOrders, useAccount } from '../hooks';
import { QuoteCard, LoadingSpinner, ErrorAlert } from '../components';

export function DashboardPage() {
  const { data: summary, isLoading: summaryLoading, error: summaryError } = useMarketSummary();
  const { data: orders, isLoading: ordersLoading } = useOrders();
  const { data: account, isLoading: accountLoading } = useAccount();

  const recentOrders = orders?.slice(0, 5) || [];

  if (summaryLoading || accountLoading) {
    return <LoadingSpinner message="Loading dashboard..." />;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>

      {/* Account Summary */}
      {account && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Account Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-4 bg-blue-50 rounded-lg">
              <p className="text-sm text-gray-600">Balance</p>
              <p className="text-2xl font-bold text-blue-600">${account.balance.toFixed(2)}</p>
            </div>
            <div className="p-4 bg-green-50 rounded-lg">
              <p className="text-sm text-gray-600">Opening Balance</p>
              <p className="text-2xl font-bold text-green-600">${account.openBalance.toFixed(2)}</p>
            </div>
            <div className="p-4 bg-purple-50 rounded-lg">
              <p className="text-sm text-gray-600">Gain/Loss</p>
              <p className={`text-2xl font-bold ${account.balance >= account.openBalance ? 'text-green-600' : 'text-red-600'}`}>
                ${(account.balance - account.openBalance).toFixed(2)}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Market Summary */}
      {summaryError ? (
        <ErrorAlert message="Failed to load market summary" />
      ) : summary && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Market Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-600">TSIA Index</p>
              <p className="text-2xl font-bold">{summary.tsia.toFixed(2)}</p>
              <p className={`text-sm ${summary.tsia >= summary.openTsia ? 'text-green-500' : 'text-red-500'}`}>
                {summary.tsia >= summary.openTsia ? '+' : ''}{(summary.tsia - summary.openTsia).toFixed(2)}
              </p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-600">Volume</p>
              <p className="text-2xl font-bold">{summary.volume.toLocaleString()}</p>
            </div>
            <div className="p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-600">Last Updated</p>
              <p className="text-lg font-medium">{new Date(summary.summaryDate).toLocaleString()}</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Top Gainers */}
            <div>
              <h3 className="text-lg font-medium text-green-600 mb-3">Top Gainers</h3>
              <div className="space-y-2">
                {summary.topGainers.slice(0, 3).map((quote) => (
                  <QuoteCard key={quote.symbol} {...quote} />
                ))}
              </div>
            </div>
            {/* Top Losers */}
            <div>
              <h3 className="text-lg font-medium text-red-600 mb-3">Top Losers</h3>
              <div className="space-y-2">
                {summary.topLosers.slice(0, 3).map((quote) => (
                  <QuoteCard key={quote.symbol} {...quote} />
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Recent Orders */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold text-gray-900">Recent Orders</h2>
          <Link to="/orders" className="text-blue-600 hover:text-blue-700 text-sm font-medium">
            View All →
          </Link>
        </div>
        {ordersLoading ? (
          <LoadingSpinner size="sm" />
        ) : recentOrders.length === 0 ? (
          <p className="text-gray-500 text-center py-4">No orders yet</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Date</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Type</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Symbol</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Qty</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {recentOrders.map((order) => (
                  <tr key={order.id}>
                    <td className="px-4 py-2 text-sm">{new Date(order.openDate).toLocaleDateString()}</td>
                    <td className={`px-4 py-2 text-sm ${order.orderType === 'buy' ? 'text-green-600' : 'text-red-600'}`}>
                      {order.orderType.toUpperCase()}
                    </td>
                    <td className="px-4 py-2 text-sm font-medium">{order.quote?.symbol || '-'}</td>
                    <td className="px-4 py-2 text-sm">{order.quantity}</td>
                    <td className="px-4 py-2 text-sm">{order.orderStatus}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Link to="/trade" className="p-6 bg-green-500 text-white rounded-lg shadow-md hover:bg-green-600 transition-colors text-center">
          <h3 className="text-lg font-semibold">Start Trading</h3>
          <p className="text-sm opacity-90 mt-1">Buy or sell stocks</p>
        </Link>
        <Link to="/portfolio" className="p-6 bg-blue-500 text-white rounded-lg shadow-md hover:bg-blue-600 transition-colors text-center">
          <h3 className="text-lg font-semibold">View Portfolio</h3>
          <p className="text-sm opacity-90 mt-1">Check your holdings</p>
        </Link>
        <Link to="/quotes" className="p-6 bg-purple-500 text-white rounded-lg shadow-md hover:bg-purple-600 transition-colors text-center">
          <h3 className="text-lg font-semibold">Browse Quotes</h3>
          <p className="text-sm opacity-90 mt-1">Explore the market</p>
        </Link>
      </div>
    </div>
  );
}

