package com.tradeflow.model;

import java.time.LocalDateTime;

public class Order {
    private final String orderId;
    private final String accountId;
    private final String symbol;
    private final OrderType type;       // BUY or SELL
    private final String executionType; // LIMIT or MARKET
    private double price;
    private int quantity;
    private int filledQuantity;
    private String status;              // PENDING, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED
    private final LocalDateTime timestamp;

    public Order(String orderId, String accountId, String symbol, OrderType type, String executionType, double price, int quantity) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.symbol = symbol;
        this.type = type;
        this.executionType = executionType;
        this.price = price;
        this.quantity = quantity;
        this.filledQuantity = 0;
        this.status = "PENDING";
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public String getAccountId() { return accountId; }
    public String getSymbol() { return symbol; }
    public OrderType getType() { return type; }
    public String getExecutionType() { return executionType; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public int getFilledQuantity() { return filledQuantity; }
    public int getRemainingQuantity() { return quantity - filledQuantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void addFill(int fillQty) {
        this.filledQuantity += fillQty;
        if (this.filledQuantity >= this.quantity) {
            this.status = "FILLED";
        } else {
            this.status = "PARTIALLY_FILLED";
        }
    }

    public boolean isFilled() {
        return filledQuantity >= quantity;
    }

    @Override
    public String toString() {
        return String.format("Order[%s %s %d %s @ $%.2f, Status: %s]", orderId, type, quantity, symbol, price, status);
    }
}
