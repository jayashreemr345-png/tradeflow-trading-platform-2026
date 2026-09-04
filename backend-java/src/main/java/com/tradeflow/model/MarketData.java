package com.tradeflow.model;

import java.time.LocalDateTime;

public class MarketData {
    private final String symbol;
    private final String name;
    private double currentPrice;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double previousClose;
    private long volume;
    private double change;
    private double changePercent;
    private double bidPrice;
    private double askPrice;
    private LocalDateTime lastUpdated;

    public MarketData(String symbol, String name, double initialPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = initialPrice;
        this.openPrice = initialPrice;
        this.highPrice = initialPrice;
        this.lowPrice = initialPrice;
        this.previousClose = initialPrice * 0.99;
        this.volume = 1_000_000L;
        this.updateDerivedValues();
    }

    public void updatePrice(double newPrice) {
        this.currentPrice = Math.round(newPrice * 100.0) / 100.0;
        if (this.currentPrice > this.highPrice) this.highPrice = this.currentPrice;
        if (this.currentPrice < this.lowPrice) this.lowPrice = this.currentPrice;
        this.volume += (long)(Math.random() * 5000 + 500);
        updateDerivedValues();
    }

    private void updateDerivedValues() {
        this.change = Math.round((currentPrice - previousClose) * 100.0) / 100.0;
        this.changePercent = Math.round((change / previousClose) * 10000.0) / 100.0;
        this.bidPrice = Math.round((currentPrice - 0.05) * 100.0) / 100.0;
        this.askPrice = Math.round((currentPrice + 0.05) * 100.0) / 100.0;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public double getOpenPrice() { return openPrice; }
    public double getHighPrice() { return highPrice; }
    public double getLowPrice() { return lowPrice; }
    public double getPreviousClose() { return previousClose; }
    public long getVolume() { return volume; }
    public double getChange() { return change; }
    public double getChangePercent() { return changePercent; }
    public double getBidPrice() { return bidPrice; }
    public double getAskPrice() { return askPrice; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
