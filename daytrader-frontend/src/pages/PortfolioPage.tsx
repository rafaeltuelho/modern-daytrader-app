import { useState } from 'react';
import { useHoldings, useSell, useQuotes } from '../hooks';
import { HoldingCard, LoadingSpinner, ErrorAlert, SuccessAlert } from '../components';

export function PortfolioPage() {
  const { data: holdings, isLoading, error, refetch } = useHoldings();
  const { data: quotes } = useQuotes();
  const sellMutation = useSell();
  const [successMessage, setSuccessMessage] = useState('');

  const handleSell = async (holdingId: number) => {
    if (!confirm('Are you sure you want to sell this holding?')) return;
    
    try {
      await sellMutation.mutateAsync(holdingId);
      setSuccessMessage('Sale completed successfully!');
      refetch();
    } catch (err) {
      // Error is handled by mutation state
    }
  };

  // Enrich holdings with current prices from quotes
  const enrichedHoldings = holdings?.map((holding) => {
    const quote = quotes?.find((q) => q.symbol === holding.quote?.symbol);
    return {
      holdingID: holding.id,
      quantity: holding.quantity,
      purchasePrice: holding.purchasePrice,
      purchaseDate: holding.purchaseDate,
      quoteSymbol: holding.quote?.symbol || 'Unknown',
      currentPrice: quote?.price || holding.quote?.price,
    };
  }) || [];

  // Calculate totals
  const totalCost = enrichedHoldings.reduce((sum, h) => sum + h.quantity * h.purchasePrice, 0);
  const totalValue = enrichedHoldings.reduce((sum, h) => sum + h.quantity * (h.currentPrice || h.purchasePrice), 0);
  const totalGain = totalValue - totalCost;

  if (isLoading) {
    return <LoadingSpinner message="Loading portfolio..." />;
  }

  if (error) {
    return <ErrorAlert message="Failed to load portfolio" />;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-white">Portfolio</h1>

      {successMessage && (
        <SuccessAlert message={successMessage} onDismiss={() => setSuccessMessage('')} />
      )}

      {sellMutation.error && (
        <ErrorAlert
          message={(sellMutation.error as { message?: string })?.message || 'Failed to sell holding'}
        />
      )}

      {/* Portfolio Summary */}
      <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
        <h2 className="text-xl font-semibold text-white mb-4">Portfolio Summary</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="p-4 bg-gradient-to-br from-purple-500/20 to-purple-600/10 rounded-xl border border-purple-500/20">
            <p className="text-sm text-gray-400">Holdings</p>
            <p className="text-2xl font-bold text-purple-400">{enrichedHoldings.length}</p>
          </div>
          <div className="p-4 bg-gradient-to-br from-blue-500/20 to-blue-600/10 rounded-xl border border-blue-500/20">
            <p className="text-sm text-gray-400">Total Cost</p>
            <p className="text-2xl font-bold text-blue-400">${totalCost.toFixed(2)}</p>
          </div>
          <div className="p-4 bg-gradient-to-br from-teal-500/20 to-teal-600/10 rounded-xl border border-teal-500/20">
            <p className="text-sm text-gray-400">Market Value</p>
            <p className="text-2xl font-bold text-teal-400">${totalValue.toFixed(2)}</p>
          </div>
          <div className={`p-4 rounded-xl border ${totalGain >= 0 ? 'bg-gradient-to-br from-emerald-500/20 to-emerald-600/10 border-emerald-500/20' : 'bg-gradient-to-br from-red-500/20 to-red-600/10 border-red-500/20'}`}>
            <p className="text-sm text-gray-400">Total Gain/Loss</p>
            <p className={`text-2xl font-bold ${totalGain >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
              {totalGain >= 0 ? '+' : ''}${totalGain.toFixed(2)}
              <span className="text-sm ml-1">
                ({totalCost > 0 ? ((totalGain / totalCost) * 100).toFixed(2) : 0}%)
              </span>
            </p>
          </div>
        </div>
      </div>

      {/* Holdings List */}
      {enrichedHoldings.length === 0 ? (
        <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-8 text-center border border-white/5">
          <div className="w-16 h-16 mx-auto mb-4 bg-purple-500/20 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-purple-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          </div>
          <p className="text-gray-400">No holdings yet. Start trading to build your portfolio!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {enrichedHoldings.map((holding) => (
            <HoldingCard
              key={holding.holdingID}
              holding={holding}
              onSell={handleSell}
            />
          ))}
        </div>
      )}
    </div>
  );
}

