import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuotes, useBuy, useHoldings, useSell } from '../hooks';
import { TradeForm, LoadingSpinner, ErrorAlert, SuccessAlert } from '../components';

export function TradePage() {
  const [searchParams] = useSearchParams();
  const initialSymbol = searchParams.get('symbol') || '';
  const initialAction = (searchParams.get('action') as 'buy' | 'sell') || 'buy';

  const { data: quotes, isLoading: quotesLoading } = useQuotes();
  const { data: holdings } = useHoldings();
  const buyMutation = useBuy();
  const sellMutation = useSell();

  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const handleTrade = async (symbol: string, quantity: number, action: 'buy' | 'sell') => {
    setSuccessMessage('');
    setErrorMessage('');

    try {
      if (action === 'buy') {
        await buyMutation.mutateAsync({ symbol, quantity });
        setSuccessMessage(`Successfully bought ${quantity} shares of ${symbol}`);
      } else {
        // For sell, we need to find the holding ID by symbol
        const holding = holdings?.find((h) => h.quote?.symbol === symbol);
        if (!holding) {
          setErrorMessage(`No holding found for ${symbol}. Go to Portfolio to sell your holdings.`);
          return;
        }
        await sellMutation.mutateAsync(holding.id);
        setSuccessMessage(`Successfully sold holding of ${symbol}`);
      }
    } catch (err: unknown) {
      const apiError = err as { message?: string; error?: string };
      setErrorMessage(apiError?.message || apiError?.error || 'Trade failed. Please try again.');
    }
  };

  const isLoading = buyMutation.isPending || sellMutation.isPending;

  if (quotesLoading) {
    return <LoadingSpinner message="Loading quotes..." />;
  }

  const formQuotes = quotes?.map((q) => ({
    symbol: q.symbol,
    companyName: q.companyName,
    price: q.price,
  })) || [];

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-3xl font-bold text-white">Trade</h1>

      {successMessage && (
        <SuccessAlert message={successMessage} onDismiss={() => setSuccessMessage('')} />
      )}

      {errorMessage && (
        <ErrorAlert message={errorMessage} onDismiss={() => setErrorMessage('')} />
      )}

      <TradeForm
        quotes={formQuotes}
        onSubmit={handleTrade}
        isLoading={isLoading}
        initialSymbol={initialSymbol}
        initialAction={initialAction}
      />

      {/* Holdings Quick Reference for Selling */}
      {holdings && holdings.length > 0 && (
        <div className="bg-[#1A1A2E]/80 backdrop-blur-sm rounded-xl p-6 border border-white/5">
          <h2 className="text-lg font-semibold text-white mb-4">Your Holdings</h2>
          <p className="text-sm text-gray-400 mb-3">
            To sell, go to your <a href="/portfolio" className="text-purple-400 hover:text-purple-300 transition-colors">Portfolio</a> and click Sell on the holding you want to sell.
          </p>
          <div className="space-y-2">
            {holdings.slice(0, 5).map((holding) => (
              <div key={holding.id} className="flex justify-between items-center p-3 bg-[#16213E]/80 rounded-lg border border-white/5 hover:border-purple-500/20 transition-colors">
                <span className="font-medium text-white">{holding.quote?.symbol || 'Unknown'}</span>
                <span className="text-gray-400">{holding.quantity} shares</span>
              </div>
            ))}
            {holdings.length > 5 && (
              <p className="text-sm text-gray-500 text-center pt-2">
                +{holdings.length - 5} more holdings
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

