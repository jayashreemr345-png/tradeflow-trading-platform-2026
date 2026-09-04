import { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import {
  Search,
  Download,
} from 'lucide-react';

export default function Transactions() {
  const { transactions } = useTrading();
  const [search, setSearch] = useState('');
  const [filterType, setFilterType] = useState('ALL');

  const filteredTransactions = transactions.filter((tx) => {
    const matchSearch =
      tx.id.toLowerCase().includes(search.toLowerCase()) ||
      tx.symbol.toLowerCase().includes(search.toLowerCase());
    const matchType = filterType === 'ALL' || tx.type === filterType;
    return matchSearch && matchType;
  });

  const handleExportCSV = () => {
    const headers = ['Type', 'Asset', 'Quantity', 'Amount', 'Date', 'Status'];
    const rows = filteredTransactions.map((t) => [
      t.type,
      t.symbol,
      t.shares,
      t.total,
      t.timestamp,
      t.status,
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `tradeflow_transactions.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="page-container transactions-page">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Transactions</h1>
        </div>

        <button className="btn-secondary" onClick={handleExportCSV}>
          <Download size={15} />
          <span>Export CSV</span>
        </button>
      </div>

      {/* Controls Bar: Search & Type Tabs */}
      <div className="filter-controls-card">
        <div className="search-input-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Search by symbol..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button className="search-clear-btn" onClick={() => setSearch('')}>✕</button>
          )}
        </div>

        <div className="sector-scroll-row">
          {['ALL', 'BUY', 'SELL', 'DEPOSIT'].map((type) => (
            <button
              key={type}
              className={`sector-pill ${filterType === type ? 'active' : ''}`}
              onClick={() => setFilterType(type)}
            >
              {type === 'ALL' ? 'All' : type === 'BUY' ? 'Buy' : type === 'SELL' ? 'Sell' : 'Deposit'}
            </button>
          ))}
        </div>
      </div>

      {/* Clean Transactions Table */}
      <div className="panel-card no-padding">
        {filteredTransactions.length === 0 ? (
          <div className="empty-state-simple">
            <p>No transactions found.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="clean-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Asset</th>
                  <th className="text-right">Quantity</th>
                  <th className="text-right">Amount</th>
                  <th className="text-right">Date</th>
                  <th className="text-center">Status</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((tx) => {
                  const isBuy = tx.type === 'BUY';
                  const isDeposit = tx.type === 'DEPOSIT';

                  return (
                    <tr key={tx.id}>
                      <td>
                        <span className={`pill-mini ${isDeposit ? 'deposit' : isBuy ? 'buy' : 'sell'}`}>
                          {tx.type}
                        </span>
                      </td>
                      <td className="font-medium">
                        {tx.symbol}
                      </td>
                      <td className="font-mono text-right text-muted">
                        {isDeposit ? '—' : tx.shares}
                      </td>
                      <td className="font-mono text-right font-medium">
                        {isDeposit ? '+' : ''}${tx.total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </td>
                      <td className="font-mono text-right text-muted text-xs">
                        {tx.timestamp}
                      </td>
                      <td className="text-center">
                        <span className="status-badge-clean">{tx.status}</span>
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
