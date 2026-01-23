import { useState, useEffect, type FormEvent } from 'react';

interface Quote {
  symbol: string;
  companyName: string;
  price: number;
}

interface TradeFormProps {
  quotes: Quote[];
  onSubmit: (symbol: string, quantity: number, action: 'buy' | 'sell') => Promise<void>;
  isLoading?: boolean;
  error?: string;
  initialSymbol?: string;
  initialAction?: 'buy' | 'sell';
}

export function TradeForm({ 
  quotes, 
  onSubmit, 
  isLoading = false, 
  error,
  initialSymbol = '',
  initialAction = 'buy'
}: TradeFormProps) {
  const [symbol, setSymbol] = useState(initialSymbol);
  const [quantity, setQuantity] = useState<number>(1);
  const [action, setAction] = useState<'buy' | 'sell'>(initialAction);
  const [showSuggestions, setShowSuggestions] = useState(false);

  useEffect(() => {
    if (initialSymbol) setSymbol(initialSymbol);
    if (initialAction) setAction(initialAction);
  }, [initialSymbol, initialAction]);

  const filteredQuotes = quotes.filter(q => 
    q.symbol.toLowerCase().includes(symbol.toLowerCase()) ||
    q.companyName.toLowerCase().includes(symbol.toLowerCase())
  ).slice(0, 5);

  const selectedQuote = quotes.find(q => q.symbol.toUpperCase() === symbol.toUpperCase());
  const estimatedCost = selectedQuote ? selectedQuote.price * quantity : 0;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!symbol || quantity <= 0) return;
    await onSubmit(symbol.toUpperCase(), quantity, action);
  };

  const handleSymbolSelect = (selectedSymbol: string) => {
    setSymbol(selectedSymbol);
    setShowSuggestions(false);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-bold text-gray-900 mb-4">Place Trade</h2>

      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Buy/Sell Toggle */}
        <div className="flex rounded-lg overflow-hidden border border-gray-300">
          <button type="button" onClick={() => setAction('buy')}
            className={`flex-1 py-2 px-4 text-sm font-medium transition-colors ${
              action === 'buy' ? 'bg-green-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}>
            BUY
          </button>
          <button type="button" onClick={() => setAction('sell')}
            className={`flex-1 py-2 px-4 text-sm font-medium transition-colors ${
              action === 'sell' ? 'bg-red-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}>
            SELL
          </button>
        </div>

        {/* Symbol Input with Autocomplete */}
        <div className="relative">
          <label htmlFor="symbol" className="block text-sm font-medium text-gray-700 mb-1">
            Stock Symbol
          </label>
          <input id="symbol" type="text" value={symbol}
            onChange={(e) => { setSymbol(e.target.value); setShowSuggestions(true); }}
            onFocus={() => setShowSuggestions(true)}
            onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Enter symbol (e.g., AAPL)" required />
          
          {showSuggestions && symbol && filteredQuotes.length > 0 && (
            <ul className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-48 overflow-auto">
              {filteredQuotes.map((quote) => (
                <li key={quote.symbol} onClick={() => handleSymbolSelect(quote.symbol)}
                  className="px-3 py-2 hover:bg-blue-50 cursor-pointer flex justify-between">
                  <span className="font-medium">{quote.symbol}</span>
                  <span className="text-gray-500 text-sm truncate ml-2">{quote.companyName}</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Quantity Input */}
        <div>
          <label htmlFor="quantity" className="block text-sm font-medium text-gray-700 mb-1">
            Quantity
          </label>
          <input id="quantity" type="number" min="1" value={quantity}
            onChange={(e) => setQuantity(parseInt(e.target.value) || 0)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            required />
        </div>

        {/* Estimated Cost */}
        {selectedQuote && (
          <div className="p-3 bg-gray-50 rounded-md">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Price per share:</span>
              <span className="font-medium">${selectedQuote.price.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-sm mt-1">
              <span className="text-gray-600">Estimated {action === 'buy' ? 'cost' : 'proceeds'}:</span>
              <span className="font-bold text-lg">${estimatedCost.toFixed(2)}</span>
            </div>
          </div>
        )}

        <button type="submit" disabled={isLoading || !symbol || quantity <= 0}
          className={`w-full py-3 px-4 font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors ${
            action === 'buy' 
              ? 'bg-green-500 hover:bg-green-600 text-white focus:ring-green-500'
              : 'bg-red-500 hover:bg-red-600 text-white focus:ring-red-500'}`}>
          {isLoading ? 'Processing...' : `${action === 'buy' ? 'Buy' : 'Sell'} ${quantity} ${symbol || 'shares'}`}
        </button>
      </form>
    </div>
  );
}

