package com.tradeflow.engine;

import com.tradeflow.model.Order;
import com.tradeflow.model.OrderType;
import com.tradeflow.model.TraderAccount;

public class RiskManager {
    private static final double MAX_SINGLE_ORDER_VALUE = 250_000.0;
    private static final int MAX_SINGLE_ORDER_SHARES = 50_000;

    public boolean checkRisk(TraderAccount account, Order order, double currentMarketPrice, StringBuilder reason) {
        if (!OrderValidator.validate(order, reason)) {
            return false;
        }

        double effectivePrice = order.getPrice() > 0 ? order.getPrice() : currentMarketPrice;
        double totalOrderValue = effectivePrice * order.getQuantity();

        if (order.getQuantity() > MAX_SINGLE_ORDER_SHARES) {
            reason.append(String.format("Order exceeds maximum allowed shares per order (%d shares).", MAX_SINGLE_ORDER_SHARES));
            return false;
        }

        if (totalOrderValue > MAX_SINGLE_ORDER_VALUE) {
            reason.append(String.format("Order value ($%.2f) exceeds single order risk threshold ($%.2f).", totalOrderValue, MAX_SINGLE_ORDER_VALUE));
            return false;
        }

        if (order.getType() == OrderType.BUY) {
            if (account.getCashBalance() < totalOrderValue) {
                reason.append(String.format("Insufficient funds: Required $%.2f, available balance $%.2f.", totalOrderValue, account.getCashBalance()));
                return false;
            }
        } else if (order.getType() == OrderType.SELL) {
            int availableShares = account.getPortfolio().getQuantity(order.getSymbol());
            if (availableShares < order.getQuantity()) {
                reason.append(String.format("Insufficient shares: Required %d %s, owned %d shares.", order.getQuantity(), order.getSymbol(), availableShares));
                return false;
            }
        }

        return true;
    }
}
