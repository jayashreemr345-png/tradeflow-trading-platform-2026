package com.tradeflow.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration manager for TradeFlow Backend.
 */
public class AppConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load application.properties, using default values.");
        }
    }

    public static int getServerPort() {
        String envPort = System.getenv("TRADEFLOW_PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return Integer.parseInt(properties.getProperty("server.port", "8085"));
    }

    public static String getServerHost() {
        return properties.getProperty("server.host", "0.0.0.0");
    }

    public static String getAppName() {
        return properties.getProperty("app.name", "TradeFlow Trading Engine");
    }

    public static String getAppVersion() {
        return properties.getProperty("app.version", "1.0.0");
    }
}
