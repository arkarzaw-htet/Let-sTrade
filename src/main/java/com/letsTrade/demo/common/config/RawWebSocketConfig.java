package com.letsTrade.demo.common.config;

import com.letsTrade.demo.market.websocket.MarketWebSocketHandler;
import com.letsTrade.demo.portfolio.websocket.PortfolioWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final MarketWebSocketHandler marketWebSocketHandler;
    private final PortfolioWebSocketHandler portfolioWebSocketHandler;

    public RawWebSocketConfig(
            MarketWebSocketHandler marketWebSocketHandler,
            PortfolioWebSocketHandler portfolioWebSocketHandler
    ) {
        this.marketWebSocketHandler = marketWebSocketHandler;
        this.portfolioWebSocketHandler = portfolioWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register raw market price endpoints
        registry.addHandler(marketWebSocketHandler, "/ws/prices", "/ws/market/raw", "/ws/raw")
                .setAllowedOriginPatterns("*");

        // Register raw portfolio holdings endpoints
        registry.addHandler(portfolioWebSocketHandler, "/ws/portfolio", "/ws/portfolio/raw", "/ws/holdings")
                .setAllowedOriginPatterns("*");
    }
}
