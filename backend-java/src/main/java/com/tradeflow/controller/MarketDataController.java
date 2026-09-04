package com.tradeflow.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tradeflow.engine.MarketDataFeed;
import com.tradeflow.model.MarketData;
import com.tradeflow.service.TradingEngineService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST controller for Market Data endpoints:
 * - GET /api/market/stocks
 * - GET /api/market/stocks/{symbol}
 */
public class MarketDataController implements HttpHandler {

    private final MarketDataFeed marketDataFeed;

    public MarketDataController() {
        this.marketDataFeed = TradingEngineService.getInstance().getMarketDataFeed();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set standard CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        // Base path is /api/market/stocks
        // Check if a specific symbol was requested (e.g., /api/market/stocks/AAPL)
        String prefix = "/api/market/stocks";
        String remainder = path.substring(prefix.length());

        if (remainder.isEmpty() || remainder.equals("/")) {
            // Return all stocks
            handleGetAllStocks(exchange);
        } else {
            // Sub-path /<symbol>
            String symbol = remainder.startsWith("/") ? remainder.substring(1).trim().toUpperCase() : remainder.trim().toUpperCase();
            handleGetStockBySymbol(exchange, symbol);
        }
    }

    private void handleGetAllStocks(HttpExchange exchange) throws IOException {
        Map<String, MarketData> allData = marketDataFeed.getAllMarketData();
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (MarketData md : allData.values()) {
            if (!first) json.append(",");
            json.append(toJson(md));
            first = false;
        }
        json.append("]");
        sendJsonResponse(exchange, 200, json.toString());
    }

    private void handleGetStockBySymbol(HttpExchange exchange, String symbol) throws IOException {
        MarketData md = marketDataFeed.getMarketData(symbol);
        if (md == null) {
            sendJsonResponse(exchange, 404, String.format("{\"error\":\"Stock symbol '%s' not found\"}", escapeJson(symbol)));
            return;
        }
        sendJsonResponse(exchange, 200, toJson(md));
    }

    private String toJson(MarketData md) {
        return String.format(
            "{\"symbol\":\"%s\",\"name\":\"%s\",\"price\":%.2f,\"open\":%.2f,\"high\":%.2f,\"low\":%.2f," +
            "\"previousClose\":%.2f,\"volume\":%d,\"change\":%.2f,\"changePercent\":%.2f,\"bid\":%.2f,\"ask\":%.2f,\"lastUpdated\":\"%s\"}",
            escapeJson(md.getSymbol()),
            escapeJson(md.getName()),
            md.getCurrentPrice(),
            md.getOpenPrice(),
            md.getHighPrice(),
            md.getLowPrice(),
            md.getPreviousClose(),
            md.getVolume(),
            md.getChange(),
            md.getChangePercent(),
            md.getBidPrice(),
            md.getAskPrice(),
            md.getLastUpdated() != null ? md.getLastUpdated().toString() : ""
        );
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
