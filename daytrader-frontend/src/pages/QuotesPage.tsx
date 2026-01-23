import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuotes } from '../hooks';
import { QuoteCard, LoadingSpinner, ErrorAlert } from '../components';

export function QuotesPage() {
  const { data: quotes, isLoading, error } = useQuotes();
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<'symbol' | 'price' | 'change'>('symbol');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  const navigate = useNavigate();

  const handleTrade = (symbol: string) => {
    navigate(`/trade?symbol=${symbol}&action=buy`);
  };

  if (isLoading) {
    return <LoadingSpinner message="Loading quotes..." />;
  }

  if (error) {
    return <ErrorAlert message="Failed to load quotes" />;
  }

  // Filter quotes by search term
  const filteredQuotes = quotes?.filter((q) =>
    q.symbol.toLowerCase().includes(searchTerm.toLowerCase()) ||
    q.companyName.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  // Sort quotes
  const sortedQuotes = [...filteredQuotes].sort((a, b) => {
    let comparison = 0;
    switch (sortBy) {
      case 'symbol':
        comparison = a.symbol.localeCompare(b.symbol);
        break;
      case 'price':
        comparison = a.price - b.price;
        break;
      case 'change':
        comparison = a.change - b.change;
        break;
    }
    return sortDir === 'asc' ? comparison : -comparison;
  });

  const handleSort = (key: 'symbol' | 'price' | 'change') => {
    if (sortBy === key) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(key);
      setSortDir('asc');
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Stock Quotes</h1>

      {/* Search and Filter */}
      <div className="bg-white rounded-lg shadow-md p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Search by symbol or company name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => handleSort('symbol')}
              className={`px-4 py-2 rounded-md text-sm font-medium ${
                sortBy === 'symbol' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Symbol {sortBy === 'symbol' && (sortDir === 'asc' ? '↑' : '↓')}
            </button>
            <button
              onClick={() => handleSort('price')}
              className={`px-4 py-2 rounded-md text-sm font-medium ${
                sortBy === 'price' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Price {sortBy === 'price' && (sortDir === 'asc' ? '↑' : '↓')}
            </button>
            <button
              onClick={() => handleSort('change')}
              className={`px-4 py-2 rounded-md text-sm font-medium ${
                sortBy === 'change' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Change {sortBy === 'change' && (sortDir === 'asc' ? '↑' : '↓')}
            </button>
          </div>
        </div>
      </div>

      {/* Results Count */}
      <p className="text-sm text-gray-600">
        Showing {sortedQuotes.length} of {quotes?.length || 0} quotes
      </p>

      {/* Quotes Grid */}
      {sortedQuotes.length === 0 ? (
        <div className="bg-white rounded-lg shadow-md p-8 text-center">
          <p className="text-gray-500">No quotes found matching "{searchTerm}"</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {sortedQuotes.map((quote) => (
            <QuoteCard
              key={quote.symbol}
              symbol={quote.symbol}
              companyName={quote.companyName}
              price={quote.price}
              change={quote.change}
              volume={quote.volume}
              onTrade={handleTrade}
            />
          ))}
        </div>
      )}
    </div>
  );
}

