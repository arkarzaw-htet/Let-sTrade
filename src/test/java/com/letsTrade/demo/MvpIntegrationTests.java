package com.letsTrade.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsTrade.demo.auth.dto.LoginRequest;
import com.letsTrade.demo.auth.dto.RegisterRequest;
import com.letsTrade.demo.order.dto.CreateOrderRequest;
import com.letsTrade.demo.order.enums.OrderSide;
import com.letsTrade.demo.wallet.dto.DepositRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MvpIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static String token;
    private static final String testEmail = "trader_" + UUID.randomUUID() + "@example.com";
    private static final String testPassword = "Password123!";
    private static Long orderId;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    @Test
    @Order(1)
    void testRegisterUserAndAutoCreateWallet() {
        RegisterRequest registerReq = new RegisterRequest("Satoshi Nakamoto", testEmail, testPassword);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/auth/register",
                registerReq,
                Void.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Order(2)
    void testLoginUser() throws Exception {
        LoginRequest loginReq = new LoginRequest(testEmail, testPassword);
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/auth/login",
                loginReq,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        String responseBody = response.getBody();
        if (responseBody.startsWith("{")) {
            JsonNode node = objectMapper.readTree(responseBody);
            token = node.has("token") ? node.get("token").asText() : responseBody;
        } else {
            token = responseBody;
        }
        assertNotNull(token);
        assertFalse(token.trim().isEmpty());
    }

    @Test
    @Order(3)
    void testGetProfile() {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/users/me",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(testEmail));
        assertTrue(response.getBody().contains("Satoshi Nakamoto"));
    }

    @Test
    @Order(4)
    void testGetTradableCryptosAndDetails() {
        // FR-04: View tradable cryptocurrencies
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/cryptocurrencies",
                String.class
        );
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertTrue(listResponse.getBody().contains("BTCUSDT"));
        assertTrue(listResponse.getBody().contains("ETHUSDT"));

        // FR-05: View crypto details
        ResponseEntity<String> detailsResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/cryptocurrencies/BTCUSDT",
                String.class
        );
        assertEquals(HttpStatus.OK, detailsResponse.getStatusCode());
        assertTrue(detailsResponse.getBody().contains("Bitcoin"));
        assertTrue(detailsResponse.getBody().contains("BTC"));
    }

    @Test
    @Order(5)
    void testGetMarketPriceAndStats() {
        // FR-06: View Current Market Price
        ResponseEntity<String> priceResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/market/BTCUSDT/price",
                String.class
        );
        assertEquals(HttpStatus.OK, priceResponse.getStatusCode());
        assertTrue(priceResponse.getBody().contains("price"));

        // Market Stats
        ResponseEntity<String> statsResponse = restTemplate.getForEntity(
                getBaseUrl() + "/api/market/BTCUSDT/stats",
                String.class
        );
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        assertTrue(statsResponse.getBody().contains("priceChangePercent"));
    }

    @Test
    @Order(6)
    void testGetWalletInitialBalance() throws Exception {
        // FR-08: View Wallet Balance (Initial 10,000 USDT)
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/wallet",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = objectMapper.readTree(response.getBody());
        assertTrue(node.has("assets"));
        boolean foundUsdt = false;
        for (JsonNode assetNode : node.get("assets")) {
            if ("USDT".equals(assetNode.get("symbol").asText())) {
                assertEquals(10000.0, assetNode.get("quantity").asDouble(), 0.01);
                foundUsdt = true;
            }
        }
        assertTrue(foundUsdt);
    }

    @Test
    @Order(7)
    void testDepositPaperMoney() throws Exception {
        // FR-09: Deposit Paper Money
        DepositRequest depositReq = new DepositRequest("USDT", new BigDecimal("5000.00"));
        HttpEntity<DepositRequest> entity = new HttpEntity<>(depositReq, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/wallet/deposit",
                HttpMethod.POST,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = objectMapper.readTree(response.getBody());
        for (JsonNode assetNode : node.get("assets")) {
            if ("USDT".equals(assetNode.get("symbol").asText())) {
                assertEquals(15000.0, assetNode.get("quantity").asDouble(), 0.01);
            }
        }
    }

    @Test
    @Order(8)
    void testPlaceBuyOrderAndExecution() throws Exception {
        // FR-10, FR-12: Place Buy Order and Execute
        CreateOrderRequest orderReq = new CreateOrderRequest("BTCUSDT", OrderSide.BUY, new BigDecimal("0.01"));
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(orderReq, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/orders",
                HttpMethod.POST,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = objectMapper.readTree(response.getBody());
        assertEquals("BTCUSDT", node.get("symbol").asText());
        assertEquals("BUY", node.get("side").asText());
        assertEquals("FILLED", node.get("status").asText());
        assertEquals(0.01, node.get("quantity").asDouble(), 0.0001);
        assertTrue(node.get("price").asDouble() > 0);
        assertTrue(node.get("totalAmount").asDouble() > 0);
        orderId = node.get("id").asLong();
    }

    @Test
    @Order(9)
    void testGetOrderHistoryAndSpecificOrder() throws Exception {
        // FR-13: View Order History
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> listResponse = restTemplate.exchange(
                getBaseUrl() + "/api/orders",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        JsonNode orders = objectMapper.readTree(listResponse.getBody());
        assertTrue(orders.isArray());
        assertTrue(orders.size() >= 1);

        // FR-14: View Specific Order
        ResponseEntity<String> singleResponse = restTemplate.exchange(
                getBaseUrl() + "/api/orders/" + orderId,
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, singleResponse.getStatusCode());
        JsonNode singleOrder = objectMapper.readTree(singleResponse.getBody());
        assertEquals(orderId.longValue(), singleOrder.get("id").asLong());
    }

    @Test
    @Order(10)
    void testPlaceSellOrderAndExecution() throws Exception {
        // FR-11, FR-12: Place Sell Order
        CreateOrderRequest orderReq = new CreateOrderRequest("BTCUSDT", OrderSide.SELL, new BigDecimal("0.005"));
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(orderReq, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/orders",
                HttpMethod.POST,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = objectMapper.readTree(response.getBody());
        assertEquals("BTCUSDT", node.get("symbol").asText());
        assertEquals("SELL", node.get("side").asText());
        assertEquals("FILLED", node.get("status").asText());
        assertEquals(0.005, node.get("quantity").asDouble(), 0.0001);
    }

    @Test
    @Order(11)
    void testPortfolio() throws Exception {
        // FR-15, FR-16: Portfolio Holdings and Total Value
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/portfolio",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = objectMapper.readTree(response.getBody());
        assertTrue(node.has("totalValueUsdt"));
        assertTrue(node.get("totalValueUsdt").asDouble() > 0);
        assertTrue(node.has("holdings"));
        assertTrue(node.get("holdings").isArray());
    }

    @Test
    @Order(12)
    void testInsufficientBalanceRejection() {
        // Attempt to buy 1000 BTC without enough funds
        CreateOrderRequest orderReq = new CreateOrderRequest("BTCUSDT", OrderSide.BUY, new BigDecimal("1000.0"));
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(orderReq, createAuthHeaders());
        try {
            restTemplate.exchange(
                    getBaseUrl() + "/api/orders",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            fail("Expected 400 Bad Request exception");
        } catch (HttpClientErrorException.BadRequest e) {
            assertTrue(e.getResponseBodyAsString().contains("Insufficient") || e.getResponseBodyAsString().contains("balance"));
        }
    }

    @Autowired
    private com.letsTrade.demo.portfolio.websocket.PortfolioWebSocketHandler portfolioWebSocketHandler;

    @Test
    @Order(13)
    void testPortfolioWebSocketHandlerNotification() {
        assertDoesNotThrow(() -> portfolioWebSocketHandler.onPriceUpdated("BTCUSDT"));
        assertDoesNotThrow(() -> portfolioWebSocketHandler.notifyUserPortfolioChanged(testEmail));
    }
}
