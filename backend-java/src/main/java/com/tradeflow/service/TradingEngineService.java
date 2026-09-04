package com.tradeflow.service;

import com.tradeflow.engine.*;
import com.tradeflow.model.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Centralized service managing the lifecycle and single source of truth
 * for the TradeFlow core trading engine, market feeds, and trader accounts.
 */
public class TradingEngineService {

    private static final TradingEngineService INSTANCE = new TradingEngineService();

    private final AccountManager accountManager;
    private final MarketDataFeed marketDataFeed;
    private final MatchingEngine matchingEngine;
    private final TransactionManager transactionManager;
    private final AnalyticsEngine analyticsEngine;
    private final RiskManager riskManager;
    private final PortfolioManager portfolioManager;
    private final MarketDataSimulator simulator;

    // Track active client orders (PENDING / PARTIALLY_FILLED)
    private final Map<String, Order> activeOrders = new ConcurrentHashMap<>();
    private final ScheduledExecutorService limitOrderMonitor = Executors.newSingleThreadScheduledExecutor();

    public static final String DEFAULT_ACCOUNT_ID = "ACC-1001";
    public static final String DEFAULT_TRADER_NAME = "Alex Vance";
    public static final double DEFAULT_INITIAL_DEPOSIT = 50000.0;

    private TradingEngineService() {
        this.accountManager = new AccountManager();
        this.marketDataFeed = new MarketDataFeed();
        this.matchingEngine = new MatchingEngine();
        this.transactionManager = new TransactionManager();
        this.analyticsEngine = new AnalyticsEngine();
        this.riskManager = new RiskManager();
        this.portfolioManager = new PortfolioManager();
        this.simulator = new MarketDataSimulator(this.marketDataFeed);

        seedInitialData();
        startMarketSimulation();
        startLimitOrderMonitor();
    }

    public static TradingEngineService getInstance() {
        return INSTANCE;
    }

    private void seedInitialData() {
        // 1. Seed Instruments
        // Tech & Growth
        marketDataFeed.registerInstrument("AAPL", "Apple Inc.", 228.50);
        marketDataFeed.registerInstrument("MSFT", "Microsoft Corp.", 442.20);
        marketDataFeed.registerInstrument("NVDA", "NVIDIA Corporation", 124.75);
        marketDataFeed.registerInstrument("GOOGL", "Alphabet Inc.", 182.40);
        marketDataFeed.registerInstrument("META", "Meta Platforms Inc.", 512.90);
        marketDataFeed.registerInstrument("AMZN", "Amazon.com Inc.", 198.60);
        marketDataFeed.registerInstrument("TSLA", "Tesla Inc.", 215.30);
        marketDataFeed.registerInstrument("AMD", "Advanced Micro Devices Inc.", 148.50);
        marketDataFeed.registerInstrument("INTC", "Intel Corporation", 21.80);
        marketDataFeed.registerInstrument("NFLX", "Netflix Inc.", 685.20);
        marketDataFeed.registerInstrument("CRM", "Salesforce Inc.", 254.90);
        marketDataFeed.registerInstrument("ORCL", "Oracle Corporation", 139.50);
        marketDataFeed.registerInstrument("ADBE", "Adobe Inc.", 565.10);

        // Financials
        marketDataFeed.registerInstrument("JPM", "JPMorgan Chase & Co.", 218.10);
        marketDataFeed.registerInstrument("BAC", "Bank of America Corp.", 39.40);
        marketDataFeed.registerInstrument("GS", "The Goldman Sachs Group Inc.", 482.10);
        marketDataFeed.registerInstrument("V", "Visa Inc.", 278.60);
        marketDataFeed.registerInstrument("MA", "Mastercard Incorporated", 475.30);

        // Healthcare
        marketDataFeed.registerInstrument("JNJ", "Johnson & Johnson", 164.20);
        marketDataFeed.registerInstrument("UNH", "UnitedHealth Group Inc.", 585.40);
        marketDataFeed.registerInstrument("PFE", "Pfizer Inc.", 29.10);

        // Consumer & Retail
        marketDataFeed.registerInstrument("WMT", "Walmart Inc.", 76.80);
        marketDataFeed.registerInstrument("KO", "The Coca-Cola Company", 69.50);
        marketDataFeed.registerInstrument("NKE", "NIKE Inc.", 82.40);
        marketDataFeed.registerInstrument("MCD", "McDonald's Corp.", 288.70);

        // Industrial, Media & Energy
        marketDataFeed.registerInstrument("DIS", "The Walt Disney Company", 94.60);
        marketDataFeed.registerInstrument("BA", "The Boeing Company", 162.30);
        marketDataFeed.registerInstrument("XOM", "Exxon Mobil Corporation", 116.80);

        // Index ETFs
        marketDataFeed.registerInstrument("SPY", "SPDR S&P 500 ETF Trust", 564.30);

        // 2. Setup Default Trader Account
        TraderAccount trader = accountManager.createAccount(DEFAULT_ACCOUNT_ID, DEFAULT_TRADER_NAME, DEFAULT_INITIAL_DEPOSIT);
        trader.getPortfolio().addPosition("AAPL", 30, 215.00);
        trader.getPortfolio().addPosition("NVDA", 40, 110.00);
        trader.getPortfolio().addPosition("MSFT", 15, 420.00);

        // 3. Seed Order Book with Market Liquidity for AAPL
        OrderBook aaplBook = matchingEngine.getOrCreateOrderBook("AAPL");
        aaplBook.addOrder(new Order("ORD-SEED-1", "MARKET-MAKER-1", "AAPL", OrderType.SELL, "LIMIT", 228.60, 50));
        aaplBook.addOrder(new Order("ORD-SEED-2", "MARKET-MAKER-1", "AAPL", OrderType.SELL, "LIMIT", 228.80, 100));
        aaplBook.addOrder(new Order("ORD-SEED-3", "MARKET-MAKER-2", "AAPL", OrderType.BUY, "LIMIT", 228.40, 60));
        aaplBook.addOrder(new Order("ORD-SEED-4", "MARKET-MAKER-2", "AAPL", OrderType.BUY, "LIMIT", 228.20, 80));
    }

