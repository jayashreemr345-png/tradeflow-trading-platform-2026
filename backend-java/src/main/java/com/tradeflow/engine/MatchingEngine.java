package com.tradeflow.engine;

import com.tradeflow.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final List<Trade> tradeHistory = new CopyOnWriteArrayList<>();

    public OrderBook getOrCreateOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, OrderBook::new);
    }

    public synchronized List<Trade> match(Order incomingOrder) {
        List<Trade> trades = new ArrayList<>();
        OrderBook book = getOrCreateOrderBook(incomingOrder.getSymbol());

        if (incomingOrder.getType() == OrderType.BUY) {
            matchBuyOrder(incomingOrder, book, trades);
        } else {
            matchSellOrder(incomingOrder, book, trades);
        }

        if (!incomingOrder.isFilled() && !"CANCELLED".equals(incomingOrder.getStatus())) {
            book.addOrder(incomingOrder);
        }

        tradeHistory.addAll(trades);
        return trades;
    }

    private void matchBuyOrder(Order buyOrder, OrderBook book, List<Trade> trades) {
        while (!buyOrder.isFilled()) {
            Order bestAsk = book.peekBestAsk();
            if (bestAsk == null) break;

            boolean isMarket = "MARKET".equalsIgnoreCase(buyOrder.getExecutionType());
            if (!isMarket && buyOrder.getPrice() < bestAsk.getPrice()) {
                // Limit price lower than best available ask -> cannot cross
                break;
            }

            int matchQuantity = Math.min(buyOrder.getRemainingQuantity(), bestAsk.getRemainingQuantity());
            double executionPrice = bestAsk.getPrice();

            buyOrder.addFill(matchQuantity);
            bestAsk.addFill(matchQuantity);

            String tradeId = "TRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Trade trade = new Trade(tradeId, buyOrder.getOrderId(), bestAsk.getOrderId(),
                    buyOrder.getSymbol(), executionPrice, matchQuantity);
            trades.add(trade);

            if (bestAsk.isFilled()) {
                book.pollBestAsk();
            }
        }
    }

    private void matchSellOrder(Order sellOrder, OrderBook book, List<Trade> trades) {
        while (!sellOrder.isFilled()) {
            Order bestBid = book.peekBestBid();
            if (bestBid == null) break;

            boolean isMarket = "MARKET".equalsIgnoreCase(sellOrder.getExecutionType());
            if (!isMarket && sellOrder.getPrice() > bestBid.getPrice()) {
                // Limit price higher than best available bid -> cannot cross
                break;
            }

            int matchQuantity = Math.min(sellOrder.getRemainingQuantity(), bestBid.getRemainingQuantity());
            double executionPrice = bestBid.getPrice();

            sellOrder.addFill(matchQuantity);
            bestBid.addFill(matchQuantity);

            String tradeId = "TRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Trade trade = new Trade(tradeId, bestBid.getOrderId(), sellOrder.getOrderId(),
                    sellOrder.getSymbol(), executionPrice, matchQuantity);
            trades.add(trade);

            if (bestBid.isFilled()) {
                book.pollBestBid();
            }
        }
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }
}
