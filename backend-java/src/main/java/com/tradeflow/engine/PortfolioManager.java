package com.tradeflow.engine;

import com.tradeflow.model.*;

import java.util.Map;

public class PortfolioManager {

    public double calculateTotalPortfolioValue(TraderAccount account, Map<String, MarketData> marketDataFeed) {
        double total = account.getCashBalance();
        for (Map.Entry<String, Integer> entry : account.getPortfolio().getHoldings().entrySet()) {
            String symbol = entry.getKey();
            int qty = entry.getValue();
            MarketData md = marketDataFeed.get(symbol);
            double currentPrice = (md != null) ? md.getCurrentPrice() : account.getPortfolio().getAvgBuyPrice(symbol);
            total += qty * currentPrice;
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public double calculateUnrealizedPnL(TraderAccount account, Map<String, MarketData> marketDataFeed) {
        double pnl = 0.0;
        for (Map.Entry<String, Integer> entry : account.getPortfolio().getHoldings().entrySet()) {
            String symbol = entry.getKey();
            int qty = entry.getValue();
            double avgCost = account.getPortfolio().getAvgBuyPrice(symbol);
            MarketData md = marketDataFeed.get(symbol);
            double currentPrice = (md != null) ? md.getCurrentPrice() : avgCost;
            pnl += qty * (currentPrice - avgCost);
        }
        return Math.round(pnl * 100.0) / 100.0;
    }

    public void processTradeExecution(TraderAccount account, Trade trade, OrderType traderSide) {
        if (traderSide == OrderType.BUY) {
            double totalCost = trade.getTotalAmount();
            account.debitCash(totalCost);
            account.getPortfolio().addPosition(trade.getSymbol(), trade.getQuantity(), trade.getPrice());
        } else {
            double totalProceeds = trade.getTotalAmount();
            account.creditCash(totalProceeds);
            account.getPortfolio().removePosition(trade.getSymbol(), trade.getQuantity());
        }
    }
}
