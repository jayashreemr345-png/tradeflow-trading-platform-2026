package com.tradeflow;

import com.tradeflow.engine.*;
import com.tradeflow.model.*;

import java.util.List;

public class TradingDashboard {
    private final AccountManager accountManager;
    private final MarketDataFeed marketDataFeed;
    private final MatchingEngine matchingEngine;
    private final TransactionManager transactionManager;
    private final AnalyticsEngine analyticsEngine;
    private final RiskManager riskManager;

    public TradingDashboard(AccountManager accountManager, MarketDataFeed marketDataFeed,
                            MatchingEngine matchingEngine, TransactionManager transactionManager,
                            AnalyticsEngine analyticsEngine, RiskManager riskManager) {
        this.accountManager = accountManager;
        this.marketDataFeed = marketDataFeed;
        this.matchingEngine = matchingEngine;
        this.transactionManager = transactionManager;
        this.analyticsEngine = analyticsEngine;
        this.riskManager = riskManager;
    }

    public void printConsoleOverview(String accountId) {
        TraderAccount account = accountManager.getAccount(accountId);
        if (account == null) {
            System.out.println("Account not found: " + accountId);
            return;
        }

        AnalyticsEngine.PerformanceMetrics metrics = analyticsEngine.calculateMetrics(
                account, matchingEngine.getTradeHistory(), marketDataFeed.getAllMarketData(), 50000.0);

        System.out.println("==================================================");
        System.out.println("          TRADEFLOW TRADING PLATFORM");
        System.out.println("==================================================");
        System.out.printf("Trader: %s (%s)\n", account.getTraderName(), account.getAccountId());
        System.out.printf("Cash Balance:        $%,.2f\n", account.getCashBalance());
        System.out.printf("Portfolio Value:     $%,.2f\n", metrics.totalPortfolioValue);
        System.out.printf("Unrealized P&L:      $%,.2f\n", metrics.unrealizedPnL);
        System.out.printf("Total Return:        %.2f%%\n", metrics.totalReturnPercent);
        System.out.printf("Total Trades:        %d\n", metrics.totalTrades);
        System.out.printf("Total Volume:        $%,.2f\n", metrics.totalVolume);
        System.out.println("--------------------------------------------------");
        System.out.println("Active Holdings:");
        account.getPortfolio().getHoldings().forEach((sym, qty) -> {
            MarketData md = marketDataFeed.getMarketData(sym);
            double curPrice = (md != null) ? md.getCurrentPrice() : 0.0;
            System.out.printf("  %-6s: %4d shares @ Avg $%.2f (Current $%.2f)\n",
                    sym, qty, account.getPortfolio().getAvgBuyPrice(sym), curPrice);
        });
        System.out.println("==================================================");
    }
}
