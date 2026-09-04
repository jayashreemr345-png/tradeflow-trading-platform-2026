import { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';

const INITIAL_STOCKS = [
  {
    symbol: 'AAPL',
    name: 'Apple Inc.',
    sector: 'Technology',
    price: 228.50,
    previousClose: 226.05,
    open: 226.50,
    high: 229.80,
    low: 225.90,
    volume: 52410200,
    change: 2.45,
    changePercent: 1.08,
    bid: 228.45,
    ask: 228.55,
    history: [224.2, 224.8, 225.5, 226.1, 225.8, 226.4, 227.0, 226.8, 227.5, 228.1, 227.9, 228.5],
  },
  {
    symbol: 'NVDA',
    name: 'NVIDIA Corporation',
    sector: 'Semiconductors',
    price: 124.75,
    previousClose: 120.85,
    open: 121.50,
    high: 126.10,
    low: 121.20,
    volume: 84120300,
    change: 3.90,
    changePercent: 3.23,
    bid: 124.70,
    ask: 124.80,
    history: [118.5, 119.2, 120.0, 120.8, 121.4, 122.5, 123.1, 122.9, 123.8, 124.2, 124.5, 124.75],
  },
  {
    symbol: 'MSFT',
    name: 'Microsoft Corp.',
    sector: 'Technology',
    price: 442.20,
    previousClose: 445.60,
    open: 446.00,
    high: 447.10,
    low: 441.50,
    volume: 24105000,
    change: -3.40,
    changePercent: -0.76,
    bid: 442.15,
    ask: 442.25,
    history: [447.0, 446.5, 446.2, 445.8, 445.0, 444.2, 443.5, 443.0, 442.8, 442.5, 442.1, 442.2],
  },
  {
    symbol: 'AMZN',
    name: 'Amazon.com Inc.',
    sector: 'Consumer Cyclical',
    price: 198.60,
    previousClose: 196.20,
    open: 196.80,
    high: 199.40,
    low: 196.50,
    volume: 38290100,
    change: 2.40,
    changePercent: 1.22,
    bid: 198.55,
    ask: 198.65,
    history: [194.0, 194.8, 195.5, 196.2, 196.9, 197.3, 197.8, 197.5, 198.0, 198.2, 198.4, 198.6],
  },
  {
    symbol: 'TSLA',
    name: 'Tesla Inc.',
    sector: 'Automotive / Clean Tech',
    price: 215.30,
    previousClose: 218.40,
    open: 217.50,
    high: 219.00,
    low: 213.80,
    volume: 68420100,
    change: -3.10,
    changePercent: -1.42,
    bid: 215.20,
    ask: 215.40,
    history: [222.0, 220.5, 219.8, 218.4, 217.2, 216.5, 215.9, 216.2, 215.5, 214.8, 215.0, 215.3],
  },
  {
    symbol: 'GOOGL',
    name: 'Alphabet Inc.',
    sector: 'Communication Services',
    price: 182.40,
    previousClose: 181.10,
    open: 181.50,
    high: 183.25,
    low: 181.00,
    volume: 29540000,
    change: 1.30,
    changePercent: 0.72,
    bid: 182.35,
    ask: 182.45,
    history: [180.2, 180.8, 181.1, 181.4, 181.9, 182.0, 181.7, 182.2, 182.5, 182.3, 182.4],
  },
  {
    symbol: 'META',
    name: 'Meta Platforms Inc.',
    sector: 'Communication Services',
    price: 512.90,
    previousClose: 504.80,
    open: 506.00,
    high: 515.20,
    low: 505.50,
    volume: 18940000,
    change: 8.10,
    changePercent: 1.60,
    bid: 512.80,
    ask: 513.00,
    history: [500.0, 502.5, 504.8, 506.2, 508.4, 509.8, 511.0, 510.5, 511.8, 512.5, 512.9],
  },
  {
    symbol: 'JPM',
    name: 'JPMorgan Chase & Co.',
    sector: 'Financial Services',
    price: 218.10,
    previousClose: 216.90,
    open: 217.20,
    high: 219.00,
    low: 216.80,
    volume: 12450000,
    change: 1.20,
    changePercent: 0.55,
    bid: 218.05,
    ask: 218.15,
    history: [215.5, 216.0, 216.9, 217.1, 217.4, 217.8, 217.5, 218.0, 218.2, 218.1],
  },
  {
    symbol: 'SPY',
    name: 'SPDR S&P 500 ETF Trust',
    sector: 'Index ETF',
    price: 564.30,
    previousClose: 561.80,
    open: 562.50,
    high: 565.40,
    low: 562.00,
    volume: 45200000,
    change: 2.50,
    changePercent: 0.44,
    bid: 564.25,
    ask: 564.35,
    history: [560.5, 561.2, 561.8, 562.4, 563.0, 563.5, 563.8, 564.0, 564.2, 564.3],
  },
  {
    symbol: 'QQQ',
    name: 'Invesco QQQ Trust',
    sector: 'Tech ETF',
    price: 488.90,
    previousClose: 484.50,
    open: 485.20,
    high: 490.10,
    low: 484.80,
    volume: 38700000,
    change: 4.40,
    changePercent: 0.91,
    bid: 488.85,
    ask: 488.95,
    history: [482.0, 483.5, 484.5, 485.8, 486.9, 487.5, 488.0, 488.4, 488.7, 488.9],
  },
];

// Generate standard order book liquidity for a stock
const generateInitialBook = (stock) => {
  const current = stock.price;
  const bids = [
    { id: `bid-${stock.symbol}-1`, type: 'BUY', price: Number((current - 0.05).toFixed(2)), quantity: 120, total: 120, timestamp: '10:42:15', accountId: 'MM-A' },
    { id: `bid-${stock.symbol}-2`, type: 'BUY', price: Number((current - 0.15).toFixed(2)), quantity: 250, total: 370, timestamp: '10:41:50', accountId: 'MM-B' },
    { id: `bid-${stock.symbol}-3`, type: 'BUY', price: Number((current - 0.30).toFixed(2)), quantity: 480, total: 850, timestamp: '10:40:22', accountId: 'INST-1' },
    { id: `bid-${stock.symbol}-4`, type: 'BUY', price: Number((current - 0.55).toFixed(2)), quantity: 650, total: 1500, timestamp: '10:39:10', accountId: 'INST-2' },
    { id: `bid-${stock.symbol}-5`, type: 'BUY', price: Number((current - 0.85).toFixed(2)), quantity: 1200, total: 2700, timestamp: '10:38:05', accountId: 'RETL-1' },
  ];

  const asks = [
    { id: `ask-${stock.symbol}-1`, type: 'SELL', price: Number((current + 0.05).toFixed(2)), quantity: 95, total: 95, timestamp: '10:42:20', accountId: 'MM-C' },
    { id: `ask-${stock.symbol}-2`, type: 'SELL', price: Number((current + 0.18).toFixed(2)), quantity: 310, total: 405, timestamp: '10:41:45', accountId: 'MM-D' },
    { id: `ask-${stock.symbol}-3`, type: 'SELL', price: Number((current + 0.35).toFixed(2)), quantity: 540, total: 945, timestamp: '10:40:11', accountId: 'INST-3' },
    { id: `ask-${stock.symbol}-4`, type: 'SELL', price: Number((current + 0.60).toFixed(2)), quantity: 720, total: 1665, timestamp: '10:39:30', accountId: 'INST-4' },
    { id: `ask-${stock.symbol}-5`, type: 'SELL', price: Number((current + 0.90).toFixed(2)), quantity: 1500, total: 3165, timestamp: '10:38:40', accountId: 'RETL-2' },
  ];

  return { bids, asks };
};

const INITIAL_BOOKS = INITIAL_STOCKS.reduce((acc, stock) => {
  acc[stock.symbol] = generateInitialBook(stock);
  return acc;
}, {});

const INITIAL_HOLDINGS = [
  { symbol: 'AAPL', name: 'Apple Inc.', shares: 35, avgBuyPrice: 218.40 },
  { symbol: 'NVDA', name: 'NVIDIA Corp.', shares: 50, avgBuyPrice: 112.50 },
  { symbol: 'MSFT', name: 'Microsoft Corp.', shares: 20, avgBuyPrice: 425.00 },
  { symbol: 'AMZN', name: 'Amazon.com Inc.', shares: 25, avgBuyPrice: 188.20 },
];

const INITIAL_TRANSACTIONS = [
  {
    id: 'TX-89410',
    orderId: 'ORD-7721',
    symbol: 'NVDA',
    type: 'BUY',
    shares: 20,
    price: 112.50,
    total: 2250.00,
    fee: 0.00,
    timestamp: '2026-09-03 09:15:22',
    status: 'COMPLETED',
  },
  {
    id: 'TX-89409',
    orderId: 'ORD-7720',
    symbol: 'AAPL',
    type: 'BUY',
    shares: 15,
    price: 218.40,
    total: 3276.00,
    fee: 0.00,
    timestamp: '2026-09-02 14:30:10',
    status: 'COMPLETED',
  },
  {
    id: 'TX-89408',
    orderId: 'ORD-7719',
    symbol: 'TSLA',
    type: 'SELL',
    shares: 10,
    price: 224.50,
    total: 2245.00,
    fee: 0.00,
    timestamp: '2026-09-02 11:20:05',
    status: 'COMPLETED',
  },
  {
    id: 'TX-89407',
    orderId: 'DEP-1001',
    symbol: 'USD',
    type: 'DEPOSIT',
    shares: 0,
    price: 1.00,
    total: 25000.00,
    fee: 0.00,
    timestamp: '2026-09-01 09:00:00',
    status: 'COMPLETED',
  },
];

const INITIAL_ACTIVE_ORDERS = [
  {
    id: 'ORD-9801',
    symbol: 'TSLA',
    type: 'BUY',
    executionType: 'LIMIT',
    price: 210.00,
    quantity: 15,
    filled: 0,
    status: 'OPEN',
    timestamp: '10:15:40',
  },
  {
    id: 'ORD-9802',
    symbol: 'META',
    type: 'BUY',
    executionType: 'LIMIT',
    price: 505.00,
    quantity: 10,
    filled: 0,
    status: 'OPEN',
    timestamp: '09:48:12',
  },
];

const INITIAL_MARKET_TRADES = [
  { id: 'TRD-101', symbol: 'NVDA', price: 124.75, quantity: 50, side: 'BUY', timestamp: '10:43:01' },
  { id: 'TRD-102', symbol: 'AAPL', price: 228.50, quantity: 100, side: 'BUY', timestamp: '10:42:58' },
  { id: 'TRD-103', symbol: 'MSFT', price: 442.15, quantity: 30, side: 'SELL', timestamp: '10:42:45' },
  { id: 'TRD-104', symbol: 'TSLA', price: 215.30, quantity: 80, side: 'SELL', timestamp: '10:42:30' },
  { id: 'TRD-105', symbol: 'AMZN', price: 198.60, quantity: 45, side: 'BUY', timestamp: '10:42:15' },
];

export const TradingContext = createContext(null);

export const TradingProvider = ({ children }) => {
  const [stocks, setStocks] = useState(INITIAL_STOCKS);
  const [orderBooks, setOrderBooks] = useState(INITIAL_BOOKS);
  const [cashBalance, setCashBalance] = useState(38650.00);
  const [holdings, setHoldings] = useState(INITIAL_HOLDINGS);
  const [transactions, setTransactions] = useState(INITIAL_TRANSACTIONS);
  const [activeOrders, setActiveOrders] = useState(INITIAL_ACTIVE_ORDERS);
  const [marketTrades, setMarketTrades] = useState(INITIAL_MARKET_TRADES);
  const [isLive, setIsLive] = useState(true);
  const [notification, setNotification] = useState(null);

  // Quick lookup dictionary for stocks
  const stockMap = useMemo(() => {
    return stocks.reduce((acc, stock) => {
      acc[stock.symbol] = stock;
      return acc;
    }, {});
  }, [stocks]);

  // Toast Notification helper
  const notify = useCallback((message, type = 'info') => {
    setNotification({ id: Date.now(), message, type });
    setTimeout(() => {
      setNotification((curr) => (curr && curr.message === message ? null : curr));
    }, 4000);
  }, []);

  // Live Price Simulation Tick
  const tickSimulation = useCallback(() => {
    setStocks((prevStocks) => {
      return prevStocks.map((stock) => {
        // -0.8% to +0.8% realistic micro fluctuation
        const percentChange = (Math.random() * 1.6 - 0.78) / 100;
        const delta = stock.price * percentChange;
        const newPrice = Number(Math.max(1, stock.price + delta).toFixed(2));
        const newChange = Number((newPrice - stock.previousClose).toFixed(2));
        const newChangePercent = Number(((newChange / stock.previousClose) * 100).toFixed(2));
        const newHigh = Number(Math.max(stock.high, newPrice).toFixed(2));
        const newLow = Number(Math.min(stock.low, newPrice).toFixed(2));
        const addedVol = Math.floor(Math.random() * 4000 + 500);

        // Update sparkline history
        const newHistory = [...stock.history.slice(1), newPrice];

        return {
          ...stock,
          price: newPrice,
          change: newChange,
          changePercent: newChangePercent,
          high: newHigh,
          low: newLow,
          volume: stock.volume + addedVol,
          bid: Number((newPrice - 0.05).toFixed(2)),
          ask: Number((newPrice + 0.05).toFixed(2)),
          history: newHistory,
        };
      });
    });

    // Also occasionally generate an executed market trade
    const randomStock = INITIAL_STOCKS[Math.floor(Math.random() * INITIAL_STOCKS.length)].symbol;
    const side = Math.random() > 0.48 ? 'BUY' : 'SELL';
    const curStock = stockMap[randomStock];
    if (curStock) {
      const tradePrice = curStock.price;
      const tradeQty = Math.floor(Math.random() * 60 + 5);
      const now = new Date();
      const timeStr = now.toTimeString().split(' ')[0];
      setMarketTrades((prev) => [
        {
          id: 'TRD-' + Math.floor(1000 + Math.random() * 9000),
          symbol: randomStock,
          price: tradePrice,
          quantity: tradeQty,
          side,
          timestamp: timeStr,
        },
        ...prev.slice(0, 19),
      ]);
    }
  }, [stockMap]);

  // Interval for live simulation
  useEffect(() => {
    if (!isLive) return;
    const interval = setInterval(tickSimulation, 3000);
    return () => clearInterval(interval);
  }, [isLive, tickSimulation]);

  // Toggle live streaming
  const toggleLiveSimulation = useCallback(() => {
    setIsLive((prev) => {
      const next = !prev;
      notify(next ? 'Live market data feed connected' : 'Market feed paused', next ? 'success' : 'info');
      return next;
    });
  }, [notify]);

  // Manual refresh
  const refreshMarket = useCallback(() => {
    tickSimulation();
    notify('Market data refreshed', 'success');
  }, [tickSimulation, notify]);

  // Comprehensive Portfolio Calculations
  const portfolioMetrics = useMemo(() => {
    const enrichedHoldings = holdings.map((h) => {
      const stock = stockMap[h.symbol];
      const curPrice = stock ? stock.price : h.avgBuyPrice;
      const prevClose = stock ? stock.previousClose : h.avgBuyPrice;
      const totalCost = Number((h.shares * h.avgBuyPrice).toFixed(2));
      const curVal = Number((h.shares * curPrice).toFixed(2));
      const pnl = Number((curVal - totalCost).toFixed(2));
      const pnlPct = totalCost > 0 ? Number(((pnl / totalCost) * 100).toFixed(2)) : 0;
      const dayPnL = Number((h.shares * (curPrice - prevClose)).toFixed(2));

      return {
        ...h,
        currentPrice: curPrice,
        previousClose: prevClose,
        totalCost,
        currentValue: curVal,
        unrealizedPnL: pnl,
        unrealizedPnLPercent: pnlPct,
        todayPnL: dayPnL,
        allocationPercent: 0, // Computed below
      };
    });

    const investedCapital = enrichedHoldings.reduce((sum, h) => sum + h.totalCost, 0);
    const currentHoldingsValue = enrichedHoldings.reduce((sum, h) => sum + h.currentValue, 0);
    const todayHoldingsPnL = enrichedHoldings.reduce((sum, h) => sum + h.todayPnL, 0);

    const totalPortfolioValue = Number((cashBalance + currentHoldingsValue).toFixed(2));
    const totalUnrealizedPnL = Number((currentHoldingsValue - investedCapital).toFixed(2));
    const totalUnrealizedPnLPct = investedCapital > 0 ? Number(((totalUnrealizedPnL / investedCapital) * 100).toFixed(2)) : 0;

    // Calculate allocation percentages
    const finalHoldings = enrichedHoldings.map((h) => ({
      ...h,
      allocationPercent: totalPortfolioValue > 0 ? Number(((h.currentValue / totalPortfolioValue) * 100).toFixed(1)) : 0,
    }));

    return {
      cashBalance,
      investedCapital: Number(investedCapital.toFixed(2)),
      currentHoldingsValue: Number(currentHoldingsValue.toFixed(2)),
      totalPortfolioValue,
      totalUnrealizedPnL,
      totalUnrealizedPnLPct,
      todayPnL: Number(todayHoldingsPnL.toFixed(2)),
      todayPnLPct: totalPortfolioValue > 0 ? Number(((todayHoldingsPnL / totalPortfolioValue) * 100).toFixed(2)) : 0,
      holdings: finalHoldings,
    };
  }, [holdings, stockMap, cashBalance]);

  // Risk & Order Validation Engine
  const validateOrder = useCallback(
    ({ symbol, type, executionType, quantity, price }) => {
      if (!symbol || !stockMap[symbol]) {
        return { valid: false, reason: 'Invalid or unsupported stock symbol.' };
      }
      const qty = parseInt(quantity, 10);
      if (isNaN(qty) || qty <= 0) {
        return { valid: false, reason: 'Quantity must be a positive integer.' };
      }
      if (qty > 10000) {
        return { valid: false, reason: 'Risk Limit Exceeded: Max 10,000 shares per single order.' };
      }

      const stock = stockMap[symbol];
      const effPrice = executionType === 'LIMIT' ? parseFloat(price) : (type === 'BUY' ? stock.ask : stock.bid);

      if (executionType === 'LIMIT' && (isNaN(effPrice) || effPrice <= 0)) {
        return { valid: false, reason: 'Limit price must be greater than $0.00.' };
      }

      const totalValue = qty * effPrice;
      if (totalValue > 250000) {
        return { valid: false, reason: 'Risk Limit Exceeded: Single order value cannot exceed $250,000.' };
      }

      if (type === 'BUY') {
        if (totalValue > cashBalance) {
          return {
            valid: false,
            reason: `Insufficient Funds: Required $${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2 })}, available cash is $${cashBalance.toLocaleString(undefined, { minimumFractionDigits: 2 })}.`,
          };
        }
      } else if (type === 'SELL') {
        const ownedHolding = holdings.find((h) => h.symbol === symbol);
        const ownedShares = ownedHolding ? ownedHolding.shares : 0;
        if (qty > ownedShares) {
          return {
            valid: false,
            reason: `Insufficient Shares: You own ${ownedShares} shares of ${symbol}, requested sell is ${qty} shares.`,
          };
        }
      }

      return { valid: true, effectivePrice: effPrice, totalValue };
    },
    [stockMap, cashBalance, holdings]
  );

  // Execute Order Function
  const executeOrder = useCallback(
    ({ symbol, type, executionType, quantity, price }) => {
      const validation = validateOrder({ symbol, type, executionType, quantity, price });
      const now = new Date();
      const dateStr = now.toISOString().replace('T', ' ').substring(0, 19);
      const timeStr = now.toTimeString().split(' ')[0];
      const orderId = 'ORD-' + Math.floor(10000 + Math.random() * 90000);
      const txId = 'TX-' + Math.floor(100000 + Math.random() * 900000);

      if (!validation.valid) {
        notify(`Order Rejected: ${validation.reason}`, 'error');
        // Record rejected attempt in transaction history for complete audit
        setTransactions((prev) => [
          {
            id: txId,
            orderId,
            symbol,
            type,
            shares: parseInt(quantity, 10) || 0,
            price: parseFloat(price) || stockMap[symbol]?.price || 0,
            total: 0,
            fee: 0,
            timestamp: dateStr,
            status: 'REJECTED',
          },
          ...prev,
        ]);
        return { success: false, reason: validation.reason };
      }

      const qty = parseInt(quantity, 10);
      const stock = stockMap[symbol];

      // If it is a Limit Order that doesn't cross market price, place in Order Book and active orders
      const willMatchImmediately =
        executionType === 'MARKET' ||
        (type === 'BUY' && validation.effectivePrice >= stock.ask) ||
        (type === 'SELL' && validation.effectivePrice <= stock.bid);

      if (!willMatchImmediately) {
        // Place in Order Book & Active Orders
        const newOrder = {
          id: orderId,
          symbol,
          type,
          executionType,
          price: validation.effectivePrice,
          quantity: qty,
          filled: 0,
          status: 'OPEN',
          timestamp: timeStr,
        };

        setActiveOrders((prev) => [newOrder, ...prev]);

        setOrderBooks((prev) => {
          const currentBook = prev[symbol] || { bids: [], asks: [] };
          if (type === 'BUY') {
            const updatedBids = [...currentBook.bids, {
              id: orderId,
              type: 'BUY',
              price: validation.effectivePrice,
              quantity: qty,
              total: qty,
              timestamp: timeStr,
              accountId: 'USER-1',
            }].sort((a, b) => b.price - a.price);
            return { ...prev, [symbol]: { ...currentBook, bids: updatedBids } };
          } else {
            const updatedAsks = [...currentBook.asks, {
              id: orderId,
              type: 'SELL',
              price: validation.effectivePrice,
              quantity: qty,
              total: qty,
              timestamp: timeStr,
              accountId: 'USER-1',
            }].sort((a, b) => a.price - b.price);
            return { ...prev, [symbol]: { ...currentBook, asks: updatedAsks } };
          }
        });

        notify(`Limit ${type} order for ${qty} ${symbol} placed at $${validation.effectivePrice.toFixed(2)} in Order Book`, 'info');
        return { success: true, filled: false, message: 'Order submitted to book' };
      }

      // Immediate Execution (Matching against Market)
      const fillPrice = executionType === 'MARKET' ? (type === 'BUY' ? stock.ask : stock.bid) : validation.effectivePrice;
      const totalAmount = Number((qty * fillPrice).toFixed(2));

      // 1. Update Cash Balance
      if (type === 'BUY') {
        setCashBalance((prev) => Number((prev - totalAmount).toFixed(2)));
      } else {
        setCashBalance((prev) => Number((prev + totalAmount).toFixed(2)));
      }

      // 2. Update Portfolio Holdings
      setHoldings((prevHoldings) => {
        const existing = prevHoldings.find((h) => h.symbol === symbol);
        if (type === 'BUY') {
          if (existing) {
            const totalShares = existing.shares + qty;
            const totalCost = existing.shares * existing.avgBuyPrice + totalAmount;
            const newAvg = Number((totalCost / totalShares).toFixed(2));
            return prevHoldings.map((h) =>
              h.symbol === symbol ? { ...h, shares: totalShares, avgBuyPrice: newAvg } : h
            );
          } else {
            return [
              ...prevHoldings,
              { symbol, name: stock.name, shares: qty, avgBuyPrice: fillPrice },
            ];
          }
        } else {
          // SELL
          if (existing.shares === qty) {
            return prevHoldings.filter((h) => h.symbol !== symbol);
          } else {
            return prevHoldings.map((h) =>
              h.symbol === symbol ? { ...h, shares: h.shares - qty } : h
            );
          }
        }
      });

      // 3. Log to Transactions
      const completedTx = {
        id: txId,
        orderId,
        symbol,
        type,
        shares: qty,
        price: fillPrice,
        total: totalAmount,
        fee: 0.00,
        timestamp: dateStr,
        status: 'COMPLETED',
      };
      setTransactions((prev) => [completedTx, ...prev]);

      // 4. Record to Market Trades Feed
      setMarketTrades((prev) => [
        {
          id: 'TRD-' + Math.floor(1000 + Math.random() * 9000),
          symbol,
          price: fillPrice,
          quantity: qty,
          side: type,
          timestamp: timeStr,
        },
        ...prev.slice(0, 19),
      ]);

      notify(
        `Successfully executed ${type} ${qty} ${symbol} @ $${fillPrice.toFixed(2)} ($${totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })})`,
        'success'
      );

      return { success: true, filled: true, fillPrice, totalAmount };
    },
    [validateOrder, stockMap, notify]
  );

  // Cancel an active order
  const cancelOrder = useCallback(
    (orderId) => {
      const order = activeOrders.find((o) => o.id === orderId);
      if (!order) return;

      setActiveOrders((prev) => prev.filter((o) => o.id !== orderId));

      setOrderBooks((prev) => {
        const book = prev[order.symbol];
        if (!book) return prev;
        return {
          ...prev,
          [order.symbol]: {
            bids: book.bids.filter((b) => b.id !== orderId),
            asks: book.asks.filter((a) => a.id !== orderId),
          },
        };
      });

      const now = new Date();
      setTransactions((prev) => [
        {
          id: 'TX-' + Math.floor(100000 + Math.random() * 900000),
          orderId,
          symbol: order.symbol,
          type: order.type,
          shares: order.quantity,
          price: order.price,
          total: 0,
          fee: 0,
          timestamp: now.toISOString().replace('T', ' ').substring(0, 19),
          status: 'CANCELLED',
        },
        ...prev,
      ]);

      notify(`Order ${orderId} (${order.type} ${order.symbol}) cancelled`, 'info');
    },
    [activeOrders, notify]
  );

  // Deposit funds helper
  const depositFunds = useCallback(
    (amount) => {
      const depositVal = parseFloat(amount);
      if (isNaN(depositVal) || depositVal <= 0) {
        notify('Please enter a valid deposit amount', 'error');
        return false;
      }
      setCashBalance((prev) => Number((prev + depositVal).toFixed(2)));
      const now = new Date();
      setTransactions((prev) => [
        {
          id: 'TX-' + Math.floor(100000 + Math.random() * 900000),
          orderId: 'DEP-' + Math.floor(1000 + Math.random() * 9000),
          symbol: 'USD',
          type: 'DEPOSIT',
          shares: 0,
          price: 1.00,
          total: depositVal,
          fee: 0,
          timestamp: now.toISOString().replace('T', ' ').substring(0, 19),
          status: 'COMPLETED',
        },
        ...prev,
      ]);
      notify(`Successfully deposited $${depositVal.toLocaleString(undefined, { minimumFractionDigits: 2 })}`, 'success');
      return true;
    },
    [notify]
  );

  // Reset to initial demo state
  const resetToDemo = useCallback(() => {
    setStocks(INITIAL_STOCKS);
    setOrderBooks(INITIAL_BOOKS);
    setCashBalance(38650.00);
    setHoldings(INITIAL_HOLDINGS);
    setTransactions(INITIAL_TRANSACTIONS);
    setActiveOrders(INITIAL_ACTIVE_ORDERS);
    setMarketTrades(INITIAL_MARKET_TRADES);
    notify('TradeFlow demo state reset to baseline', 'info');
  }, [notify]);

  return (
    <TradingContext.Provider
      value={{
        stocks,
        stockMap,
        orderBooks,
        portfolioMetrics,
        activeOrders,
        transactions,
        marketTrades,
        isLive,
        notification,
        executeOrder,
        cancelOrder,
        validateOrder,
        refreshMarket,
        toggleLiveSimulation,
        depositFunds,
        resetToDemo,
        notify,
      }}
    >
      {children}
    </TradingContext.Provider>
  );
};

export const useTrading = () => {
  const context = useContext(TradingContext);
  if (!context) {
    throw new Error('useTrading must be used within a TradingProvider');
  }
  return context;
};
