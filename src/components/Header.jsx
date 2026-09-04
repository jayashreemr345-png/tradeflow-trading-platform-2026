import { useTrading } from '../context/TradingContext';
import { RefreshCw, Play, Pause } from 'lucide-react';

export default function Header() {
  const { stocks, portfolioMetrics, isLive, toggleLiveSimulation, refreshMarket } = useTrading();

  // Pick top 3-4 important market items
  const tickerStocks = stocks.slice(0, 4);

  return (
    <header className="terminal-header">
      {/* Minimal Market Ticker */}
      <div className="ticker-tape">
        <div className="ticker-items">
          {tickerStocks.map((stock) => {
            const isPos = stock.change >= 0;
            return (
              <div key={stock.symbol} className="ticker-item">
                <span className="ticker-symbol">{stock.symbol}</span>
                <span className={`ticker-change ${isPos ? 'pos' : 'neg'}`}>
                  {isPos ? '+' : ''}{stock.changePercent.toFixed(1)}%
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Subtle Account Values & Controls */}
      <div className="header-actions">
        <div className="header-metric">
          <span className="metric-label">Buying Power</span>
          <span className="metric-val">${portfolioMetrics.cashBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
        </div>

        <div className="header-metric">
          <span className="metric-label">Portfolio</span>
          <span className="metric-val highlight">${portfolioMetrics.totalPortfolioValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
        </div>

        <div className="header-controls">
          <button
            className={`feed-toggle-btn ${isLive ? 'active' : ''}`}
            onClick={toggleLiveSimulation}
            title={isLive ? 'Pause live market' : 'Resume live market'}
          >
            {isLive ? <Pause size={13} /> : <Play size={13} />}
            <span>{isLive ? 'Live' : 'Paused'}</span>
          </button>

          <button
            className="refresh-btn"
            onClick={refreshMarket}
            title="Refresh market data"
          >
            <RefreshCw size={13} />
          </button>
        </div>
      </div>
    </header>
  );
}
