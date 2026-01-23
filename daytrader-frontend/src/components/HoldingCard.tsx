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
    <div className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-shadow">
      <div className="flex justify-between items-start mb-3">
        <div>
          <h3 className="text-lg font-bold text-gray-900">{quoteSymbol}</h3>
          <p className="text-sm text-gray-600">
            {quantity} shares @ ${purchasePrice.toFixed(2)}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xl font-semibold text-gray-900">
            ${currentValue.toFixed(2)}
          </p>
          <p className={`text-sm font-medium ${isPositive ? 'text-green-500' : 'text-red-500'}`}>
            {isPositive ? '+' : ''}${gainLoss.toFixed(2)} ({isPositive ? '+' : ''}{gainLossPercent.toFixed(2)}%)
          </p>
        </div>
      </div>
      
      <div className="text-xs text-gray-500 mb-3">
        Purchased: {formattedDate}
      </div>
      
      <div className="flex justify-between items-center pt-3 border-t border-gray-100">
        <div className="text-sm">
          <span className="text-gray-500">Cost basis: </span>
          <span className="font-medium">${totalCost.toFixed(2)}</span>
        </div>
        {onSell && (
          <button
            onClick={() => onSell(holdingID)}
            className="px-4 py-1.5 bg-red-500 text-white text-sm font-medium rounded hover:bg-red-600 transition-colors"
          >
            Sell
          </button>
        )}
      </div>
    </div>
  );
}

