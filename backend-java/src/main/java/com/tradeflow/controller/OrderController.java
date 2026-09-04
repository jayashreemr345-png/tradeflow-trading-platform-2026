package com.tradeflow.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tradeflow.model.Order;
import com.tradeflow.model.OrderExecutionResult;
import com.tradeflow.model.Transaction;
import com.tradeflow.service.TradingEngineService;
import com.tradeflow.util.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller handling REST API routes for trade orders:
 * - POST /api/orders/submit
 * - GET  /api/orders/active
 * - POST /api/orders/cancel/{id}
 * - GET  /api/orders/transactions
 */
public class OrderController implements HttpHandler {

    private final TradingEngineService engineService = TradingEngineService.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Handle CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendResponse(exchange, 204, "");
            return;
        }

        try {
            if (path.equals("/api/orders/submit")) {
                if ("POST".equalsIgnoreCase(method)) {
                    handleSubmitOrder(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed. Use POST for order submission.");
                }
            } else if (path.equals("/api/orders/active")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetActiveOrders(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed. Use GET for active orders.");
                }
            } else if (path.startsWith("/api/orders/cancel")) {
                if ("POST".equalsIgnoreCase(method)) {
                    handleCancelOrder(exchange, path);
                } else {
                    sendError(exchange, 405, "Method Not Allowed. Use POST for cancelling orders.");
                }
            } else if (path.equals("/api/orders/transactions") || path.equals("/api/orders/history")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetTransactions(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed. Use GET for transactions.");
                }
            } else {
                sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleSubmitOrder(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null || body.trim().isEmpty()) {
            sendError(exchange, 400, "Request body is empty.");
            return;
        }

        String symbol = JsonParser.getString(body, "symbol");
        String side = JsonParser.getString(body, "side");

        // Try orderType first, fallback to type or executionType
        String orderType = JsonParser.getString(body, "orderType");
        if (orderType == null) orderType = JsonParser.getString(body, "type");
        if (orderType == null) orderType = JsonParser.getString(body, "executionType");

        Integer quantity = JsonParser.getInt(body, "quantity");
        Double price = JsonParser.getDouble(body, "price");
        String accountId = JsonParser.getString(body, "accountId");

        if (quantity == null) {
            sendError(exchange, 400, "Order quantity is required and must be an integer.");
            return;
        }

        double finalPrice = (price != null) ? price : 0.0;

        OrderExecutionResult result = engineService.submitOrder(
                accountId, symbol, side, orderType, finalPrice, quantity
        );

        sendResponse(exchange, result.getStatusCode(), result.toJson());
    }

    private void handleGetActiveOrders(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String accountId = null;
        if (query != null && query.contains("accountId=")) {
            for (String param : query.split("&")) {
                if (param.startsWith("accountId=")) {
                    accountId = param.substring("accountId=".length());
                }
            }
        }

        List<Order> activeOrders = engineService.getActiveOrders(accountId);
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < activeOrders.size(); i++) {
            Order o = activeOrders.get(i);
            json.append("{");
            json.append(String.format("\"orderId\":\"%s\",", o.getOrderId()));
            json.append(String.format("\"accountId\":\"%s\",", o.getAccountId()));
            json.append(String.format("\"symbol\":\"%s\",", o.getSymbol()));
            json.append(String.format("\"side\":\"%s\",", o.getType().name()));
            json.append(String.format("\"orderType\":\"%s\",", o.getExecutionType()));
            json.append(String.format("\"price\":%.2f,", o.getPrice()));
            json.append(String.format("\"quantity\":%d,", o.getQuantity()));
            json.append(String.format("\"filledQuantity\":%d,", o.getFilledQuantity()));
            json.append(String.format("\"remainingQuantity\":%d,", o.getRemainingQuantity()));
            json.append(String.format("\"status\":\"%s\",", o.getStatus()));
            json.append(String.format("\"timestamp\":\"%s\"", o.getTimestamp().toString()));
            json.append("}");
            if (i < activeOrders.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        sendResponse(exchange, 200, json.toString());
    }

    private void handleCancelOrder(HttpExchange exchange, String path) throws IOException {
        String orderId = null;

        // 1. Try URL path /api/orders/cancel/{id}
        if (path.length() > "/api/orders/cancel/".length()) {
            orderId = path.substring("/api/orders/cancel/".length()).trim();
        }

        // 2. Try URL query /api/orders/cancel?id=...
        if (orderId == null || orderId.isEmpty()) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=") || param.startsWith("orderId=")) {
                        orderId = param.substring(param.indexOf('=') + 1).trim();
                    }
                }
            }
        }

        // 3. Try request body
        if (orderId == null || orderId.isEmpty()) {
            String body = readRequestBody(exchange);
            if (body != null && !body.trim().isEmpty()) {
                orderId = JsonParser.getString(body, "orderId");
                if (orderId == null) orderId = JsonParser.getString(body, "id");
            }
        }

        if (orderId == null || orderId.isEmpty()) {
            sendError(exchange, 400, "Order ID is required to cancel an order.");
            return;
        }

        StringBuilder reason = new StringBuilder();
        boolean cancelled = engineService.cancelOrder(orderId, reason);

        if (cancelled) {
            String json = String.format("{\"success\":true,\"status\":\"CANCELLED\",\"orderId\":\"%s\",\"message\":\"Order %s successfully cancelled.\"}", orderId, orderId);
            sendResponse(exchange, 200, json);
        } else {
            int code = reason.toString().contains("not found") ? 404 : 400;
            String json = String.format("{\"success\":false,\"orderId\":\"%s\",\"error\":\"%s\"}", orderId, JsonParser.escape(reason.toString()));
            sendResponse(exchange, code, json);
        }
    }

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
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

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
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
