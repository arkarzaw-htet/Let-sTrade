package com.letsTrade.demo.portfolio.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsTrade.demo.auth.security.JwtService;
import com.letsTrade.demo.portfolio.dto.PortfolioResponse;
import com.letsTrade.demo.portfolio.service.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PortfolioWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PortfolioWebSocketHandler.class);

    private final PortfolioService portfolioService;
    private final JwtService jwtService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Map of userEmail -> Set of active WebSocketSessions
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // Map of session.getId() -> userEmail
    private final Map<String, String> sessionEmailMap = new ConcurrentHashMap<>();

    public PortfolioWebSocketHandler(
            @Lazy PortfolioService portfolioService,
            JwtService jwtService,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ) {
        this.portfolioService = portfolioService;
        this.jwtService = jwtService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        if (token != null && jwtService.isValid(token)) {
            String email = jwtService.extractEmail(token);
            authenticateSession(session, email);
        } else {
            session.sendMessage(new TextMessage(
                    "{\"status\":\"AUTHENTICATION_REQUIRED\",\"message\":\"Please authenticate with a valid JWT token via ?token=... or send {\\\"token\\\":\\\"<jwt>\\\"}\"}"
            ));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try {
            JsonNode node = objectMapper.readTree(payload);
            String token = node.has("token") ? node.get("token").asText() : null;
            if (token != null && jwtService.isValid(token)) {
                String email = jwtService.extractEmail(token);
                authenticateSession(session, email);
            } else {
                session.sendMessage(new TextMessage("{\"status\":\"ERROR\",\"message\":\"Invalid or expired token\"}"));
            }
        } catch (Exception e) {
            log.warn("Invalid message received on portfolio websocket: {}", e.getMessage());
        }
    }

    private void authenticateSession(WebSocketSession session, String email) throws IOException {
        sessionEmailMap.put(session.getId(), email);
        userSessions.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("Portfolio WebSocket authenticated for user: {}", email);

        // Send welcome & immediate current portfolio snapshot
        sendCurrentPortfolioToSession(session, email);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String email = sessionEmailMap.remove(session.getId());
        if (email != null) {
            Set<WebSocketSession> sessions = userSessions.get(email);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(email);
                }
            }
        }
        log.info("Portfolio WebSocket closed for session: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    public void sendCurrentPortfolioToSession(WebSocketSession session, String email) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            PortfolioResponse portfolio = portfolioService.getPortfolio(email);
            String json = objectMapper.writeValueAsString(portfolio);
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.error("Failed to send portfolio snapshot to user {}: {}", email, e.getMessage());
        }
    }

    public void notifyUserPortfolioChanged(String email) {
        if (email == null) return;

        try {
            PortfolioResponse portfolio = portfolioService.getPortfolio(email);
            String json = objectMapper.writeValueAsString(portfolio);
            TextMessage textMessage = new TextMessage(json);

            // 1. Send to raw WebSocket sessions for this user
            Set<WebSocketSession> sessions = userSessions.get(email);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        try {
                            synchronized (s) {
                                s.sendMessage(textMessage);
                            }
                        } catch (IOException ex) {
                            log.warn("Error sending portfolio update to session {}: {}", s.getId(), ex.getMessage());
                        }
                    }
                }
            }

            // 2. Also send over STOMP user queue and topic
            messagingTemplate.convertAndSendToUser(email, "/queue/portfolio", portfolio);
            messagingTemplate.convertAndSend("/topic/portfolio/" + email, portfolio);
        } catch (Exception e) {
            log.error("Failed to notify user portfolio changed for {}: {}", email, e.getMessage());
        }
    }

    public void onPriceUpdated(String symbol) {
        if (userSessions.isEmpty()) {
            return;
        }
        // Recalculate portfolio for all connected active users
        for (String email : userSessions.keySet()) {
            notifyUserPortfolioChanged(email);
        }
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String query = uri.getQuery();
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && ("token".equalsIgnoreCase(pair[0]) || "bearer".equalsIgnoreCase(pair[0]) || "auth".equalsIgnoreCase(pair[0]))) {
                    return pair[1];
                }
            }
        }

        // Try Authorization header
        var headers = session.getHandshakeHeaders();
        String authHeader = headers.getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
