import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useTrading } from '../context/TradingContext';
import {
  LayoutDashboard,
  TrendingUp,
  ArrowLeftRight,
  Briefcase,
  BookOpen,
  Receipt,
  BarChart3,
  DollarSign,
  RotateCcw,
  Zap,
} from 'lucide-react';

export default function Sidebar() {
  const { activeOrders, portfolioMetrics, depositFunds, resetToDemo } = useTrading();
  const [showDepositModal, setShowDepositModal] = useState(false);
  const [depositAmount, setDepositAmount] = useState('5000');

  const handleDeposit = (e) => {
    e.preventDefault();
    if (depositFunds(depositAmount)) {
      setShowDepositModal(false);
      setDepositAmount('5000');
    }
  };

  return (
    <aside className="terminal-sidebar">
      {/* Platform Brand */}
      <div className="sidebar-brand">
        <div className="brand-logo-icon">
          <Zap size={18} className="text-emerald-400" />
        </div>
        <div className="brand-text">
          <h2>Trade<span>Flow</span></h2>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="sidebar-nav">
        <NavLink
          to="/"
          end
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <LayoutDashboard size={18} />
          <span>Dashboard</span>
        </NavLink>

        <NavLink
          to="/live-market"
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <TrendingUp size={18} />
          <span>Live Market</span>
        </NavLink>

        <NavLink
          to="/trading"
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <ArrowLeftRight size={18} />
          <span>Trading</span>
        </NavLink>

        <NavLink
          to="/order-book"
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <BookOpen size={18} />
          <span>Order Book</span>
          {activeOrders.length > 0 && (
            <span className="nav-badge">{activeOrders.length}</span>
          )}
        </NavLink>

        <NavLink
          to="/portfolio"
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <Briefcase size={18} />
          <span>Portfolio</span>
        </NavLink>

        <NavLink
          to="/transactions"
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <Receipt size={18} />
          <span>Transactions</span>
        </NavLink>

        <NavLink
          to="/analytics"
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'active' : ''}`}
        >
          <BarChart3 size={18} />
          <span>Analytics</span>
        </NavLink>
      </nav>

      {/* Account Info & Quick Actions */}
      <div className="sidebar-account-box">
        <div className="account-balance-display">
          <span className="bal-caption">Cash Balance</span>
          <span className="bal-amount">${portfolioMetrics.cashBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
        </div>

        <div className="account-actions">
          <button
            className="action-btn deposit"
            onClick={() => setShowDepositModal(true)}
          >
            <DollarSign size={13} />
            <span>Deposit</span>
          </button>
          <button
            className="action-btn reset"
            onClick={resetToDemo}
            title="Reset to demo data"
          >
            <RotateCcw size={13} />
            <span>Reset</span>
          </button>
        </div>
      </div>

      {/* Deposit Modal */}
      {showDepositModal && (
        <div className="modal-overlay" onClick={() => setShowDepositModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Deposit Trading Capital</h3>
              <button className="modal-close" onClick={() => setShowDepositModal(false)}>✕</button>
            </div>
            <form onSubmit={handleDeposit}>
              <p className="modal-description">
                Credit additional virtual capital to your trading account to test higher purchasing power and risk limits.
              </p>
              <div className="form-group">
                <label>Deposit Amount (USD)</label>
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
                {['2500', '5000', '10000', '25000'].map((amt) => (
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
                  Confirm Deposit
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </aside>
  );
}
