package com.letsTrade.demo.market.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsTrade.demo.market.client.BinanceClient;
import com.letsTrade.demo.market.dto.MarketPriceResponse;
import com.letsTrade.demo.market.websocket.MarketWebSocketHandler;
import com.letsTrade.demo.portfolio.websocket.PortfolioWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class BinanceWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketService.class);
    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/!miniTicker@arr";

    private final BinanceClient binanceClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final MarketWebSocketHandler marketWebSocketHandler;
    private final PortfolioWebSocketHandler portfolioWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private WebSocket webSocket;
    private final Map<String, BigDecimal> lastBroadcastPrices = new ConcurrentHashMap<>();

    public BinanceWebSocketService(
            BinanceClient binanceClient,
            SimpMessagingTemplate messagingTemplate,
            MarketWebSocketHandler marketWebSocketHandler,
            PortfolioWebSocketHandler portfolioWebSocketHandler,
            ObjectMapper objectMapper
    ) {
        this.binanceClient = binanceClient;
        this.messagingTemplate = messagingTemplate;
        this.marketWebSocketHandler = marketWebSocketHandler;
        this.portfolioWebSocketHandler = portfolioWebSocketHandler;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        connectToBinanceWebSocket();
    }

    @PreDestroy
    public void stop() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Application shutting down");
        }
        scheduler.shutdown();
    }

    private synchronized void connectToBinanceWebSocket() {
        try {
            log.info("Connecting to Binance WebSocket stream at {}", BINANCE_WS_URL);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            client.newWebSocketBuilder()
                    .buildAsync(URI.create(BINANCE_WS_URL), new BinanceWebSocketListener())
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        log.info("Successfully connected to Binance live price stream!");
                    })
                    .exceptionally(ex -> {
                        log.warn("Failed to connect to Binance WebSocket: {}. Retrying in 5s...", ex.getMessage());
                        scheduleReconnect();
                        return null;
                    });
        } catch (Exception e) {
            log.warn("Error initiating Binance WebSocket connection: {}. Retrying in 5s...", e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        scheduler.schedule(this::connectToBinanceWebSocket, 5, TimeUnit.SECONDS);
    }

    private class BinanceWebSocketListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("Binance WebSocket stream opened and active.");
            webSocket.request(1);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String message = buffer.toString();
                buffer.setLength(0);
                processIncomingPriceMessage(message);
            }
            webSocket.request(1);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            webSocket.request(1);
            return WebSocket.Listener.super.onPing(webSocket, message);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("Binance WebSocket closed (code: {}, reason: {}). Reconnecting in 5s...", statusCode, reason);
            scheduleReconnect();
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("Binance WebSocket error: {}. Reconnecting in 5s...", error.getMessage());
            scheduleReconnect();
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }

    private void processIncomingPriceMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.isArray()) {
                for (JsonNode ticker : root) {
                    String symbol = ticker.path("s").asText(); // e.g. BTCUSDT
                    String priceStr = ticker.path("c").asText(); // close price

                    if (!symbol.isEmpty() && !priceStr.isEmpty()) {
                        BigDecimal currentPrice = new BigDecimal(priceStr);
                        BigDecimal previousPrice = lastBroadcastPrices.get(symbol);

                        // If price changed or first seen
                        if (previousPrice == null || previousPrice.compareTo(currentPrice) != 0) {
                            lastBroadcastPrices.put(symbol, currentPrice);

                            // Update internal cache
                            binanceClient.updatePriceCache(symbol, currentPrice);

                            MarketPriceResponse priceUpdate = new MarketPriceResponse(symbol, currentPrice);

                            // 1. Broadcast to Raw WebSocket clients (e.g. Postman, direct WebSocket)
                            marketWebSocketHandler.broadcast(priceUpdate);

                            // 2. Broadcast to STOMP topic /topic/prices
                            messagingTemplate.convertAndSend("/topic/prices", priceUpdate);

                            // 3. Broadcast to STOMP symbol topic /topic/market/BTCUSDT
                            messagingTemplate.convertAndSend("/topic/market/" + symbol, priceUpdate);

                            // 4. Recalculate and stream portfolio updates to connected users
                            portfolioWebSocketHandler.onPriceUpdated(symbol);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error parsing Binance stream message: {}", e.getMessage());
        }
    }
}
