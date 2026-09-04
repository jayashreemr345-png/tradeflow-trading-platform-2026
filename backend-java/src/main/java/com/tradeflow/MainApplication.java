package com.tradeflow;

import com.sun.net.httpserver.HttpServer;
import com.tradeflow.config.AppConfig;
import com.tradeflow.controller.HealthController;
import com.tradeflow.controller.MarketDataController;
import com.tradeflow.controller.PortfolioController;
import com.tradeflow.service.TradingEngineService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application bootstrap for TradeFlow REST API backend.
 */
public class MainApplication {

    private static HttpServer server;

    public static void main(String[] args) {
        startServer();
    }

    public static synchronized void startServer() {
        if (server != null) {
            System.out.println("TradeFlow server is already running.");
            return;
        }

        try {
            // Eagerly initialize core trading engine and background market simulator
            TradingEngineService engineService = TradingEngineService.getInstance();
            System.out.println("Trading Engine initialized for trader: " + 
                engineService.getDefaultAccount().getTraderName() + " (" + 
                engineService.getDefaultAccount().getAccountId() + ")");

            int port = AppConfig.getServerPort();
            String host = AppConfig.getServerHost();
            server = HttpServer.create(new InetSocketAddress(host, port), 0);

            // Register REST endpoints
            server.createContext("/api/health", new HealthController());
            server.createContext("/api/market/stocks", new MarketDataController());
            server.createContext("/api/portfolio", new PortfolioController());
            server.createContext("/api/orders", new com.tradeflow.controller.OrderController());
            server.createContext("/api/transactions", new com.tradeflow.controller.TransactionController());

            // Multi-threaded executor for handling concurrent HTTP requests
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("==================================================");
            System.out.println("  " + AppConfig.getAppName() + " v" + AppConfig.getAppVersion());
            System.out.println("  Listening on: http://" + host + ":" + port);
            System.out.println("  Health check: http://" + host + ":" + port + "/api/health");
            System.out.println("  Market data:  http://" + host + ":" + port + "/api/market/stocks");
            System.out.println("  Portfolio:    http://" + host + ":" + port + "/api/portfolio");
            System.out.println("==================================================");

            // Graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Stopping TradeFlow server...");
                engineService.stopMarketSimulation();
                stopServer();
            }));

        } catch (IOException e) {
            System.err.println("Failed to start TradeFlow server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(1);
            server = null;
            System.out.println("TradeFlow server stopped.");
        }
    }
}