    public synchronized void startMarketSimulation() {
        if (!simulator.isRunning()) {
            simulator.start(2000);
            System.out.println("MarketDataSimulator background engine started (tick rate: 2000ms).");
        }
    }

    public synchronized void stopMarketSimulation() {
        if (simulator.isRunning()) {
            simulator.stop();
            System.out.println("MarketDataSimulator stopped.");
        }
        limitOrderMonitor.shutdownNow();
    }

    private void startLimitOrderMonitor() {
        // Run every 1000ms to monitor whether market price movements triggered resting limit orders
        limitOrderMonitor.scheduleWithFixedDelay(this::checkAndExecuteRestingLimitOrders, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Executes order submission pipeline through existing engine components:
     * OrderValidator -> RiskManager -> MatchingEngine -> PortfolioManager -> TransactionManager
     */
    public synchronized OrderExecutionResult submitOrder(String accountId, String rawSymbol, String rawSide,
                                                         String rawExecutionType, double price, int quantity) {
        // 1. Account verification
        String targetAccountId = (accountId != null && !accountId.isBlank()) ? accountId.trim() : DEFAULT_ACCOUNT_ID;
        TraderAccount account = accountManager.getAccount(targetAccountId);
        if (account == null) {
            return OrderExecutionResult.error(404, "Trader account not found: " + targetAccountId, rawSymbol, rawSide, rawExecutionType, quantity, price);
        }

        // 2. Validate symbol
        if (rawSymbol == null || rawSymbol.trim().isEmpty()) {
            return OrderExecutionResult.error(400, "Stock symbol is required.", rawSymbol, rawSide, rawExecutionType, quantity, price);
        }
        String symbol = rawSymbol.trim().toUpperCase();
        MarketData marketData = marketDataFeed.getMarketData(symbol);
        if (marketData == null) {
            return OrderExecutionResult.error(404, "Invalid stock symbol: '" + symbol + "' is not supported by the trading engine.", symbol, rawSide, rawExecutionType, quantity, price);
        }

        // 3. Validate Side
        if (rawSide == null || rawSide.trim().isEmpty()) {
            return OrderExecutionResult.error(400, "Order side is required (BUY or SELL).", symbol, rawSide, rawExecutionType, quantity, price);
        }
        OrderType orderSide;
        try {
            orderSide = OrderType.valueOf(rawSide.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OrderExecutionResult.error(400, "Invalid order side '" + rawSide + "': must be BUY or SELL.", symbol, rawSide, rawExecutionType, quantity, price);
        }

        // 4. Validate Execution Type
        String executionType = (rawExecutionType != null && !rawExecutionType.isBlank()) ? rawExecutionType.trim().toUpperCase() : "MARKET";
        if (!"MARKET".equals(executionType) && !"LIMIT".equals(executionType)) {
            return OrderExecutionResult.error(400, "Invalid order type '" + rawExecutionType + "': must be MARKET or LIMIT.", symbol, rawSide, rawExecutionType, quantity, price);
        }

        // 5. Validate Quantity
        if (quantity <= 0) {
            return OrderExecutionResult.error(400, "Quantity must be greater than zero.", symbol, rawSide, executionType, quantity, price);
        }

        // 6. Validate Price
        double curMarketPrice = marketData.getCurrentPrice();
        double effectivePrice;
        if ("LIMIT".equals(executionType)) {
            if (price <= 0) {
                return OrderExecutionResult.error(400, "Limit price must be greater than zero.", symbol, rawSide, executionType, quantity, price);
            }
            effectivePrice = price;
        } else {
            effectivePrice = curMarketPrice;
        }

        // 7. Instantiate Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, targetAccountId, symbol, orderSide, executionType, effectivePrice, quantity);

        // 8. OrderValidator
        StringBuilder valReason = new StringBuilder();
        if (!OrderValidator.validate(order, valReason)) {
            return OrderExecutionResult.error(400, valReason.toString(), symbol, rawSide, executionType, quantity, effectivePrice);
        }

        // 9. RiskManager check
        StringBuilder riskReason = new StringBuilder();
        if (!riskManager.checkRisk(account, order, curMarketPrice, riskReason)) {
            String rejectionMsg = riskReason.toString();
            // Record REJECTED transaction
            TransactionType txType = (orderSide == OrderType.BUY) ? TransactionType.BUY : TransactionType.SELL;
            Transaction tx = transactionManager.recordTransaction(
                    targetAccountId, orderId, symbol, txType, quantity, effectivePrice,
                    effectivePrice * quantity, 0.0, "REJECTED"
            );
            return OrderExecutionResult.rejected(orderId, rejectionMsg, symbol, orderSide.name(), executionType, quantity, effectivePrice, tx.getTransactionId());
        }

        // 10. Execution through MatchingEngine & OrderBook
        OrderBook book = matchingEngine.getOrCreateOrderBook(symbol);

        if ("MARKET".equals(executionType)) {
            // Market orders execute immediately at current market price.
            // Clean up any stale market-maker orders so trade executes at exact current market price
            for (Order ask : book.getActiveAsks()) {
                if (ask.getAccountId().startsWith("MARKET-MAKER")) {
                    book.cancelOrder(ask.getOrderId());
                }
            }
            for (Order bid : book.getActiveBids()) {
                if (bid.getAccountId().startsWith("MARKET-MAKER")) {
                    book.cancelOrder(bid.getOrderId());
                }
            }

            // Provide fresh liquidity order to matching engine at exact current market price
            OrderType counterSide = (orderSide == OrderType.BUY) ? OrderType.SELL : OrderType.BUY;
            String mmOrderId = "MM-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            Order liquidityOrder = new Order(mmOrderId, "MARKET-MAKER", symbol, counterSide, "LIMIT", curMarketPrice, quantity);
            book.addOrder(liquidityOrder);

            List<Trade> executedTrades = matchingEngine.match(order);

            double totalSpentOrReceived = 0.0;
            String lastTxId = null;
            for (Trade trade : executedTrades) {
                portfolioManager.processTradeExecution(account, trade, orderSide);
                Transaction tx = transactionManager.recordTransaction(
                        targetAccountId, orderId, symbol,
                        (orderSide == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL),
                        trade.getQuantity(), trade.getPrice(), trade.getTotalAmount(), 0.0, "COMPLETED"
                );
                lastTxId = tx.getTransactionId();
                totalSpentOrReceived += trade.getTotalAmount();
            }

            double finalExecPrice = !executedTrades.isEmpty() ? (totalSpentOrReceived / quantity) : curMarketPrice;

            return OrderExecutionResult.filled(
                    orderId, symbol, orderSide.name(), executionType, quantity,
                    effectivePrice, finalExecPrice, totalSpentOrReceived,
                    String.format("Market %s order executed for %d %s @ $%.2f", orderSide, quantity, symbol, finalExecPrice),
                    lastTxId
            );
        } else {
            // LIMIT Order: Match against order book
            List<Trade> executedTrades = matchingEngine.match(order);

            if (!executedTrades.isEmpty()) {
                double totalSpentOrReceived = 0.0;
                String lastTxId = null;
                for (Trade trade : executedTrades) {
                    portfolioManager.processTradeExecution(account, trade, orderSide);
                    Transaction tx = transactionManager.recordTransaction(
                            targetAccountId, orderId, symbol,
                            (orderSide == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL),
                            trade.getQuantity(), trade.getPrice(), trade.getTotalAmount(), 0.0, "COMPLETED"
                    );
                    lastTxId = tx.getTransactionId();
                    totalSpentOrReceived += trade.getTotalAmount();
                }

                if (order.isFilled()) {
                    return OrderExecutionResult.filled(
                            orderId, symbol, orderSide.name(), executionType, quantity,
                            effectivePrice, executedTrades.get(0).getPrice(), totalSpentOrReceived,
                            String.format("Limit %s order executed for %d %s @ $%.2f", orderSide, quantity, symbol, executedTrades.get(0).getPrice()),
                            lastTxId
                    );
                }
            }

            // Order remains open in OrderBook
            activeOrders.put(orderId, order);
            return OrderExecutionResult.pending(
                    orderId, symbol, orderSide.name(), executionType, order.getRemainingQuantity(),
                    effectivePrice, "Limit order placed successfully and is OPEN in the order book."
            );
        }
    }

    /**
     * Cancel an active/pending limit order.
     */
    public synchronized boolean cancelOrder(String orderId, StringBuilder reason) {
        if (orderId == null || orderId.isBlank()) {
            reason.append("Order ID is required.");
            return false;
        }

        Order order = activeOrders.get(orderId);
        if (order == null) {
            // Search all order books
            for (String symbol : marketDataFeed.getAllMarketData().keySet()) {
                OrderBook book = matchingEngine.getOrCreateOrderBook(symbol);
                for (Order b : book.getActiveBids()) {
                    if (orderId.equals(b.getOrderId())) { order = b; break; }
                }
                if (order == null) {
                    for (Order a : book.getActiveAsks()) {
                        if (orderId.equals(a.getOrderId())) { order = a; break; }
                    }
                }
                if (order != null) break;
            }
        }

        if (order == null) {
            reason.append("Order '").append(orderId).append("' not found.");
            return false;
        }

        if ("FILLED".equals(order.getStatus())) {
            reason.append("Order '").append(orderId).append("' is already filled and cannot be cancelled.");
            return false;
        }

        if ("CANCELLED".equals(order.getStatus())) {
            reason.append("Order '").append(orderId).append("' is already cancelled.");
            return false;
        }

        OrderBook book = matchingEngine.getOrCreateOrderBook(order.getSymbol());
        boolean cancelled = book.cancelOrder(orderId);
        order.setStatus("CANCELLED");
        activeOrders.remove(orderId);

        // Record cancelled transaction
        transactionManager.recordTransaction(
                order.getAccountId(), orderId, order.getSymbol(),
                (order.getType() == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL),
                order.getRemainingQuantity(), order.getPrice(),
                order.getPrice() * order.getRemainingQuantity(), 0.0, "CANCELLED"
        );

        return true;
    }

    /**
     * Returns all open/pending orders for a given account.
     */
    public List<Order> getActiveOrders(String accountId) {
        String targetAccountId = (accountId != null && !accountId.isBlank()) ? accountId : DEFAULT_ACCOUNT_ID;
        List<Order> result = new ArrayList<>();

        for (Order order : activeOrders.values()) {
            if (targetAccountId.equals(order.getAccountId()) &&
                    ("PENDING".equals(order.getStatus()) || "PARTIALLY_FILLED".equals(order.getStatus()))) {
                result.add(order);
            }
        }

        // Also check OrderBooks directly to guarantee synchronization
        for (String symbol : marketDataFeed.getAllMarketData().keySet()) {
            OrderBook book = matchingEngine.getOrCreateOrderBook(symbol);
            for (Order b : book.getActiveBids()) {
                if (targetAccountId.equals(b.getAccountId()) &&
                        ("PENDING".equals(b.getStatus()) || "PARTIALLY_FILLED".equals(b.getStatus()))) {
                    if (!result.contains(b)) result.add(b);
                }
            }
            for (Order a : book.getActiveAsks()) {
                if (targetAccountId.equals(a.getAccountId()) &&
                        ("PENDING".equals(a.getStatus()) || "PARTIALLY_FILLED".equals(a.getStatus()))) {
                    if (!result.contains(a)) result.add(a);
                }
            }
        }

        return result;
    }

    /**
     * Monitored background execution: checks whether market price movements
     * satisfy limit conditions of resting open limit orders.
     */
    public synchronized void checkAndExecuteRestingLimitOrders() {
        TraderAccount account = getDefaultAccount();
        if (account == null) return;

        Iterator<Map.Entry<String, Order>> iterator = activeOrders.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Order> entry = iterator.next();
            Order order = entry.getValue();

            if (!"PENDING".equals(order.getStatus()) && !"PARTIALLY_FILLED".equals(order.getStatus())) {
                iterator.remove();
                continue;
            }

            MarketData md = marketDataFeed.getMarketData(order.getSymbol());
            if (md == null) continue;

            double curPrice = md.getCurrentPrice();
            boolean shouldExecute = false;

            if (order.getType() == OrderType.BUY && curPrice <= order.getPrice()) {
                shouldExecute = true;
            } else if (order.getType() == OrderType.SELL && curPrice >= order.getPrice()) {
                shouldExecute = true;
            }

            if (shouldExecute) {
                OrderBook book = matchingEngine.getOrCreateOrderBook(order.getSymbol());
                int fillQty = order.getRemainingQuantity();
                double execPrice = order.getPrice();

                // Provide counter-party match
                OrderType counterType = (order.getType() == OrderType.BUY) ? OrderType.SELL : OrderType.BUY;
                Order counterOrder = new Order("MM-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "MARKET-MAKER", order.getSymbol(), counterType, "LIMIT", execPrice, fillQty);
                book.addOrder(counterOrder);

                List<Trade> trades = matchingEngine.match(order);
                for (Trade trade : trades) {
                    portfolioManager.processTradeExecution(account, trade, order.getType());
                    transactionManager.recordTransaction(
                            account.getAccountId(), order.getOrderId(), order.getSymbol(),
                            (order.getType() == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL),
                            trade.getQuantity(), trade.getPrice(), trade.getTotalAmount(), 0.0, "COMPLETED"
                    );
                }

                if (order.isFilled()) {
                    iterator.remove();
                    System.out.println("[LIMIT EXECUTED] " + order.getOrderId() + " " + order.getType() + " " + fillQty + " " + order.getSymbol() + " @ $" + execPrice);
                }
            }
        }
    }

    public AccountManager getAccountManager() { return accountManager; }
    public MarketDataFeed getMarketDataFeed() { return marketDataFeed; }
    public MatchingEngine getMatchingEngine() { return matchingEngine; }
    public TransactionManager getTransactionManager() { return transactionManager; }
    public AnalyticsEngine getAnalyticsEngine() { return analyticsEngine; }
    public RiskManager getRiskManager() { return riskManager; }
    public PortfolioManager getPortfolioManager() { return portfolioManager; }
    public MarketDataSimulator getSimulator() { return simulator; }
    public TraderAccount getDefaultAccount() { return accountManager.getAccount(DEFAULT_ACCOUNT_ID); }
}
