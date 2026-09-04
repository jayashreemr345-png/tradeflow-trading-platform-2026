package com.tradeflow;

import com.tradeflow.engine.*;
import com.tradeflow.model.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing TradeFlow Core Trading Engine...");

        AccountManager accountManager = new AccountManager();
        MarketDataFeed marketDataFeed = new MarketDataFeed();
        MatchingEngine matchingEngine = new MatchingEngine();
        TransactionManager transactionManager = new TransactionManager();
        AnalyticsEngine analyticsEngine = new AnalyticsEngine();
        RiskManager riskManager = new RiskManager();

        // 1. Seed Instruments
        marketDataFeed.registerInstrument("AAPL", "Apple Inc.", 228.50);
        marketDataFeed.registerInstrument("MSFT", "Microsoft Corp.", 442.20);
        marketDataFeed.registerInstrument("NVDA", "NVIDIA Corp.", 124.75);
        marketDataFeed.registerInstrument("GOOGL", "Alphabet Inc.", 182.40);
        marketDataFeed.registerInstrument("AMZN", "Amazon.com Inc.", 198.60);
        marketDataFeed.registerInstrument("TSLA", "Tesla Inc.", 215.30);
        marketDataFeed.registerInstrument("META", "Meta Platforms", 512.90);
        marketDataFeed.registerInstrument("JPM", "JPMorgan Chase", 218.10);

        // 2. Setup Trader Account
        TraderAccount trader = accountManager.createAccount("ACC-1001", "Alex Vance", 50000.0);
        trader.getPortfolio().addPosition("AAPL", 30, 215.00);
        trader.getPortfolio().addPosition("NVDA", 40, 110.00);
        trader.getPortfolio().addPosition("MSFT", 15, 420.00);

        // 3. Seed Order Book with Market Liquidity
        OrderBook aaplBook = matchingEngine.getOrCreateOrderBook("AAPL");
        aaplBook.addOrder(new Order("ORD-SEED-1", "MARKET-MAKER-1", "AAPL", OrderType.SELL, "LIMIT", 228.60, 50));
        aaplBook.addOrder(new Order("ORD-SEED-2", "MARKET-MAKER-1", "AAPL", OrderType.SELL, "LIMIT", 228.80, 100));
        aaplBook.addOrder(new Order("ORD-SEED-3", "MARKET-MAKER-2", "AAPL", OrderType.BUY, "LIMIT", 228.40, 60));
        aaplBook.addOrder(new Order("ORD-SEED-4", "MARKET-MAKER-2", "AAPL", OrderType.BUY, "LIMIT", 228.20, 80));

        // 4. Print Dashboard
        TradingDashboard dashboard = new TradingDashboard(
                accountManager, marketDataFeed, matchingEngine, transactionManager, analyticsEngine, riskManager
        );
        dashboard.printConsoleOverview("ACC-1001");

        // 5. Test Market Order Execution
        TradingPlatformGUI gui = new TradingPlatformGUI(
                accountManager, marketDataFeed, matchingEngine, transactionManager, riskManager
        );

        System.out.println("\nExecuting sample BUY market order for 10 AAPL shares...");
        boolean success = gui.submitOrder("ACC-1001", "AAPL", OrderType.BUY, "MARKET", 0.0, 10);
        System.out.println("Execution success: " + success);

        System.out.println("\nUpdated Dashboard overview:");
        dashboard.printConsoleOverview("ACC-1001");
    }
}
