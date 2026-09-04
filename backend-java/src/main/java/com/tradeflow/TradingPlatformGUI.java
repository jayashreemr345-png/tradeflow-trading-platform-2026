package com.tradeflow;

import com.tradeflow.engine.*;
import com.tradeflow.model.*;

import java.util.List;
import java.util.UUID;

public class TradingPlatformGUI {
    private final AccountManager accountManager;
    private final MarketDataFeed marketDataFeed;
    private final MatchingEngine matchingEngine;
    private final TransactionManager transactionManager;
    private final RiskManager riskManager;

    public TradingPlatformGUI(AccountManager accountManager, MarketDataFeed marketDataFeed,
                              MatchingEngine matchingEngine, TransactionManager transactionManager,
                              RiskManager riskManager) {
        this.accountManager = accountManager;
        this.marketDataFeed = marketDataFeed;
        this.matchingEngine = matchingEngine;
        this.transactionManager = transactionManager;
        this.riskManager = riskManager;
    }

    public boolean submitOrder(String accountId, String symbol, OrderType type, String executionType, double price, int quantity) {
        TraderAccount account = accountManager.getAccount(accountId);
        if (account == null) return false;

        MarketData md = marketDataFeed.getMarketData(symbol);
        double curPrice = (md != null) ? md.getCurrentPrice() : price;

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, accountId, symbol, type, executionType, price, quantity);

        StringBuilder reason = new StringBuilder();
        if (!riskManager.checkRisk(account, order, curPrice, reason)) {
            System.out.println("[RISK REJECTION] " + reason);
            transactionManager.recordTransaction(accountId, orderId, symbol,
                    (type == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL),
                    quantity, price, price * quantity, 0.0, "REJECTED");
            return false;
        }

        List<Trade> executedTrades = matchingEngine.match(order);
        PortfolioManager pm = new PortfolioManager();

        for (Trade trade : executedTrades) {
            pm.processTradeExecution(account, trade, type);
            transactionManager.recordTransaction(accountId, orderId, symbol,
                    (type == OrderType.BUY ? TransactionType.BUY : TransactionType.SELL),
                    trade.getQuantity(), trade.getPrice(), trade.getTotalAmount(), 0.0, "COMPLETED");
        }

        return true;
    }
}
