import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  Divider,
} from '@mui/material';
import { ShoppingCart as BuyIcon, TrendingDown as SellIcon } from '@mui/icons-material';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useBuy, useSell } from '../hooks/useOrders';
import { useQuote } from '../hooks/useQuotes';
import { useHolding } from '../hooks/usePortfolio';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorAlert } from '../components/ErrorAlert';
import { PriceDisplay } from '../components/PriceDisplay';
import type { Order } from '../types';

const ORDER_FEE = 9.99;

export const TradePage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const action = searchParams.get('action') || 'buy';
  const symbolParam = searchParams.get('symbol') || '';
  const holdingIDParam = searchParams.get('holdingID');

  // Form state
  const [symbol, setSymbol] = useState(symbolParam);
  const [debouncedSymbol, setDebouncedSymbol] = useState(symbolParam);
  const [quantity, setQuantity] = useState(1);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [successOrder, setSuccessOrder] = useState<Order | null>(null);

  // Debounce symbol input to avoid 404 errors on partial symbols
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSymbol(symbol);
    }, 500); // 500ms debounce
    return () => clearTimeout(timer);
  }, [symbol]);

  // Hooks - use debounced symbol for quote lookup
  const { data: quote, isLoading: quoteLoading, error: quoteError } = useQuote(debouncedSymbol);
  const { data: holding, isLoading: holdingLoading, error: holdingError } = useHolding(
    holdingIDParam ? parseInt(holdingIDParam) : 0
  );
  const buyMutation = useBuy();
  const sellMutation = useSell();

  // Update symbol when param changes
  useEffect(() => {
    if (symbolParam) {
      setSymbol(symbolParam);
      setDebouncedSymbol(symbolParam);
    }
  }, [symbolParam]);

  const handleBuySubmit = async () => {
    setConfirmOpen(false);
    try {
      const order = await buyMutation.mutateAsync({ symbol, quantity });
      setSuccessOrder(order);
    } catch (error) {
      // Error is handled by mutation state
    }
  };

  const handleSellSubmit = async () => {
    setConfirmOpen(false);
    if (!holdingIDParam) return;
    try {
      const order = await sellMutation.mutateAsync({ holdingID: parseInt(holdingIDParam) });
      // Navigate immediately to avoid the holding query refetch issue
      // (sold holding returns 404 on refetch, causing render errors)
      navigate(`/orders?sold=${order.orderID}`);
    } catch (error) {
      // Error is handled by mutation state
    }
  };

  const handleSuccessClose = () => {
    setSuccessOrder(null);
    navigate('/orders');
  };

  const estimatedTotal = quote ? quantity * quote.price + ORDER_FEE : 0;
  const estimatedProceeds = (holding?.currentPrice && holding?.quantity)
    ? holding.quantity * holding.currentPrice - ORDER_FEE
    : 0;

  const isFormValid = action === 'buy' ? symbol.trim() && quantity > 0 : !!holdingIDParam;

  // Show only success dialog when order is successful (holding may be null after sell)
  if (successOrder) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom fontWeight={600}>
          {action === 'buy' ? 'Buy Stock' : 'Sell Stock'}
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          {action === 'buy' ? 'Purchase shares of a stock' : 'Sell your holdings'}
        </Typography>
        <Dialog open={true} onClose={handleSuccessClose}>
          <DialogTitle>Order Placed Successfully</DialogTitle>
          <DialogContent>
            <Box>
              <Typography variant="body1" gutterBottom>
                Your {successOrder.orderType} order has been placed.
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Grid container spacing={2}>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Order ID
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.orderID}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Symbol
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.symbol}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Quantity
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.quantity}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Price
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${successOrder.price.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Order Fee
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${successOrder.orderFee.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Status
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.orderStatus}
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleSuccessClose} variant="contained">
              View Orders
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        {action === 'buy' ? 'Buy Stock' : 'Sell Stock'}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        {action === 'buy' ? 'Purchase shares of a stock' : 'Sell your holdings'}
      </Typography>

      {/* Success Dialog */}
      <Dialog open={!!successOrder} onClose={handleSuccessClose}>
        <DialogTitle>Order Placed Successfully</DialogTitle>
        <DialogContent>
          {successOrder && (
            <Box>
              <Typography variant="body1" gutterBottom>
                Your {successOrder.orderType} order has been placed.
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Grid container spacing={2}>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Order ID
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.orderID}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Symbol
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.symbol}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Quantity
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.quantity}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Price
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${successOrder.price.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Order Fee
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${successOrder.orderFee.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Status
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {successOrder.orderStatus}
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleSuccessClose} variant="contained">
            View Orders
          </Button>
        </DialogActions>
      </Dialog>

      {/* Buy Form */}
      {action === 'buy' && (
        <Card>
          <CardContent>
            <Typography variant="h6" fontWeight={600} gutterBottom>
              Buy Stock
            </Typography>

            {buyMutation.isError && (
              <Alert severity="error" sx={{ mb: 3 }}>
                Failed to place buy order. Please try again.
              </Alert>
            )}

            <Grid container spacing={3}>
              <Grid size={12}>
                <TextField
                  fullWidth
                  label="Stock Symbol"
                  placeholder="Enter symbol (e.g., s:0)"
                  value={symbol}
                  onChange={(e) => setSymbol(e.target.value.toUpperCase())}
                  variant="outlined"
                  required
                />
              </Grid>

              <Grid size={12}>
                <TextField
                  fullWidth
                  type="number"
                  label="Quantity"
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                  variant="outlined"
                  required
                  inputProps={{ min: 1 }}
                />
              </Grid>

              {symbol && (
                <Grid size={12}>
                  {quoteLoading && <LoadingSpinner message="Loading quote..." />}
                  {quoteError && (
                    <ErrorAlert message={`Failed to load quote for ${symbol}`} />
                  )}
                  {!quoteLoading && !quoteError && quote && (
                    <Box>
                      <Divider sx={{ my: 2 }} />
                      <Typography variant="h6" gutterBottom>
                        Quote Details
                      </Typography>
                      <Grid container spacing={2}>
                        <Grid size={6}>
                          <Typography variant="body2" color="text.secondary">
                            Company
                          </Typography>
                          <Typography variant="body1" fontWeight={600}>
                            {quote.companyName}
                          </Typography>
                        </Grid>
                        <Grid size={6}>
                          <Typography variant="body2" color="text.secondary">
                            Current Price
                          </Typography>
                          <PriceDisplay price={quote.price} change={quote.change} showIcon />
                        </Grid>
                        <Grid size={6}>
                          <Typography variant="body2" color="text.secondary">
                            Quantity
                          </Typography>
                          <Typography variant="body1" fontWeight={600}>
                            {quantity}
                          </Typography>
                        </Grid>
                        <Grid size={6}>
                          <Typography variant="body2" color="text.secondary">
                            Order Fee
                          </Typography>
                          <Typography variant="body1" fontWeight={600}>
                            ${ORDER_FEE.toFixed(2)}
                          </Typography>
                        </Grid>
                        <Grid size={12}>
                          <Divider sx={{ my: 1 }} />
                          <Typography variant="body2" color="text.secondary">
                            Estimated Total
                          </Typography>
                          <Typography variant="h5" fontWeight={600} color="primary">
                            ${estimatedTotal.toFixed(2)}
                          </Typography>
                        </Grid>
                      </Grid>
                    </Box>
                  )}
                </Grid>
              )}

              <Grid size={12}>
                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  startIcon={<BuyIcon />}
                  onClick={() => setConfirmOpen(true)}
                  disabled={!isFormValid || quoteLoading || buyMutation.isPending}
                >
                  {buyMutation.isPending ? 'Placing Order...' : 'Buy Stock'}
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* Sell Form */}
      {action === 'sell' && (
        <Card>
          <CardContent>
            <Typography variant="h6" fontWeight={600} gutterBottom>
              Sell Stock
            </Typography>

            {!holdingIDParam && (
              <Alert severity="warning" sx={{ mb: 3 }}>
                No holding ID provided. Please select a holding from your portfolio.
              </Alert>
            )}

            {sellMutation.isError && (
              <Alert severity="error" sx={{ mb: 3 }}>
                Failed to place sell order. Please try again.
              </Alert>
            )}

            {holdingIDParam && (
              <>
                {holdingLoading && <LoadingSpinner message="Loading holding details..." />}
                {holdingError && (
                  <ErrorAlert message="Failed to load holding details" />
                )}
                {!holdingLoading && !holdingError && holding && (
                  <Box>
                    <Grid container spacing={2}>
                      <Grid size={6}>
                        <Typography variant="body2" color="text.secondary">
                          Holding ID
                        </Typography>
                        <Typography variant="body1" fontWeight={600}>
                          {holding.holdingID}
                        </Typography>
                      </Grid>
                      <Grid size={6}>
                        <Typography variant="body2" color="text.secondary">
                          Symbol
                        </Typography>
                        <Typography variant="body1" fontWeight={600}>
                          {holding.symbol}
                        </Typography>
                      </Grid>
                      <Grid size={6}>
                        <Typography variant="body2" color="text.secondary">
                          Quantity Owned
                        </Typography>
                        <Typography variant="body1" fontWeight={600}>
                          {holding.quantity}
                        </Typography>
                      </Grid>
                      <Grid size={6}>
                        <Typography variant="body2" color="text.secondary">
                          Purchase Price
                        </Typography>
                        <PriceDisplay price={holding.purchasePrice} />
                      </Grid>
                      <Grid size={6}>
                        <Typography variant="body2" color="text.secondary">
                          Current Price
                        </Typography>
                        {holding.currentPrice ? (
                          <PriceDisplay price={holding.currentPrice} />
                        ) : (
                          <Typography variant="body1">N/A</Typography>
                        )}
                      </Grid>
                      <Grid size={6}>
                        <Typography variant="body2" color="text.secondary">
                          Order Fee
                        </Typography>
                        <Typography variant="body1" fontWeight={600}>
                          ${ORDER_FEE.toFixed(2)}
                        </Typography>
                      </Grid>
                      <Grid size={12}>
                        <Divider sx={{ my: 1 }} />
                        <Typography variant="body2" color="text.secondary">
                          Estimated Proceeds
                        </Typography>
                        <Typography variant="h5" fontWeight={600} color="success.main">
                          ${estimatedProceeds.toFixed(2)}
                        </Typography>
                      </Grid>
                    </Grid>

                    <Button
                      fullWidth
                      variant="contained"
                      color="error"
                      size="large"
                      startIcon={<SellIcon />}
                      onClick={() => setConfirmOpen(true)}
                      disabled={sellMutation.isPending}
                      sx={{ mt: 3 }}
                    >
                      {sellMutation.isPending ? 'Placing Order...' : 'Sell Stock'}
                    </Button>
                  </Box>
                )}
              </>
            )}
          </CardContent>
        </Card>
      )}

      {/* Confirmation Dialog */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirm {action === 'buy' ? 'Buy' : 'Sell'} Order</DialogTitle>
        <DialogContent>
          {action === 'buy' && quote && (
            <Box>
              <Typography variant="body1" gutterBottom>
                Are you sure you want to buy {quantity} shares of {symbol}?
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Grid container spacing={1}>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Symbol
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {symbol}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Quantity
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {quantity}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Price per Share
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${quote.price.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Order Fee
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${ORDER_FEE.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={12}>
                  <Divider sx={{ my: 1 }} />
                  <Typography variant="body2" color="text.secondary">
                    Total Cost
                  </Typography>
                  <Typography variant="h6" fontWeight={600} color="primary">
                    ${estimatedTotal.toFixed(2)}
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          )}
          {action === 'sell' && holding && (
            <Box>
              <Typography variant="body1" gutterBottom>
                Are you sure you want to sell {holding.quantity} shares of {holding.symbol}?
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Grid container spacing={1}>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Symbol
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {holding.symbol}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Quantity
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {holding.quantity}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Current Price
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${holding.currentPrice?.toFixed(2) || 'N/A'}
                  </Typography>
                </Grid>
                <Grid size={6}>
                  <Typography variant="body2" color="text.secondary">
                    Order Fee
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    ${ORDER_FEE.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid size={12}>
                  <Divider sx={{ my: 1 }} />
                  <Typography variant="body2" color="text.secondary">
                    Estimated Proceeds
                  </Typography>
                  <Typography variant="h6" fontWeight={600} color="success.main">
                    ${estimatedProceeds.toFixed(2)}
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Cancel</Button>
          <Button
            onClick={action === 'buy' ? handleBuySubmit : handleSellSubmit}
            variant="contained"
            color={action === 'buy' ? 'primary' : 'error'}
          >
            Confirm {action === 'buy' ? 'Buy' : 'Sell'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

