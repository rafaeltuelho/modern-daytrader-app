interface QuoteCardProps {
  symbol: string;
  companyName: string;
  price: number;
  change: number;
  volume: number;
  onTrade?: (symbol: string) => void;
}

export function QuoteCard({ symbol, companyName, price, change, volume, onTrade }: QuoteCardProps) {
  const isPositive = change >= 0;
  const changePercent = price > 0 ? ((change / (price - change)) * 100).toFixed(2) : '0.00';

  return (
    <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-4 border border-white/5 hover:border-purple-500/20 hover:bg-[#1A1A2E]/95 transition-all duration-300 group">
      <div className="flex justify-between items-start mb-3">
        <div>
          <h3 className="text-lg font-bold text-white group-hover:text-purple-400 transition-colors">{symbol}</h3>
          <p className="text-sm text-gray-400 truncate max-w-[150px]" title={companyName}>
            {companyName}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xl font-semibold text-white">
            ${price.toFixed(2)}
          </p>
          <p className={`text-sm font-medium ${isPositive ? 'text-emerald-400' : 'text-red-400'}`}>
            {isPositive ? '+' : ''}{change.toFixed(2)} ({isPositive ? '+' : ''}{changePercent}%)
          </p>
        </div>
      </div>

      <div className="flex justify-between items-center mt-3 pt-3 border-t border-white/5">
        <span className="text-xs text-gray-500">
          Vol: {volume.toLocaleString()}
        </span>
        {onTrade && (
          <button
            onClick={() => onTrade(symbol)}
            className="px-4 py-1.5 bg-gradient-to-r from-purple-600 to-purple-500 text-white text-sm font-medium rounded-lg hover:from-purple-500 hover:to-purple-400 transition-all shadow-lg shadow-purple-500/20 hover:shadow-purple-500/40"
          >
            Trade
          </button>
        )}
      </div>
    </div>
  );
}

