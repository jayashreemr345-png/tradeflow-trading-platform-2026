package com.tradeflow.engine;

import com.tradeflow.model.Order;

public class OrderValidator {

    public static boolean validate(Order order, StringBuilder errorReason) {
        if (order == null) {
            errorReason.append("Order cannot be null.");
            return false;
        }
        if (order.getSymbol() == null || order.getSymbol().trim().isEmpty()) {
            errorReason.append("Stock symbol is required.");
            return false;
        }
        if (order.getQuantity() <= 0) {
            errorReason.append("Quantity must be greater than zero.");
            return false;
        }
        if ("LIMIT".equalsIgnoreCase(order.getExecutionType()) && order.getPrice() <= 0) {
            errorReason.append("Limit price must be greater than zero.");
            return false;
        }
        return true;
    }
}
