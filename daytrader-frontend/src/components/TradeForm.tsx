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
    <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
      <h2 className="text-xl font-bold text-white mb-4">Place Trade</h2>

      {error && (
        <div className="mb-4 p-3 bg-red-500/20 border border-red-500/30 text-red-400 rounded-lg">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Buy/Sell Toggle */}
        <div className="flex rounded-lg overflow-hidden border border-white/10">
          <button type="button" onClick={() => setAction('buy')}
            className={`flex-1 py-3 px-4 text-sm font-semibold transition-all ${
              action === 'buy'
                ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-500/20'
                : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white'}`}>
            BUY
          </button>
          <button type="button" onClick={() => setAction('sell')}
            className={`flex-1 py-3 px-4 text-sm font-semibold transition-all ${
              action === 'sell'
                ? 'bg-red-600 text-white shadow-lg shadow-red-500/20'
                : 'bg-white/5 text-gray-400 hover:bg-white/10 hover:text-white'}`}>
            SELL
          </button>
        </div>

        {/* Symbol Input with Autocomplete */}
        <div className="relative">
          <label htmlFor="symbol" className="block text-sm font-medium text-gray-400 mb-2">
            Stock Symbol
          </label>
          <input id="symbol" type="text" value={symbol}
            onChange={(e) => { setSymbol(e.target.value); setShowSuggestions(true); }}
            onFocus={() => setShowSuggestions(true)}
            onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
            className="w-full px-4 py-3 bg-[#16213E] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500 transition-all"
            placeholder="Enter symbol (e.g., AAPL)" required />

          {showSuggestions && symbol && filteredQuotes.length > 0 && (
            <ul className="absolute z-10 w-full mt-2 bg-[#16213E] border border-white/10 rounded-lg shadow-xl max-h-48 overflow-auto">
              {filteredQuotes.map((quote) => (
                <li key={quote.symbol} onClick={() => handleSymbolSelect(quote.symbol)}
                  className="px-4 py-3 hover:bg-purple-500/20 cursor-pointer flex justify-between border-b border-white/5 last:border-0 transition-colors">
                  <span className="font-medium text-white">{quote.symbol}</span>
                  <span className="text-gray-400 text-sm truncate ml-2">{quote.companyName}</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Quantity Input */}
        <div>
          <label htmlFor="quantity" className="block text-sm font-medium text-gray-400 mb-2">
            Quantity
          </label>
          <input id="quantity" type="number" min="1" value={quantity}
            onChange={(e) => setQuantity(parseInt(e.target.value) || 0)}
            className="w-full px-4 py-3 bg-[#16213E] border border-white/10 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500/50 focus:border-purple-500 transition-all"
            required />
        </div>

        {/* Estimated Cost */}
        {selectedQuote && (
          <div className="p-4 bg-[#16213E]/80 rounded-lg border border-white/5">
            <div className="flex justify-between text-sm">
              <span className="text-gray-400">Price per share:</span>
              <span className="font-medium text-white">${selectedQuote.price.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center mt-2 pt-2 border-t border-white/5">
              <span className="text-gray-400">Estimated {action === 'buy' ? 'cost' : 'proceeds'}:</span>
              <span className={`font-bold text-xl ${action === 'buy' ? 'text-emerald-400' : 'text-red-400'}`}>
                ${estimatedCost.toFixed(2)}
              </span>
            </div>
          </div>
        )}

        <button type="submit" disabled={isLoading || !symbol || quantity <= 0}
          className={`w-full py-4 px-4 font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-[#0D0D0D] disabled:opacity-50 disabled:cursor-not-allowed transition-all ${
            action === 'buy'
              ? 'bg-gradient-to-r from-emerald-600 to-emerald-500 hover:from-emerald-500 hover:to-emerald-400 text-white shadow-lg shadow-emerald-500/20 hover:shadow-emerald-500/40 focus:ring-emerald-500'
              : 'bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-400 text-white shadow-lg shadow-red-500/20 hover:shadow-red-500/40 focus:ring-red-500'}`}>
          {isLoading ? 'Processing...' : `${action === 'buy' ? 'Buy' : 'Sell'} ${quantity} ${symbol || 'shares'}`}
        </button>
      </form>
    </div>
  );
}

