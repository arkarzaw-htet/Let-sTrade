package com.letsTrade.demo.market.service;

import com.letsTrade.demo.market.client.BinanceClient;
import com.letsTrade.demo.market.dto.MarketPriceResponse;
import com.letsTrade.demo.market.dto.MarketStatsResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MarketService {

    private final BinanceClient binanceClient;

    public MarketService(BinanceClient binanceClient) {
        this.binanceClient = binanceClient;
    }

    public MarketPriceResponse getPrice(String symbol) {
        return binanceClient.getPrice(symbol);
    }

    public BigDecimal getCurrentPrice(String symbol) {
        return binanceClient.getPrice(symbol).getPrice();
    }

    public MarketStatsResponse getStats(String symbol) {
        return binanceClient.getStats(symbol);
    }
}
