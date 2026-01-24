import { useState } from 'react';

export interface Order {
  orderID: number;
  orderType: 'buy' | 'sell';
  orderStatus: 'open' | 'completed' | 'cancelled';
  openDate: string;
  completionDate?: string;
  quantity: number;
  price: number;
  orderFee: number;
  symbol: string;
}

interface OrderHistoryProps {
  orders: Order[];
}

type SortKey = 'openDate' | 'orderType' | 'symbol' | 'quantity' | 'price' | 'orderStatus';
type SortDirection = 'asc' | 'desc';

export function OrderHistory({ orders }: OrderHistoryProps) {
  const [sortKey, setSortKey] = useState<SortKey>('openDate');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortKey(key);
      setSortDirection('desc');
    }
  };

  const sortedOrders = [...orders].sort((a, b) => {
    let comparison = 0;

    switch (sortKey) {
      case 'openDate':
        comparison = new Date(a.openDate).getTime() - new Date(b.openDate).getTime();
        break;
      case 'orderType':
      case 'symbol':
      case 'orderStatus':
        comparison = a[sortKey].localeCompare(b[sortKey]);
        break;
      case 'quantity':
      case 'price':
        comparison = a[sortKey] - b[sortKey];
        break;
    }

    return sortDirection === 'asc' ? comparison : -comparison;
  });

  const SortHeader = ({ label, sortKeyName }: { label: string; sortKeyName: SortKey }) => (
    <th
      onClick={() => handleSort(sortKeyName)}
      className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase tracking-wider cursor-pointer hover:bg-purple-500/10 hover:text-purple-400 transition-colors"
    >
      <div className="flex items-center space-x-1">
        <span>{label}</span>
        {sortKey === sortKeyName && (
          <span className="text-purple-400">{sortDirection === 'asc' ? '↑' : '↓'}</span>
        )}
      </div>
    </th>
  );

  const getStatusBadge = (status: Order['orderStatus']) => {
    const styles = {
      open: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
      completed: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30',
      cancelled: 'bg-red-500/20 text-red-400 border border-red-500/30',
    };
    return (
      <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${styles[status]}`}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </span>
    );
  };

  if (orders.length === 0) {
    return (
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-8 text-center text-gray-500 border border-white/5">
        No orders found.
      </div>
    );
  }

  return (
    <div className="bg-[#1A1A2E]/60 backdrop-blur-sm rounded-xl overflow-hidden border border-white/5">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-white/5">
          <thead className="bg-[#16213E]/80">
            <tr>
              <SortHeader label="Date" sortKeyName="openDate" />
              <SortHeader label="Type" sortKeyName="orderType" />
              <SortHeader label="Symbol" sortKeyName="symbol" />
              <SortHeader label="Quantity" sortKeyName="quantity" />
              <SortHeader label="Price" sortKeyName="price" />
              <SortHeader label="Status" sortKeyName="orderStatus" />
              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Total</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {sortedOrders.map((order) => (
              <tr key={order.orderID} className="hover:bg-purple-500/5 transition-colors">
                <td className="px-4 py-3 text-sm text-gray-300">
                  {new Date(order.openDate).toLocaleDateString()}
                </td>
                <td className="px-4 py-3 text-sm">
                  <span className={`font-medium ${order.orderType === 'buy' ? 'text-emerald-400' : 'text-red-400'}`}>
                    {order.orderType.toUpperCase()}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm font-medium text-white">{order.symbol}</td>
                <td className="px-4 py-3 text-sm text-gray-300">{order.quantity}</td>
                <td className="px-4 py-3 text-sm text-gray-300">${order.price.toFixed(2)}</td>
                <td className="px-4 py-3 text-sm">{getStatusBadge(order.orderStatus)}</td>
                <td className="px-4 py-3 text-sm font-medium text-white">
                  ${(order.quantity * order.price + order.orderFee).toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

