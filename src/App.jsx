import { Routes, Route } from 'react-router-dom';
import './App.css';

import { TradingProvider } from './context/TradingContext';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import NotificationToast from './components/NotificationToast';

import Dashboard from './pages/Dashboard';
import LiveMarket from './pages/LiveMarket';
import Trading from './pages/Trading';
import Portfolio from './pages/Portfolio';
import OrderBook from './pages/OrderBook';
import Transactions from './pages/Transactions';
import Analytics from './pages/Analytics';

export default function App() {
  return (
    <TradingProvider>
      <div className="app-shell">
        {/* Persistent Pro Sidebar */}
        <Sidebar />

        {/* Main Terminal Area */}
        <div className="terminal-main-area">
          {/* Top Ticker & Status Bar */}
          <Header />

          {/* Dynamic Page Router */}
          <main className="terminal-page-outlet">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/live-market" element={<LiveMarket />} />
              <Route path="/trading" element={<Trading />} />
              <Route path="/portfolio" element={<Portfolio />} />
              <Route path="/order-book" element={<OrderBook />} />
              <Route path="/transactions" element={<Transactions />} />
              <Route path="/analytics" element={<Analytics />} />
            </Routes>
          </main>
        </div>

        {/* Global Floating Toast Notifications */}
        <NotificationToast />
      </div>
    </TradingProvider>
  );
}
