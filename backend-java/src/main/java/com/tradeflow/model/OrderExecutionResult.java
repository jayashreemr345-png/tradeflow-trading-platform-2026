package com.tradeflow.model;

import com.tradeflow.util.JsonParser;

/**
 * Result object returned by the order execution pipeline.
 */
public class OrderExecutionResult {
    private final boolean success;
    private final int statusCode;
    private final String orderId;
    private final String status; // FILLED, PENDING, REJECTED, CANCELLED
    private final String symbol;
    private final String side;
    private final String orderType;
    private final int quantity;
    private final double requestedPrice;
    private final double executionPrice;
    private final double totalAmount;
    private final String message;
    private final String error;
    private final String transactionId;

    public OrderExecutionResult(boolean success, int statusCode, String orderId, String status,
                                String symbol, String side, String orderType, int quantity,
                                double requestedPrice, double executionPrice, double totalAmount,
                                String message, String error, String transactionId) {
        this.success = success;
        this.statusCode = statusCode;
        this.orderId = orderId;
        this.status = status;
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
        this.quantity = quantity;
        this.requestedPrice = requestedPrice;
        this.executionPrice = executionPrice;
        this.totalAmount = totalAmount;
        this.message = message;
        this.error = error;
        this.transactionId = transactionId;
    }

    public static OrderExecutionResult error(int statusCode, String error, String symbol, String side, String orderType, int quantity, double price) {
        return new OrderExecutionResult(false, statusCode, null, "REJECTED", symbol, side, orderType, quantity, price, 0.0, 0.0, null, error, null);
    }

    public static OrderExecutionResult rejected(String orderId, String reason, String symbol, String side, String orderType, int quantity, double price, String transactionId) {
        return new OrderExecutionResult(false, 400, orderId, "REJECTED", symbol, side, orderType, quantity, price, 0.0, 0.0, null, reason, transactionId);
    }

    public static OrderExecutionResult filled(String orderId, String symbol, String side, String orderType,
                                              int quantity, double requestedPrice, double executionPrice,
                                              double totalAmount, String message, String transactionId) {
        return new OrderExecutionResult(true, 200, orderId, "FILLED", symbol, side, orderType, quantity,
                requestedPrice, executionPrice, totalAmount, message, null, transactionId);
    }

    public static OrderExecutionResult pending(String orderId, String symbol, String side, String orderType,
                                               int quantity, double requestedPrice, String message) {
        return new OrderExecutionResult(true, 200, orderId, "PENDING", symbol, side, orderType, quantity,
                requestedPrice, 0.0, 0.0, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public String getOrderType() { return orderType; }
    public int getQuantity() { return quantity; }
    public double getRequestedPrice() { return requestedPrice; }
    public double getExecutionPrice() { return executionPrice; }
    public double getTotalAmount() { return totalAmount; }
    public String getMessage() { return message; }
    public String getError() { return error; }
    public String getTransactionId() { return transactionId; }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"success\":%b,", success));
        if (orderId != null) sb.append(String.format("\"orderId\":\"%s\",", JsonParser.escape(orderId)));
        if (status != null) sb.append(String.format("\"status\":\"%s\",", JsonParser.escape(status)));
        if (symbol != null) sb.append(String.format("\"symbol\":\"%s\",", JsonParser.escape(symbol)));
        if (side != null) sb.append(String.format("\"side\":\"%s\",", JsonParser.escape(side)));
        if (orderType != null) sb.append(String.format("\"orderType\":\"%s\",", JsonParser.escape(orderType)));
        sb.append(String.format("\"quantity\":%d,", quantity));
        sb.append(String.format("\"requestedPrice\":%.2f,", requestedPrice));
        sb.append(String.format("\"executionPrice\":%.2f,", executionPrice));
        sb.append(String.format("\"totalAmount\":%.2f,", totalAmount));
        if (transactionId != null) sb.append(String.format("\"transactionId\":\"%s\",", JsonParser.escape(transactionId)));
        if (message != null) sb.append(String.format("\"message\":\"%s\",", JsonParser.escape(message)));
        if (error != null) sb.append(String.format("\"error\":\"%s\",", JsonParser.escape(error)));

        // Remove trailing comma if present
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }
}
