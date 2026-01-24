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
      <h1 className="text-3xl font-bold text-white">Stock Quotes</h1>

      {/* Search and Filter */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-4 border border-white/5">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg className="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <input
              type="text"
              placeholder="Search by symbol or company name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-3 bg-[#16213E] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500 transition-all"
            />
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => handleSort('symbol')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                sortBy === 'symbol'
                  ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
                  : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white border border-white/10'
              }`}
            >
              Symbol {sortBy === 'symbol' && (sortDir === 'asc' ? '↑' : '↓')}
            </button>
            <button
              onClick={() => handleSort('price')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                sortBy === 'price'
                  ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
                  : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white border border-white/10'
              }`}
            >
              Price {sortBy === 'price' && (sortDir === 'asc' ? '↑' : '↓')}
            </button>
            <button
              onClick={() => handleSort('change')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                sortBy === 'change'
                  ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/20'
                  : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white border border-white/10'
              }`}
            >
              Change {sortBy === 'change' && (sortDir === 'asc' ? '↑' : '↓')}
            </button>
          </div>
        </div>
      </div>

      {/* Results Count */}
      <p className="text-sm text-gray-400">
        Showing <span className="text-purple-400 font-medium">{sortedQuotes.length}</span> of <span className="text-white font-medium">{quotes?.length || 0}</span> quotes
      </p>

      {/* Quotes Grid */}
      {sortedQuotes.length === 0 ? (
        <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-8 text-center border border-white/5">
          <div className="w-16 h-16 mx-auto mb-4 bg-purple-500/20 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <p className="text-gray-400">No quotes found matching "<span className="text-purple-400">{searchTerm}</span>"</p>
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

