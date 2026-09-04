package com.tradeflow.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tradeflow.model.Transaction;
import com.tradeflow.service.TradingEngineService;
import com.tradeflow.util.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller handling GET /api/transactions to view transaction audit history.
 */
public class TransactionController implements HttpHandler {

    private final TradingEngineService engineService = TradingEngineService.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendResponse(exchange, 204, "");
            return;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "Method Not Allowed. Use GET to retrieve transactions.");
            return;
        }

        List<Transaction> transactions = engineService.getTransactionManager().getAllTransactions();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            json.append("{");
            json.append(String.format("\"transactionId\":\"%s\",", tx.getTransactionId()));
            json.append(String.format("\"accountId\":\"%s\",", tx.getAccountId()));
            json.append(String.format("\"orderId\":\"%s\",", tx.getOrderId()));
            json.append(String.format("\"symbol\":\"%s\",", tx.getSymbol()));
            json.append(String.format("\"type\":\"%s\",", tx.getType().name()));
            json.append(String.format("\"quantity\":%d,", tx.getQuantity()));
            json.append(String.format("\"price\":%.2f,", tx.getPrice()));
            json.append(String.format("\"totalAmount\":%.2f,", tx.getTotalAmount()));
            json.append(String.format("\"fee\":%.2f,", tx.getFee()));
            json.append(String.format("\"status\":\"%s\",", tx.getStatus()));
            json.append(String.format("\"timestamp\":\"%s\"", tx.getTimestamp().toString()));
            json.append("}");
            if (i < transactions.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        sendResponse(exchange, 200, json.toString());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (statusCode == 204 || bytes.length == 0) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        String json = String.format("{\"error\":\"%s\"}", JsonParser.escape(errorMessage));
        sendResponse(exchange, statusCode, json);
    }
}
