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
      className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
    >
      <div className="flex items-center space-x-1">
        <span>{label}</span>
        {sortKey === sortKeyName && (
          <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
        )}
      </div>
    </th>
  );

  const getStatusBadge = (status: Order['orderStatus']) => {
    const styles = {
      open: 'bg-yellow-100 text-yellow-800',
      completed: 'bg-green-100 text-green-800',
      cancelled: 'bg-red-100 text-red-800',
    };
    return (
      <span className={`px-2 py-1 text-xs font-medium rounded-full ${styles[status]}`}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </span>
    );
  };

  if (orders.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6 text-center text-gray-500">
        No orders found.
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <SortHeader label="Date" sortKeyName="openDate" />
              <SortHeader label="Type" sortKeyName="orderType" />
              <SortHeader label="Symbol" sortKeyName="symbol" />
              <SortHeader label="Quantity" sortKeyName="quantity" />
              <SortHeader label="Price" sortKeyName="price" />
              <SortHeader label="Status" sortKeyName="orderStatus" />
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Total</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {sortedOrders.map((order) => (
              <tr key={order.orderID} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-sm text-gray-900">
                  {new Date(order.openDate).toLocaleDateString()}
                </td>
                <td className="px-4 py-3 text-sm">
                  <span className={order.orderType === 'buy' ? 'text-green-600' : 'text-red-600'}>
                    {order.orderType.toUpperCase()}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm font-medium text-gray-900">{order.symbol}</td>
                <td className="px-4 py-3 text-sm text-gray-900">{order.quantity}</td>
                <td className="px-4 py-3 text-sm text-gray-900">${order.price.toFixed(2)}</td>
                <td className="px-4 py-3 text-sm">{getStatusBadge(order.orderStatus)}</td>
                <td className="px-4 py-3 text-sm font-medium text-gray-900">
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

