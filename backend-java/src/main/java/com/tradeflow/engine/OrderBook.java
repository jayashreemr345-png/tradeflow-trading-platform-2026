package com.tradeflow.engine;

import com.tradeflow.model.Order;
import com.tradeflow.model.OrderType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OrderBook {
    private final String symbol;

    // Buy orders (Bids): highest price first, then earliest timestamp
    private final PriorityQueue<Order> bids = new PriorityQueue<>(
            Comparator.comparingDouble(Order::getPrice).reversed()
                    .thenComparing(Order::getTimestamp)
    );

    // Sell orders (Asks): lowest price first, then earliest timestamp
    private final PriorityQueue<Order> asks = new PriorityQueue<>(
            Comparator.comparingDouble(Order::getPrice)
                    .thenComparing(Order::getTimestamp)
    );

    private final Map<String, Order> orderIndex = new ConcurrentHashMap<>();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public synchronized void addOrder(Order order) {
        orderIndex.put(order.getOrderId(), order);
        if (order.getType() == OrderType.BUY) {
            bids.add(order);
        } else {
            asks.add(order);
        }
    }

    public synchronized boolean cancelOrder(String orderId) {
        Order order = orderIndex.get(orderId);
        if (order != null && !order.isFilled() && !"CANCELLED".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            if (order.getType() == OrderType.BUY) {
                bids.remove(order);
            } else {
                asks.remove(order);
            }
            return true;
        }
        return false;
    }

    public synchronized Order peekBestBid() {
        while (!bids.isEmpty() && (bids.peek().isFilled() || "CANCELLED".equals(bids.peek().getStatus()))) {
            bids.poll();
        }
        return bids.peek();
    }

    public synchronized Order peekBestAsk() {
        while (!asks.isEmpty() && (asks.peek().isFilled() || "CANCELLED".equals(asks.peek().getStatus()))) {
            asks.poll();
        }
        return asks.peek();
    }

    public synchronized Order pollBestBid() {
        peekBestBid();
        return bids.poll();
    }

    public synchronized Order pollBestAsk() {
        peekBestAsk();
        return asks.poll();
    }

    public synchronized double getSpread() {
        Order bestBid = peekBestBid();
        Order bestAsk = peekBestAsk();
        if (bestBid != null && bestAsk != null) {
            return Math.round((bestAsk.getPrice() - bestBid.getPrice()) * 100.0) / 100.0;
        }
        return 0.0;
    }

    public synchronized List<Order> getActiveBids() {
        return new ArrayList<>(bids);
    }

    public synchronized List<Order> getActiveAsks() {
        return new ArrayList<>(asks);
    }

    public String getSymbol() { return symbol; }
}
