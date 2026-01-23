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
    <div className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-shadow">
      <div className="flex justify-between items-start mb-2">
        <div>
          <h3 className="text-lg font-bold text-gray-900">{symbol}</h3>
          <p className="text-sm text-gray-600 truncate max-w-[150px]" title={companyName}>
            {companyName}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xl font-semibold text-gray-900">
            ${price.toFixed(2)}
          </p>
          <p className={`text-sm font-medium ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
            {isPositive ? '+' : ''}{change.toFixed(2)} ({isPositive ? '+' : ''}{changePercent}%)
          </p>
        </div>
      </div>
      
      <div className="flex justify-between items-center mt-3 pt-3 border-t border-gray-100">
        <span className="text-xs text-gray-500">
          Vol: {volume.toLocaleString()}
        </span>
        {onTrade && (
          <button
            onClick={() => onTrade(symbol)}
            className="px-4 py-1.5 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 transition-colors"
          >
            Trade
          </button>
        )}
      </div>
    </div>
  );
}

