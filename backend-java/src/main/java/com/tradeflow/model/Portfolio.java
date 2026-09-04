package com.tradeflow.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Portfolio {
    private final String accountId;
    // Symbol -> Quantity held
    private final Map<String, Integer> holdings = new ConcurrentHashMap<>();
    // Symbol -> Average Purchase Price
    private final Map<String, Double> avgBuyPrices = new ConcurrentHashMap<>();

    public Portfolio(String accountId) {
        this.accountId = accountId;
    }

    public synchronized void addPosition(String symbol, int quantity, double price) {
        int currentQty = holdings.getOrDefault(symbol, 0);
        double currentAvg = avgBuyPrices.getOrDefault(symbol, 0.0);

        int newQty = currentQty + quantity;
        if (newQty > 0) {
            double totalCost = (currentQty * currentAvg) + (quantity * price);
            double newAvg = totalCost / newQty;
            holdings.put(symbol, newQty);
            avgBuyPrices.put(symbol, Math.round(newAvg * 100.0) / 100.0);
        }
    }

    public synchronized boolean removePosition(String symbol, int quantity) {
        int currentQty = holdings.getOrDefault(symbol, 0);
        if (currentQty < quantity) {
            return false;
        }
        int remaining = currentQty - quantity;
        if (remaining == 0) {
            holdings.remove(symbol);
            avgBuyPrices.remove(symbol);
        } else {
            holdings.put(symbol, remaining);
        }
        return true;
    }

    public int getQuantity(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    public double getAvgBuyPrice(String symbol) {
        return avgBuyPrices.getOrDefault(symbol, 0.0);
    }

    public Map<String, Integer> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public String getAccountId() {
        return accountId;
    }
}
