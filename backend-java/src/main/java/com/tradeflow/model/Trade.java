package com.tradeflow.model;

import java.time.LocalDateTime;

public class Trade {
    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String symbol;
    private final double price;
    private final int quantity;
    private final LocalDateTime timestamp;

    public Trade(String tradeId, String buyOrderId, String sellOrderId, String symbol, double price, int quantity) {
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }

    public String getTradeId() { return tradeId; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return price * quantity; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Trade[%s: %d %s @ $%.2f, Total: $%.2f]", tradeId, quantity, symbol, price, getTotalAmount());
    }
}
