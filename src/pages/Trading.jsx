import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTrading } from '../context/TradingContext';
import Sparkline from '../components/Sparkline';
import {
  TrendingUp,
  TrendingDown,
  ChevronDown,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';

export default function Trading() {
  const [searchParams] = useSearchParams();
  const { stocks, stockMap, portfolioMetrics, executeOrder, validateOrder } = useTrading();

  const querySymbol = searchParams.get('symbol');
  const [selectedSymbolOverride, setSelectedSymbolOverride] = useState(null);
  const selectedSymbol = selectedSymbolOverride || (querySymbol && stockMap[querySymbol] ? querySymbol : 'AAPL');

  const [orderSide, setOrderSide] = useState('BUY'); // 'BUY' | 'SELL'
  const [orderType, setOrderType] = useState('MARKET'); // 'MARKET' | 'LIMIT'
  const [quantity, setQuantity] = useState('10');
  const [customPrice, setCustomPrice] = useState(null);
  const [orderFeedback, setOrderFeedback] = useState(null);

  const currentStock = stockMap[selectedSymbol] || stocks[0];
  const limitPrice = customPrice !== null ? customPrice : (currentStock ? currentStock.price.toFixed(2) : '0.00');
  const userHolding = portfolioMetrics.holdings.find((h) => h.symbol === selectedSymbol);
  const ownedShares = userHolding ? userHolding.shares : 0;

  const effPrice = orderType === 'LIMIT'
    ? parseFloat(limitPrice) || currentStock.price
    : (orderSide === 'BUY' ? currentStock.ask : currentStock.bid);

  const numQty = parseInt(quantity, 10) || 0;
  const estimatedTotal = Number((numQty * effPrice).toFixed(2));

  // Quick percentage allocation
  const handleQuickPercent = (pct) => {
    if (orderSide === 'BUY') {
      const maxAffordable = Math.floor(portfolioMetrics.cashBalance / effPrice);
      const calculatedQty = Math.max(1, Math.floor(maxAffordable * (pct / 100)));
      setQuantity(calculatedQty.toString());
    } else {
      const calculatedQty = Math.max(0, Math.floor(ownedShares * (pct / 100)));
      setQuantity(calculatedQty.toString());
    }
  };

  const validation = validateOrder({
    symbol: selectedSymbol,
    type: orderSide,
    executionType: orderType,
    quantity: numQty,
    price: limitPrice,
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setOrderFeedback(null);

    const result = executeOrder({
      symbol: selectedSymbol,
      type: orderSide,
      executionType: orderType,
      quantity: numQty,
      price: limitPrice,
    });

    if (result.success) {
      setOrderFeedback({
        type: 'success',
        message: result.filled
          ? `Order filled: ${orderSide} ${numQty} ${selectedSymbol} @ $${result.fillPrice.toFixed(2)}`
          : `Limit order placed: ${orderSide} ${numQty} ${selectedSymbol} @ $${parseFloat(limitPrice).toFixed(2)}`,
      });
      setTimeout(() => setOrderFeedback(null), 5000);
    } else {
      setOrderFeedback({
        type: 'error',
        message: result.reason,
      });
    }
  };

  const isPos = currentStock.change >= 0;

  return (
    <div className="page-container trading-page">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Trading</h1>
        </div>
      </div>

      <div className="trading-workspace-grid">
        {/* Left: Trade Form */}
        <div className="panel-card trade-form-card">
          <div className="trade-side-tabs">
            <button
              type="button"
              className={`trade-tab buy ${orderSide === 'BUY' ? 'active' : ''}`}
              onClick={() => setOrderSide('BUY')}
            >
              Buy
            </button>
            <button
              type="button"
              className={`trade-tab sell ${orderSide === 'SELL' ? 'active' : ''}`}
              onClick={() => setOrderSide('SELL')}
            >
              Sell
            </button>
          </div>

          <form className="trade-form" onSubmit={handleSubmit}>
            {/* Symbol Selector */}
            <div className="form-group">
              <label>Asset</label>
              <div className="select-wrapper">
                <select
                  value={selectedSymbol}
                  onChange={(e) => {
                    setSelectedSymbolOverride(e.target.value);
                    setCustomPrice(null);
                  }}
                  className="clean-select"
                >
                  {stocks.map((s) => (
                    <option key={s.symbol} value={s.symbol}>
                      {s.symbol} — {s.name} (${s.price.toFixed(2)})
                    </option>
                  ))}
                </select>
                <ChevronDown size={16} className="select-arrow" />
              </div>
            </div>

            {/* Order Type */}
            <div className="form-group">
              <label>Order Type</label>
              <div className="order-type-buttons">
                {['MARKET', 'LIMIT'].map((type) => (
                  <button
                    key={type}
                    type="button"
                    className={`type-btn ${orderType === type ? 'active' : ''}`}
                    onClick={() => setOrderType(type)}
                  >
                    {type === 'MARKET' ? 'Market' : 'Limit'}
                  </button>
                ))}
              </div>
            </div>

            {/* Price (if limit) */}
            {orderType === 'LIMIT' && (
              <div className="form-group">
                <label>Limit Price</label>
                <div className="input-with-symbol">
                  <span>$</span>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={limitPrice}
                    onChange={(e) => setCustomPrice(e.target.value)}
                    required
                  />
                </div>
              </div>
            )}

            {/* Quantity */}
            <div className="form-group">
              <div className="label-with-balance">
                <label>Shares</label>
                <span className="balance-hint">
                  {orderSide === 'BUY'
                    ? `Buying Power: $${portfolioMetrics.cashBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                    : `Owned: ${ownedShares} shares`}
                </span>
              </div>
              <input
                type="number"
                min="1"
                max="10000"
                step="1"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                className="clean-input"
                required
              />

              <div className="quick-percent-row">
                {[25, 50, 100].map((pct) => (
                  <button
                    key={pct}
                    type="button"
                    className="pct-btn"
                    onClick={() => handleQuickPercent(pct)}
                  >
                    {pct}%
                  </button>
                ))}
              </div>
            </div>

            {/* Feedback */}
            {orderFeedback && (
              <div className={`clean-feedback ${orderFeedback.type}`}>
                {orderFeedback.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
                <span>{orderFeedback.message}</span>
              </div>
            )}

            {/* Clear Primary Action Button */}
            <button
              type="submit"
              className={`btn-trade-action ${orderSide === 'BUY' ? 'buy' : 'sell'}`}
              disabled={!validation.valid}
            >
              {orderSide} {selectedSymbol}
            </button>
          </form>
        </div>

        {/* Right: Order Summary */}
        <div className="panel-card order-summary-card">
          <div className="summary-stock-header">
            <div>
              <div className="asset-symbol text-lg">{currentStock.symbol}</div>
              <div className="asset-name">{currentStock.name}</div>
            </div>
            <div className="text-right">
              <div className="font-mono text-xl font-semibold">${currentStock.price.toFixed(2)}</div>
              <div className={`text-sm inline-flex items-center gap-1 ${isPos ? 'text-green' : 'text-red'}`}>
                {isPos ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                {isPos ? '+' : ''}{currentStock.changePercent.toFixed(2)}%
              </div>
            </div>
          </div>

          <div className="summary-chart">
            <Sparkline data={currentStock.history} isPositive={isPos} width={420} height={50} />
          </div>

          <h2 className="section-title mt-4">Order Summary</h2>

          <div className="summary-rows-box">
            <div className="summary-row">
              <span className="text-muted">Order Side</span>
              <span className="font-medium">{orderSide === 'BUY' ? 'Buy' : 'Sell'}</span>
            </div>
            <div className="summary-row">
              <span className="text-muted">Execution Price</span>
              <span className="font-mono font-medium">${effPrice.toFixed(2)}</span>
            </div>
            <div className="summary-row">
              <span className="text-muted">Quantity</span>
              <span className="font-mono font-medium">{numQty} shares</span>
            </div>
            <div className="summary-row">
              <span className="text-muted">Commission</span>
              <span className="text-green font-medium">$0.00</span>
            </div>
            <div className="summary-divider"></div>
            <div className="summary-row total">
              <span>Total Estimated</span>
              <span className="font-mono text-lg font-bold">${estimatedTotal.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
