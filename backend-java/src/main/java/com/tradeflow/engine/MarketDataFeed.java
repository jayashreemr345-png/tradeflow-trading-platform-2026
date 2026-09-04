package com.tradeflow.engine;

import com.tradeflow.model.MarketData;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataFeed {
    private final Map<String, MarketData> marketDataMap = new ConcurrentHashMap<>();

    public void registerInstrument(String symbol, String name, double initialPrice) {
        marketDataMap.put(symbol, new MarketData(symbol, name, initialPrice));
    }

    public MarketData getMarketData(String symbol) {
        return marketDataMap.get(symbol);
    }

    public Map<String, MarketData> getAllMarketData() {
        return Collections.unmodifiableMap(marketDataMap);
    }

    public void updatePrice(String symbol, double newPrice) {
        MarketData data = marketDataMap.get(symbol);
        if (data != null) {
            data.updatePrice(newPrice);
        }
    }
}
