package com.tradeflow.model;

import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final String accountId;
    private final String orderId;
    private final String symbol;
    private final TransactionType type;
    private final int quantity;
    private final double price;
    private final double totalAmount;
    private final double fee;
    private final String status;
    private final LocalDateTime timestamp;

    public Transaction(String transactionId, String accountId, String orderId, String symbol,
                       TransactionType type, int quantity, double price, double totalAmount, double fee, String status) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.fee = fee;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public String getOrderId() { return orderId; }
    public String getSymbol() { return symbol; }
    public TransactionType getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotalAmount() { return totalAmount; }
    public double getFee() { return fee; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Tx[%s %s %d %s @ $%.2f Total: $%.2f (%s)]",
                transactionId, type, quantity, symbol, price, totalAmount, status);
    }
}
