import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTrading } from '../context/TradingContext';
import {
  TrendingUp,
  TrendingDown,
  Plus,
} from 'lucide-react';

export default function Portfolio() {
  const navigate = useNavigate();
  const { portfolioMetrics, depositFunds } = useTrading();
  const [showDepositModal, setShowDepositModal] = useState(false);
  const [depositAmount, setDepositAmount] = useState('5000');

  const {
    cashBalance,
    totalPortfolioValue,
    totalUnrealizedPnL,
    totalUnrealizedPnLPct,
    holdings,
  } = portfolioMetrics;

  const isTotalPos = totalUnrealizedPnL >= 0;

  // Cash allocation %
  const cashPercent = totalPortfolioValue > 0 ? Number(((cashBalance / totalPortfolioValue) * 100).toFixed(1)) : 100;

  const handleDepositSubmit = (e) => {
    e.preventDefault();
    if (depositFunds(depositAmount)) {
      setShowDepositModal(false);
    }
  };

  const colors = ['#10b981', '#38bdf8', '#818cf8', '#f59e0b', '#ec4899', '#a78bfa'];

  return (
    <div className="page-container portfolio-page">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Portfolio</h1>
        </div>

        <button className="btn-secondary" onClick={() => setShowDepositModal(true)}>
          <Plus size={15} />
          <span>Deposit</span>
        </button>
      </div>

      {/* Three Main Metrics */}
      <div className="metrics-grid-3">
        <div className="metric-card">
          <span className="metric-name">Portfolio Value</span>
          <div className="metric-primary-val font-mono">
            ${totalPortfolioValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>

        <div className="metric-card">
          <span className="metric-name">Unrealized P&L</span>
          <div className={`metric-primary-val font-mono ${isTotalPos ? 'text-green' : 'text-red'}`}>
            {isTotalPos ? '+' : '-'}${Math.abs(totalUnrealizedPnL).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="metric-footer">
            <span className={`metric-tag ${isTotalPos ? 'positive' : 'negative'}`}>
              {isTotalPos ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
              {isTotalPos ? '+' : ''}{totalUnrealizedPnLPct.toFixed(2)}%
            </span>
          </div>
        </div>

        <div className="metric-card">
          <span className="metric-name">Cash Balance</span>
          <div className="metric-primary-val font-mono">
            ${cashBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>
      </div>

      {/* Asset Allocation */}
      <div className="panel-card">
        <h2 className="section-title">Asset Allocation</h2>

        <div className="allocation-bar-container">
          <div
            className="alloc-segment"
            style={{ width: `${cashPercent}%`, backgroundColor: '#475569' }}
            title={`Cash: ${cashPercent}%`}
          />
          {holdings.map((h, i) => (
            <div
              key={h.symbol}
              className="alloc-segment"
              style={{ width: `${h.allocationPercent}%`, backgroundColor: colors[i % colors.length] }}
              title={`${h.symbol}: ${h.allocationPercent}%`}
            />
          ))}
        </div>

        <div className="alloc-legend-row">
          <div className="legend-item">
            <span className="legend-dot" style={{ backgroundColor: '#475569' }}></span>
            <span className="legend-label">Cash</span>
            <span className="legend-val font-mono">{cashPercent}%</span>
          </div>
          {holdings.map((h, i) => (
            <div key={h.symbol} className="legend-item">
              <span className="legend-dot" style={{ backgroundColor: colors[i % colors.length] }}></span>
              <span className="legend-label">{h.symbol}</span>
              <span className="legend-val font-mono">{h.allocationPercent}%</span>
            </div>
          ))}
        </div>
      </div>

      {/* Holdings Table */}
      <div className="panel-card no-padding">
        <div className="panel-header-padded">
          <h2 className="section-title">Holdings</h2>
        </div>

        {holdings.length === 0 ? (
          <div className="empty-state-simple">
            <p>No active holdings in your portfolio.</p>
            <button className="btn-primary mt-3" onClick={() => navigate('/trading')}>
              Start Trading
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="clean-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th className="text-right">Shares</th>
                  <th className="text-right">Avg Price</th>
                  <th className="text-right">Current Price</th>
                  <th className="text-right">P&L</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {holdings.map((h) => {
                  const isPos = h.unrealizedPnL >= 0;
                  return (
                    <tr key={h.symbol}>
                      <td>
                        <div className="asset-cell">
                          <span className="asset-symbol">{h.symbol}</span>
                          <span className="asset-name">{h.name}</span>
                        </div>
                      </td>
                      <td className="font-mono text-right">{h.shares}</td>
                      <td className="font-mono text-right text-muted">${h.avgBuyPrice.toFixed(2)}</td>
                      <td className="font-mono text-right font-medium">${h.currentPrice.toFixed(2)}</td>
                      <td className="text-right font-mono">
                        <span className={isPos ? 'text-green font-medium' : 'text-red font-medium'}>
                          {isPos ? '+' : ''}${h.unrealizedPnL.toFixed(2)} ({isPos ? '+' : ''}{h.unrealizedPnLPercent.toFixed(2)}%)
                        </span>
                      </td>
                      <td className="text-right">
                        <button
                          className="btn-subtle"
                          onClick={() => navigate(`/trading?symbol=${h.symbol}`)}
                        >
                          Trade
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Deposit Modal */}
      {showDepositModal && (
        <div className="modal-overlay" onClick={() => setShowDepositModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Deposit Funds</h3>
              <button className="modal-close" onClick={() => setShowDepositModal(false)}>✕</button>
            </div>
            <form onSubmit={handleDepositSubmit}>
              <div className="form-group">
                <label>Amount (USD)</label>
                <div className="input-with-symbol">
                  <span>$</span>
                  <input
                    type="number"
                    step="100"
                    min="100"
                    max="1000000"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="quick-amount-buttons">
                {['1000', '5000', '10000', '25000'].map((amt) => (
                  <button
                    key={amt}
                    type="button"
                    className="quick-amt-btn"
                    onClick={() => setDepositAmount(amt)}
                  >
                    +${Number(amt).toLocaleString()}
                  </button>
                ))}
              </div>
              <div className="modal-buttons">
                <button type="button" className="btn-secondary" onClick={() => setShowDepositModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Confirm
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
