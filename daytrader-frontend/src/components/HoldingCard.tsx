export interface Holding {
  holdingID: number;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;
  quoteSymbol: string;
  currentPrice?: number;
}

interface HoldingCardProps {
  holding: Holding;
  onSell?: (holdingId: number) => void;
}

export function HoldingCard({ holding, onSell }: HoldingCardProps) {
  const { holdingID, quantity, purchasePrice, purchaseDate, quoteSymbol, currentPrice } = holding;

  const totalCost = quantity * purchasePrice;
  const currentValue = currentPrice ? quantity * currentPrice : totalCost;
  const gainLoss = currentValue - totalCost;
  const gainLossPercent = totalCost > 0 ? ((gainLoss / totalCost) * 100) : 0;
  const isPositive = gainLoss >= 0;

  const formattedDate = new Date(purchaseDate).toLocaleDateString();

  return (
    <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-4 border border-white/5 hover:border-purple-500/20 hover:bg-[#1A1A2E]/95 transition-all duration-300 group">
      <div className="flex justify-between items-start mb-3">
        <div>
          <h3 className="text-lg font-bold text-white group-hover:text-purple-400 transition-colors">{quoteSymbol}</h3>
          <p className="text-sm text-gray-400">
            {quantity} shares @ ${purchasePrice.toFixed(2)}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xl font-semibold text-white">
            ${currentValue.toFixed(2)}
          </p>
          <p className={`text-sm font-medium ${isPositive ? 'text-emerald-400' : 'text-red-400'}`}>
            {isPositive ? '+' : ''}${gainLoss.toFixed(2)} ({isPositive ? '+' : ''}{gainLossPercent.toFixed(2)}%)
          </p>
        </div>
      </div>

      <div className="text-xs text-gray-500 mb-3">
        Purchased: {formattedDate}
      </div>

      <div className="flex justify-between items-center pt-3 border-t border-white/5">
        <div className="text-sm">
          <span className="text-gray-500">Cost basis: </span>
          <span className="font-medium text-gray-300">${totalCost.toFixed(2)}</span>
        </div>
        {onSell && (
          <button
            onClick={() => onSell(holdingID)}
            className="px-4 py-1.5 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-500 transition-all shadow-lg shadow-red-500/20 hover:shadow-red-500/40"
          >
            Sell
          </button>
        )}
      </div>
    </div>
  );
}

