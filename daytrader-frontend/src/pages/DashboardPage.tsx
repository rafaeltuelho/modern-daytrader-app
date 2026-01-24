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
      <h1 className="text-3xl font-bold text-white">Dashboard</h1>

      {/* Account Summary */}
      {account && (
        <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
          <h2 className="text-xl font-semibold text-white mb-4">Account Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-4 bg-gradient-to-br from-purple-500/20 to-purple-600/10 rounded-xl border border-purple-500/20">
              <p className="text-sm text-gray-400">Balance</p>
              <p className="text-2xl font-bold text-purple-400">${account.balance.toFixed(2)}</p>
            </div>
            <div className="p-4 bg-gradient-to-br from-teal-500/20 to-teal-600/10 rounded-xl border border-teal-500/20">
              <p className="text-sm text-gray-400">Opening Balance</p>
              <p className="text-2xl font-bold text-teal-400">${account.openBalance.toFixed(2)}</p>
            </div>
            <div className={`p-4 rounded-xl border ${account.balance >= account.openBalance ? 'bg-gradient-to-br from-emerald-500/20 to-emerald-600/10 border-emerald-500/20' : 'bg-gradient-to-br from-red-500/20 to-red-600/10 border-red-500/20'}`}>
              <p className="text-sm text-gray-400">Gain/Loss</p>
              <p className={`text-2xl font-bold ${account.balance >= account.openBalance ? 'text-emerald-400' : 'text-red-400'}`}>
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
        <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
          <h2 className="text-xl font-semibold text-white mb-4">Market Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className={`p-4 rounded-xl border ${summary.tsia >= summary.openTsia ? 'bg-gradient-to-br from-emerald-500/20 to-emerald-600/10 border-emerald-500/20' : 'bg-gradient-to-br from-red-500/20 to-red-600/10 border-red-500/20'}`}>
              <p className="text-sm text-gray-400">TSIA Index</p>
              <p className={`text-2xl font-bold ${summary.tsia >= summary.openTsia ? 'text-emerald-400' : 'text-red-400'}`}>{summary.tsia.toFixed(2)}</p>
              <p className={`text-sm font-medium ${summary.tsia >= summary.openTsia ? 'text-emerald-400' : 'text-red-400'}`}>
                {summary.tsia >= summary.openTsia ? '+' : ''}{(summary.tsia - summary.openTsia).toFixed(2)}
              </p>
            </div>
            <div className="p-4 bg-gradient-to-br from-blue-500/20 to-blue-600/10 rounded-xl border border-blue-500/20">
              <p className="text-sm text-gray-400">Volume</p>
              <p className="text-2xl font-bold text-blue-400">{summary.volume.toLocaleString()}</p>
            </div>
            <div className="p-4 bg-gradient-to-br from-purple-500/20 to-purple-600/10 rounded-xl border border-purple-500/20">
              <p className="text-sm text-gray-400">Last Updated</p>
              <p className="text-lg font-medium text-purple-400">{new Date(summary.summaryDate).toLocaleString()}</p>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Top Gainers */}
            <div>
              <h3 className="text-lg font-semibold text-emerald-400 mb-3 flex items-center gap-2">
                <span className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></span>
                Top Gainers
              </h3>
              <div className="space-y-2">
                {summary.topGainers.slice(0, 3).map((quote) => (
                  <QuoteCard key={quote.symbol} {...quote} />
                ))}
              </div>
            </div>
            {/* Top Losers */}
            <div>
              <h3 className="text-lg font-semibold text-red-400 mb-3 flex items-center gap-2">
                <span className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></span>
                Top Losers
              </h3>
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
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold text-white">Recent Orders</h2>
          <Link to="/orders" className="text-purple-400 hover:text-purple-300 text-sm font-medium transition-colors flex items-center gap-1">
            View All
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        </div>
        {ordersLoading ? (
          <LoadingSpinner size="sm" />
        ) : recentOrders.length === 0 ? (
          <p className="text-gray-500 text-center py-4">No orders yet</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-white/5">
              <thead className="bg-[#16213E]/80">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Date</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Type</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Symbol</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Qty</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {recentOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-purple-500/5 transition-colors">
                    <td className="px-4 py-3 text-sm text-gray-300">{new Date(order.openDate).toLocaleDateString()}</td>
                    <td className={`px-4 py-3 text-sm font-medium ${order.orderType === 'buy' ? 'text-emerald-400' : 'text-red-400'}`}>
                      {order.orderType.toUpperCase()}
                    </td>
                    <td className="px-4 py-3 text-sm font-medium text-white">{order.quote?.symbol || '-'}</td>
                    <td className="px-4 py-3 text-sm text-gray-300">{order.quantity}</td>
                    <td className="px-4 py-3 text-sm">
                      <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-purple-500/20 text-purple-400 border border-purple-500/30">
                        {order.orderStatus}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>


    </div>
  );
}

