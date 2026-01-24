import { useState } from 'react';
import { useOrders } from '../hooks';
import { LoadingSpinner, ErrorAlert } from '../components';

const ORDERS_PER_PAGE = 10;

export function OrderHistoryPage() {
  const { data: orders, isLoading, error } = useOrders();
  const [currentPage, setCurrentPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [typeFilter, setTypeFilter] = useState<string>('all');

  if (isLoading) {
    return <LoadingSpinner message="Loading order history..." />;
  }

  if (error) {
    return <ErrorAlert message="Failed to load order history" />;
  }

  // Filter orders
  const filteredOrders = orders?.filter((order) => {
    if (statusFilter !== 'all' && order.orderStatus !== statusFilter) return false;
    if (typeFilter !== 'all' && order.orderType !== typeFilter) return false;
    return true;
  }) || [];

  // Pagination
  const totalPages = Math.ceil(filteredOrders.length / ORDERS_PER_PAGE);
  const startIndex = (currentPage - 1) * ORDERS_PER_PAGE;
  const paginatedOrders = filteredOrders.slice(startIndex, startIndex + ORDERS_PER_PAGE);

  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      open: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
      processing: 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
      completed: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30',
      closed: 'bg-gray-500/20 text-gray-400 border border-gray-500/30',
      cancelled: 'bg-red-500/20 text-red-400 border border-red-500/30',
    };
    return (
      <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${styles[status] || styles.open}`}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-white">Order History</h1>

      {/* Filters */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-4 border border-white/5">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-400 mb-2">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}
              className="w-full px-4 py-3 bg-[#16213E] border border-white/10 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500 transition-all"
            >
              <option value="all">All Statuses</option>
              <option value="open">Open</option>
              <option value="processing">Processing</option>
              <option value="completed">Completed</option>
              <option value="closed">Closed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-400 mb-2">Type</label>
            <select
              value={typeFilter}
              onChange={(e) => { setTypeFilter(e.target.value); setCurrentPage(1); }}
              className="w-full px-4 py-3 bg-[#16213E] border border-white/10 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500 transition-all"
            >
              <option value="all">All Types</option>
              <option value="buy">Buy</option>
              <option value="sell">Sell</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results Count */}
      <p className="text-sm text-gray-400">
        Showing <span className="text-purple-400 font-medium">{startIndex + 1}-{Math.min(startIndex + ORDERS_PER_PAGE, filteredOrders.length)}</span> of <span className="text-white font-medium">{filteredOrders.length}</span> orders
      </p>

      {/* Orders Table */}
      {paginatedOrders.length === 0 ? (
        <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-8 text-center border border-white/5">
          <div className="w-16 h-16 mx-auto mb-4 bg-purple-500/20 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
          <p className="text-gray-400">No orders found</p>
        </div>
      ) : (
        <div className="bg-[#1A1A2E]/60 backdrop-blur-sm rounded-xl overflow-hidden border border-white/5">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-white/5">
              <thead className="bg-[#16213E]/80">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">ID</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Date</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Type</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Symbol</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Qty</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Price</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Fee</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Total</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {paginatedOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-purple-500/5 transition-colors">
                    <td className="px-4 py-3 text-sm text-gray-500">#{order.id}</td>
                    <td className="px-4 py-3 text-sm text-gray-300">
                      {new Date(order.openDate).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <span className={order.orderType === 'buy' ? 'text-emerald-400 font-medium' : 'text-red-400 font-medium'}>
                        {order.orderType.toUpperCase()}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm font-medium text-white">{order.quote?.symbol || '-'}</td>
                    <td className="px-4 py-3 text-sm text-gray-300">{order.quantity}</td>
                    <td className="px-4 py-3 text-sm text-gray-300">${order.price.toFixed(2)}</td>
                    <td className="px-4 py-3 text-sm text-gray-500">${order.orderFee.toFixed(2)}</td>
                    <td className="px-4 py-3 text-sm font-medium text-white">
                      ${(order.quantity * order.price + order.orderFee).toFixed(2)}
                    </td>
                    <td className="px-4 py-3 text-sm">{getStatusBadge(order.orderStatus)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center space-x-3">
          <button
            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            disabled={currentPage === 1}
            className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm font-medium text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-white/10 hover:text-white transition-all"
          >
            Previous
          </button>
          <span className="text-sm text-gray-400">
            Page <span className="text-purple-400 font-medium">{currentPage}</span> of <span className="text-white font-medium">{totalPages}</span>
          </span>
          <button
            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            disabled={currentPage === totalPages}
            className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm font-medium text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-white/10 hover:text-white transition-all"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}

