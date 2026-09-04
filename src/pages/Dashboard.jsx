import { useNavigate } from 'react-router-dom';
import { useTrading } from '../context/TradingContext';
import Sparkline from '../components/Sparkline';
import {
  TrendingUp,
  TrendingDown,
  ArrowRight,
  ArrowUpRight,
  ArrowDownRight,
  DollarSign,
} from 'lucide-react';

export default function Dashboard() {
  const navigate = useNavigate();
  const { stocks, portfolioMetrics, activeOrders, transactions } = useTrading();

  const { totalPortfolioValue, totalUnrealizedPnL, totalUnrealizedPnLPct, todayPnL, todayPnLPct } = portfolioMetrics;

  const isDayPositive = todayPnL >= 0;
  const isTotalPositive = totalUnrealizedPnL >= 0;

  // Maximum 4 stocks for clean market overview
  const featuredStocks = stocks.slice(0, 4);

  // Latest 3 activities
  const recentActivities = transactions.slice(0, 3);

  return (
    <div className="page-container dashboard-page">
      {/* Page Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Dashboard</h1>
        </div>
      </div>

      {/* 3 Core Metric Cards */}
      <div className="metrics-grid-3">
        {/* Card 1: Portfolio Value */}
        <div className="metric-card">
          <span className="metric-name">Portfolio Value</span>
          <div className="metric-primary-val">
            ${totalPortfolioValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="metric-footer">
            <span className={`metric-tag ${isTotalPositive ? 'positive' : 'negative'}`}>
              {isTotalPositive ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
              {isTotalPositive ? '+' : ''}${Math.abs(totalUnrealizedPnL).toFixed(2)} ({isTotalPositive ? '+' : ''}{totalUnrealizedPnLPct.toFixed(2)}%)
            </span>
          </div>
        </div>

        {/* Card 2: Today's P&L */}
        <div className="metric-card">
          <span className="metric-name">Today's P&L</span>
          <div className={`metric-primary-val ${isDayPositive ? 'text-green' : 'text-red'}`}>
            {isDayPositive ? '+' : '-'}${Math.abs(todayPnL).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="metric-footer">
            <span className={`metric-tag ${isDayPositive ? 'positive' : 'negative'}`}>
              {isDayPositive ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
              {isDayPositive ? '+' : ''}{todayPnLPct.toFixed(2)}%
            </span>
          </div>
        </div>

        {/* Card 3: Active Orders */}
        <div
          className="metric-card clickable"
          onClick={() => navigate('/order-book')}
        >
          <span className="metric-name">Active Orders</span>
          <div className="metric-primary-val">
            {activeOrders.length}
          </div>
          <div className="metric-footer">
            <span className="metric-link">
              View Order Book →
            </span>
          </div>
        </div>
      </div>

      {/* Market Overview */}
      <div className="panel-section">
        <div className="panel-header">
          <h2 className="section-title">Market Overview</h2>
          <button className="panel-link-btn" onClick={() => navigate('/live-market')}>
            View all ({stocks.length}) <ArrowRight size={14} />
          </button>
        </div>

        <div className="market-cards-grid">
          {featuredStocks.map((stock) => {
            const isPos = stock.change >= 0;
            return (
              <div key={stock.symbol} className="market-card">
                <div className="market-card-top">
                  <div>
                    <div className="market-card-symbol">{stock.symbol}</div>
                    <div className="market-card-name">{stock.name}</div>
                  </div>
                  <div className="text-right">
                    <div className="market-card-price">${stock.price.toFixed(2)}</div>
                    <div className={`market-card-change ${isPos ? 'pos' : 'neg'}`}>
                      {isPos ? '+' : ''}{stock.changePercent.toFixed(2)}%
                    </div>
                  </div>
                </div>

                <div className="market-card-chart">
                  <Sparkline data={stock.history} isPositive={isPos} width={240} height={36} />
                </div>

                <div className="market-card-footer">
                  <button
                    className="btn-trade-simple"
                    onClick={() => navigate(`/trading?symbol=${stock.symbol}`)}
                  >
                    Trade
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="panel-section">
        <div className="panel-header">
          <h2 className="section-title">Recent Activity</h2>
          <button className="panel-link-btn" onClick={() => navigate('/transactions')}>
            View all <ArrowRight size={14} />
          </button>
        </div>

        <div className="panel-card no-padding">
          {recentActivities.length === 0 ? (
            <div className="empty-state-simple">
              <p>No recent activity.</p>
            </div>
          ) : (
            <div className="activity-list">
              {recentActivities.map((tx) => {
                const isBuy = tx.type === 'BUY';
                const isDeposit = tx.type === 'DEPOSIT';
                return (
                  <div key={tx.id} className="activity-item">
                    <div className={`activity-pill ${isDeposit ? 'deposit' : isBuy ? 'buy' : 'sell'}`}>
                      {isDeposit ? <DollarSign size={14} /> : isBuy ? <ArrowDownRight size={14} /> : <ArrowUpRight size={14} />}
                      <span>{tx.type}</span>
                    </div>

                    <div className="activity-info">
                      <span className="activity-title">
                        {isDeposit ? 'Deposit' : `${tx.shares} shares of ${tx.symbol}`}
                      </span>
                      <span className="activity-time">{tx.timestamp}</span>
                    </div>

                    <div className="activity-amount font-mono">
                      {isDeposit ? `+$${tx.total.toLocaleString(undefined, { minimumFractionDigits: 2 })}` : `$${tx.total.toLocaleString(undefined, { minimumFractionDigits: 2 })}`}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
