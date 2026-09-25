package com.letsTrade.demo.market.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsTrade.demo.market.dto.MarketPriceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MarketWebSocketHandler.class);
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public MarketWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("New raw WebSocket client connected: {}", session.getId());
        session.sendMessage(new TextMessage("{\"message\":\"Connected to Let's Trade Live Price Stream!\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Raw WebSocket client disconnected: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
        log.warn("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    public void broadcast(MarketPriceResponse priceUpdate) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(priceUpdate);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(message);
                        }
                    } catch (IOException e) {
                        log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize price update: {}", e.getMessage());
        }
    }
}
