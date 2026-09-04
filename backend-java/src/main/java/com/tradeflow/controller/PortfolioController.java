package com.tradeflow.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tradeflow.engine.PortfolioManager;
import com.tradeflow.model.MarketData;
import com.tradeflow.model.Portfolio;
import com.tradeflow.model.TraderAccount;
import com.tradeflow.service.TradingEngineService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST controller for Portfolio endpoints:
 * - GET /api/portfolio
 */
public class PortfolioController implements HttpHandler {

    private final TradingEngineService engineService;

    public PortfolioController() {
        this.engineService = TradingEngineService.getInstance();
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

        TraderAccount account = engineService.getDefaultAccount();
        if (account == null) {
            sendJsonResponse(exchange, 404, "{\"error\":\"Account not found\"}");
            return;
        }

        Portfolio portfolio = account.getPortfolio();
        Map<String, MarketData> marketDataMap = engineService.getMarketDataFeed().getAllMarketData();
        PortfolioManager pm = engineService.getPortfolioManager();

        double totalPortfolioValue = pm.calculateTotalPortfolioValue(account, marketDataMap);
        double unrealizedPnL = pm.calculateUnrealizedPnL(account, marketDataMap);
        double cashBalance = account.getCashBalance();

        // Calculate total invested capital
        double investedCapital = 0.0;
        for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
            String sym = entry.getKey();
            int qty = entry.getValue();
            double avgCost = portfolio.getAvgBuyPrice(sym);
            investedCapital += qty * avgCost;
        }
        investedCapital = Math.round(investedCapital * 100.0) / 100.0;

        StringBuilder json = new StringBuilder("{");
        json.append(String.format("\"accountId\":\"%s\",", escapeJson(account.getAccountId())));
        json.append(String.format("\"traderName\":\"%s\",", escapeJson(account.getTraderName())));
        json.append(String.format("\"cashBalance\":%.2f,", cashBalance));
        json.append(String.format("\"portfolioValue\":%.2f,", totalPortfolioValue));
        json.append(String.format("\"investedCapital\":%.2f,", investedCapital));
        json.append(String.format("\"unrealizedPnL\":%.2f,", unrealizedPnL));
        
        // Holdings array
        json.append("\"holdings\":[");
        boolean firstHolding = true;
        for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
            String sym = entry.getKey();
            int qty = entry.getValue();
            double avgPrice = portfolio.getAvgBuyPrice(sym);
            MarketData md = marketDataMap.get(sym);
            double currentPrice = (md != null) ? md.getCurrentPrice() : avgPrice;
            double marketValue = Math.round((qty * currentPrice) * 100.0) / 100.0;
            double positionCost = Math.round((qty * avgPrice) * 100.0) / 100.0;
            double positionPnL = Math.round((marketValue - positionCost) * 100.0) / 100.0;
            double pnlPercent = positionCost > 0 ? Math.round(((positionPnL / positionCost) * 100.0) * 100.0) / 100.0 : 0.0;

            if (!firstHolding) json.append(",");
            json.append(String.format(
                "{\"symbol\":\"%s\",\"shares\":%d,\"averageCost\":%.2f,\"currentPrice\":%.2f," +
                "\"marketValue\":%.2f,\"totalCost\":%.2f,\"unrealizedPnL\":%.2f,\"pnlPercent\":%.2f}",
                escapeJson(sym),
                qty,
                avgPrice,
                currentPrice,
                marketValue,
                positionCost,
                positionPnL,
                pnlPercent
            ));
            firstHolding = false;
        }
        json.append("]}");

        sendJsonResponse(exchange, 200, json.toString());
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
