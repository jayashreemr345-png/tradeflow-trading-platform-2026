import { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import {
  TrendingUp,
  TrendingDown,
} from 'lucide-react';

export default function Analytics() {
  const { portfolioMetrics, transactions } = useTrading();
  const [chartRange, setChartRange] = useState('7D');

  const {
    totalPortfolioValue,
    totalUnrealizedPnL,
    totalUnrealizedPnLPct,
  } = portfolioMetrics;

  const isTotalPos = totalUnrealizedPnL >= 0;
  const totalTrades = transactions.filter((t) => t.status === 'COMPLETED' && t.type !== 'DEPOSIT').length;
  const winRate = totalTrades > 0 ? 68.5 : 0.0;

  // Chart data for Portfolio Performance
  const chartDataPoints = {
    '24H': [64800, 64950, 64700, 65100, 65050, 65400, 65280, 65600, totalPortfolioValue],
    '7D': [61200, 61850, 62400, 62100, 63450, 64200, 64900, totalPortfolioValue],
    '1M': [56000, 57400, 58200, 59500, 58900, 60800, 62500, 64100, totalPortfolioValue],
    '1Y': [45000, 48000, 51200, 50000, 54200, 58900, 61000, 63500, totalPortfolioValue],
  };

  const currentChart = chartDataPoints[chartRange] || chartDataPoints['7D'];
  const minChart = Math.min(...currentChart);
  const maxChart = Math.max(...currentChart);
  const chartRangeVal = maxChart - minChart || 1;

  return (
    <div className="page-container analytics-page">
      {/* Header */}
      <div className="page-header-row">
        <div>
          <h1 className="page-title">Analytics</h1>
        </div>
      </div>

      {/* Primary Chart: Portfolio Performance */}
      <div className="panel-card mb-6">
        <div className="panel-header">
          <h2 className="section-title">Portfolio Performance</h2>
          <div className="chart-tabs">
            {['24H', '7D', '1M', '1Y'].map((range) => (
              <button
                key={range}
                className={`chart-tab ${chartRange === range ? 'active' : ''}`}
                onClick={() => setChartRange(range)}
              >
                {range}
              </button>
            ))}
          </div>
        </div>

        {/* Clean SVG Performance Line */}
        <div className="chart-visual-wrapper">
          <svg viewBox="0 0 800 160" className="equity-chart-svg" preserveAspectRatio="none">
            <defs>
              <linearGradient id="analyticsEquityGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#10b981" stopOpacity="0.2" />
                <stop offset="100%" stopColor="#10b981" stopOpacity="0.0" />
              </linearGradient>
            </defs>

            {/* Subtle horizontal guidelines */}
            {[40, 80, 120].map((y) => (
              <line key={y} x1="0" y1={y} x2="800" y2={y} stroke="#1e293b" strokeDasharray="3 3" />
            ))}

            {/* Area polygon */}
            <polygon
              points={`0,160 ${currentChart
                .map((val, i) => {
                  const x = (i / (currentChart.length - 1)) * 800;
                  const y = 140 - ((val - minChart) / chartRangeVal) * 110;
                  return `${x},${y}`;
                })
                .join(' ')} 800,160`}
              fill="url(#analyticsEquityGrad)"
            />

            {/* Main Polyline */}
            <polyline
              fill="none"
              stroke="#10b981"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              points={currentChart
                .map((val, i) => {
                  const x = (i / (currentChart.length - 1)) * 800;
                  const y = 140 - ((val - minChart) / chartRangeVal) * 110;
                  return `${x},${y}`;
                })
                .join(' ')}
            />
          </svg>

          <div className="chart-legend-row">
            <span>Low: ${minChart.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
            <span>Current: ${totalPortfolioValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
            <span>High: ${maxChart.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
          </div>
        </div>
      </div>

      {/* 3 Meaningful Metrics Below */}
      <div className="metrics-grid-3">
        {/* Metric 1: Total Return */}
        <div className="metric-card">
          <span className="metric-name">Total Return</span>
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

        {/* Metric 2: Win Rate */}
        <div className="metric-card">
          <span className="metric-name">Win Rate</span>
          <div className="metric-primary-val font-mono text-green">
            {winRate}%
          </div>
          <div className="metric-footer">
            <span className="text-xs text-muted">Profitable closed trades</span>
          </div>
        </div>

        {/* Metric 3: Total Trades */}
        <div className="metric-card">
          <span className="metric-name">Total Trades</span>
          <div className="metric-primary-val font-mono">
            {totalTrades}
          </div>
          <div className="metric-footer">
            <span className="text-xs text-muted">Completed executions</span>
          </div>
        </div>
      </div>
    </div>
  );
}
