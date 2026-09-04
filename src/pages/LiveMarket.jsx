import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTrading } from '../context/TradingContext';
import {
  Search,
  RefreshCw,
  TrendingUp,
  TrendingDown,
} from 'lucide-react';

export default function LiveMarket() {
  const navigate = useNavigate();
  const { stocks, refreshMarket } = useTrading();

  const [search, setSearch] = useState('');
  const [selectedSector, setSelectedSector] = useState('ALL');

  const sectors = ['ALL', 'Technology', 'Semiconductors', 'Communication Services', 'Consumer Cyclical'];

  const filteredStocks = stocks
    .filter((stock) => {
      const matchSearch =
        stock.symbol.toLowerCase().includes(search.toLowerCase()) ||
        stock.name.toLowerCase().includes(search.toLowerCase());
      const matchSector = selectedSector === 'ALL' || stock.sector === selectedSector;
      return matchSearch && matchSector;
    })
    .slice(0, 6);

  return (
    <div className="page-container live-market-page">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Live Market</h1>
        </div>

        <button className="btn-secondary" onClick={refreshMarket} title="Refresh market data">
          <RefreshCw size={14} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Controls Bar: Search & Filter */}
      <div className="filter-controls-card">
        <div className="search-input-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Search symbol or company..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button className="search-clear-btn" onClick={() => setSearch('')}>✕</button>
          )}
        </div>

        <div className="sector-scroll-row">
          {sectors.map((sec) => (
            <button
              key={sec}
              className={`sector-pill ${selectedSector === sec ? 'active' : ''}`}
              onClick={() => setSelectedSector(sec)}
            >
              {sec}
            </button>
          ))}
        </div>
      </div>

      {/* Clean Market Table */}
      <div className="panel-card no-padding">
        {filteredStocks.length === 0 ? (
          <div className="empty-state-simple">
            <p>No stocks match your filter.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="clean-table">
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th className="text-right">Price</th>
                  <th className="text-right">Change</th>
                  <th className="text-center">Signal</th>
                  <th className="text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredStocks.map((stock) => {
                  const isPos = stock.change >= 0;
                  return (
                    <tr key={stock.symbol}>
                      <td>
                        <div className="asset-cell">
                          <span className="asset-symbol">{stock.symbol}</span>
                          <span className="asset-name">{stock.name}</span>
                        </div>
                      </td>
                      <td className="font-mono text-right font-medium">
                        ${stock.price.toFixed(2)}
                      </td>
                      <td className="text-right font-mono">
                        <span className={`inline-flex items-center gap-1 ${isPos ? 'text-green font-medium' : 'text-red font-medium'}`}>
                          {isPos ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
                          {isPos ? '+' : ''}{stock.changePercent.toFixed(2)}%
                        </span>
                      </td>
                      <td className="text-center">
                        <span className={`signal-tag ${isPos ? 'bullish' : 'bearish'}`}>
                          {isPos ? 'Bullish' : 'Bearish'}
                        </span>
                      </td>
                      <td className="text-right">
                        <button
                          className="btn-subtle"
                          onClick={() => navigate(`/trading?symbol=${stock.symbol}`)}
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
    </div>
  );
}
