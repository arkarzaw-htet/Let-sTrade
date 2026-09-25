package com.letsTrade.demo.market.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsTrade.demo.market.dto.MarketPriceResponse;
import com.letsTrade.demo.market.dto.MarketStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BinanceClient {

    private static final Logger log = LoggerFactory.getLogger(BinanceClient.class);
    private static final String BINANCE_PRICE_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private static final String BINANCE_24HR_URL = "https://api.binance.com/api/v3/ticker/24hr?symbol=";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Fallback baseline prices in case Binance API is unreachable or rate-limited
    private final Map<String, BigDecimal> fallbackPrices = new ConcurrentHashMap<>();

    public BinanceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();

        fallbackPrices.put("BTCUSDT", new BigDecimal("95420.50"));
        fallbackPrices.put("ETHUSDT", new BigDecimal("2680.75"));
        fallbackPrices.put("SOLUSDT", new BigDecimal("152.30"));
        fallbackPrices.put("BNBUSDT", new BigDecimal("595.00"));
        fallbackPrices.put("DOGEUSDT", new BigDecimal("0.1650"));
        fallbackPrices.put("ADAUSDT", new BigDecimal("0.6800"));
        fallbackPrices.put("XRPUSDT", new BigDecimal("1.4500"));
    }

    public MarketPriceResponse getPrice(String symbol) {
        String cleanSymbol = symbol.trim().toUpperCase();
        try {
            String json = restTemplate.getForObject(BINANCE_PRICE_URL + cleanSymbol, String.class);
            if (json != null) {
                JsonNode node = objectMapper.readTree(json);
                BigDecimal price = new BigDecimal(node.get("price").asText());
                fallbackPrices.put(cleanSymbol, price);
                return new MarketPriceResponse(cleanSymbol, price);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live price for {} from Binance API: {}. Using fallback.", cleanSymbol, e.getMessage());
        }

        BigDecimal fallback = fallbackPrices.getOrDefault(cleanSymbol, new BigDecimal("100.00"));
        return new MarketPriceResponse(cleanSymbol, fallback);
    }

    public MarketStatsResponse getStats(String symbol) {
        String cleanSymbol = symbol.trim().toUpperCase();
        try {
            String json = restTemplate.getForObject(BINANCE_24HR_URL + cleanSymbol, String.class);
            if (json != null) {
                JsonNode node = objectMapper.readTree(json);
                BigDecimal lastPrice = new BigDecimal(node.get("lastPrice").asText());
                BigDecimal priceChangePercent = new BigDecimal(node.get("priceChangePercent").asText());
                BigDecimal highPrice = new BigDecimal(node.get("highPrice").asText());
                BigDecimal lowPrice = new BigDecimal(node.get("lowPrice").asText());
                BigDecimal volume = new BigDecimal(node.get("volume").asText());

                fallbackPrices.put(cleanSymbol, lastPrice);
                return new MarketStatsResponse(cleanSymbol, lastPrice, priceChangePercent, highPrice, lowPrice, volume);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch 24h stats for {} from Binance API: {}. Using fallback.", cleanSymbol, e.getMessage());
        }

        BigDecimal fallbackPrice = fallbackPrices.getOrDefault(cleanSymbol, new BigDecimal("100.00"));
        return new MarketStatsResponse(
                cleanSymbol,
                fallbackPrice,
                new BigDecimal("2.35"),
                fallbackPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP),
                fallbackPrice.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP),
                new BigDecimal("12450.00")
        );
    }

    public void updatePriceCache(String symbol, BigDecimal price) {
        if (symbol != null && price != null) {
            fallbackPrices.put(symbol.trim().toUpperCase(), price);
        }
    }
}
