package com.tradeflow.engine;

import com.tradeflow.model.MarketData;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MarketDataSimulator {
    private final MarketDataFeed feed;
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;
    private volatile boolean running = false;

    public MarketDataSimulator(MarketDataFeed feed) {
        this.feed = feed;
    }

    public synchronized void start(long intervalMillis) {
        if (running) return;
        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public void tick() {
        for (MarketData data : feed.getAllMarketData().values()) {
            // Random fluctuation between -1.5% and +1.5%
            double deltaPct = (random.nextDouble() * 0.03) - 0.015;
            double newPrice = data.getCurrentPrice() * (1.0 + deltaPct);
            feed.updatePrice(data.getSymbol(), newPrice);
        }
    }

    public boolean isRunning() {
        return running;
    }
}
