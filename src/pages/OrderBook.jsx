import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTrading } from '../context/TradingContext';
import { ChevronDown } from 'lucide-react';

export default function OrderBook() {
  const navigate = useNavigate();
  const { stocks, orderBooks, activeOrders, cancelOrder } = useTrading();
  const [selectedSymbol, setSelectedSymbol] = useState('AAPL');

  const book = orderBooks[selectedSymbol] || { bids: [], asks: [] };

  const sortedAsks = [...book.asks].sort((a, b) => a.price - b.price).slice(0, 5);
  const sortedBids = [...book.bids].sort((a, b) => b.price - a.price).slice(0, 5);

  return (
    <div className="page-container order-book-page">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Order Book</h1>
        </div>

        {/* Instrument Selector */}
        <div className="select-wrapper">
          <select
            value={selectedSymbol}
            onChange={(e) => setSelectedSymbol(e.target.value)}
            className="clean-select"
          >
            {stocks.map((s) => (
              <option key={s.symbol} value={s.symbol}>
                {s.symbol} — ${s.price.toFixed(2)}
              </option>
            ))}
          </select>
          <ChevronDown size={16} className="select-arrow" />
        </div>
      </div>

      {/* Open Orders */}
      <div className="panel-card no-padding">
        <div className="panel-header-padded">
          <h2 className="section-title">Open Orders</h2>
        </div>

        {activeOrders.length === 0 ? (
          <div className="empty-state-simple">
            <p>No open orders.</p>
            <button
              className="btn-primary mt-3"
              onClick={() => navigate(`/trading?symbol=${selectedSymbol}`)}
            >
              Place Order
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="clean-table">
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Type</th>
                  <th className="text-right">Price</th>
                  <th className="text-right">Quantity</th>
                  <th className="text-center">Status</th>
                  <th className="text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {activeOrders.map((ord) => (
                  <tr key={ord.id}>
                    <td className="font-bold">{ord.symbol}</td>
                    <td>
                      <span className={`pill-mini ${ord.type === 'BUY' ? 'buy' : 'sell'}`}>
                        {ord.type}
                      </span>
                    </td>
                    <td className="font-mono text-right font-medium">${ord.price.toFixed(2)}</td>
                    <td className="font-mono text-right">{ord.quantity}</td>
                    <td className="text-center">
                      <span className="status-badge-clean">{ord.status}</span>
                    </td>
                    <td className="text-right">
                      <button
                        className="btn-cancel-clean"
                        onClick={() => cancelOrder(ord.id)}
                      >
                        Cancel
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Market Depth for Selected Symbol */}
      <div className="panel-section">
        <h2 className="section-title mb-4">Market Depth ({selectedSymbol})</h2>

        <div className="order-depth-grid">
          {/* Bids */}
          <div className="panel-card no-padding">
            <div className="panel-header-padded">
              <span className="text-sm font-medium text-green">Bids (Buy)</span>
            </div>
            <table className="clean-table">
              <thead>
                <tr>
                  <th>Price</th>
                  <th className="text-right">Quantity</th>
                </tr>
              </thead>
              <tbody>
                {sortedBids.map((bid) => (
                  <tr key={bid.id}>
                    <td className="font-mono text-green font-medium">${bid.price.toFixed(2)}</td>
                    <td className="font-mono text-right text-muted">{bid.quantity}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Asks */}
          <div className="panel-card no-padding">
            <div className="panel-header-padded">
              <span className="text-sm font-medium text-red">Asks (Sell)</span>
            </div>
            <table className="clean-table">
              <thead>
                <tr>
                  <th>Price</th>
                  <th className="text-right">Quantity</th>
                </tr>
              </thead>
              <tbody>
                {sortedAsks.map((ask) => (
                  <tr key={ask.id}>
                    <td className="font-mono text-red font-medium">${ask.price.toFixed(2)}</td>
                    <td className="font-mono text-right text-muted">{ask.quantity}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
