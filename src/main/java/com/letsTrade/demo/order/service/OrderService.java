package com.letsTrade.demo.order.service;

import com.letsTrade.demo.common.exception.InsufficientBalanceException;
import com.letsTrade.demo.common.exception.ResourceNotFoundException;
import com.letsTrade.demo.crypto.entity.Cryptocurrency;
import com.letsTrade.demo.crypto.repository.CryptoRepository;
import com.letsTrade.demo.market.service.MarketService;
import com.letsTrade.demo.order.dto.CreateOrderRequest;
import com.letsTrade.demo.order.dto.OrderResponse;
import com.letsTrade.demo.order.entity.Order;
import com.letsTrade.demo.order.enums.OrderSide;
import com.letsTrade.demo.order.enums.OrderStatus;
import com.letsTrade.demo.order.repository.OrderRepository;
import com.letsTrade.demo.portfolio.websocket.PortfolioWebSocketHandler;
import com.letsTrade.demo.user.entity.User;
import com.letsTrade.demo.user.service.UserService;
import com.letsTrade.demo.wallet.entity.Wallet;
import com.letsTrade.demo.wallet.repository.WalletRepository;
import com.letsTrade.demo.wallet.service.WalletService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CryptoRepository cryptoRepository;
    private final MarketService marketService;
    private final UserService userService;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final PortfolioWebSocketHandler portfolioWebSocketHandler;

    public OrderService(
            OrderRepository orderRepository,
            CryptoRepository cryptoRepository,
            MarketService marketService,
            UserService userService,
            WalletService walletService,
            WalletRepository walletRepository,
            @Lazy PortfolioWebSocketHandler portfolioWebSocketHandler
    ) {
        this.orderRepository = orderRepository;
        this.cryptoRepository = cryptoRepository;
        this.marketService = marketService;
        this.userService = userService;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.portfolioWebSocketHandler = portfolioWebSocketHandler;
    }

    @Transactional
    public OrderResponse createAndExecuteOrder(String email, CreateOrderRequest request) {
        if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        if (request.getSide() == null) {
            throw new IllegalArgumentException("Order side (BUY or SELL) is required");
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        String symbol = request.getSymbol().trim().toUpperCase();
        Cryptocurrency crypto = cryptoRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Cryptocurrency not found with symbol: " + symbol));

        BigDecimal marketPrice = marketService.getCurrentPrice(symbol);
        if (marketPrice == null || marketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Unable to retrieve valid market price for " + symbol);
        }

        BigDecimal quantity = request.getQuantity();
        BigDecimal totalAmount = quantity.multiply(marketPrice).setScale(8, RoundingMode.HALF_UP);

        User user = userService.getUserByEmail(email);
        Wallet wallet = walletService.getOrCreateWalletEntity(email);

        String baseAsset = crypto.getBaseAsset();
        String quoteAsset = crypto.getQuoteAsset();

        if (request.getSide() == OrderSide.BUY) {
            BigDecimal currentQuoteBalance = wallet.getBalanceQuantity(quoteAsset);
            if (currentQuoteBalance.compareTo(totalAmount) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient " + quoteAsset + " balance. Required: " + totalAmount + ", Available: " + currentQuoteBalance
                );
            }
            wallet.deductBalance(quoteAsset, totalAmount);
            wallet.addBalance(baseAsset, quantity);
        } else if (request.getSide() == OrderSide.SELL) {
            BigDecimal currentBaseBalance = wallet.getBalanceQuantity(baseAsset);
            if (currentBaseBalance.compareTo(quantity) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient " + baseAsset + " balance. Required: " + quantity + ", Available: " + currentBaseBalance
                );
            }
            wallet.deductBalance(baseAsset, quantity);
            wallet.addBalance(quoteAsset, totalAmount);
        }

        walletRepository.save(wallet);

        Order order = new Order(
                user,
                crypto.getSymbol(),
                request.getSide(),
                quantity,
                marketPrice,
                totalAmount,
                OrderStatus.FILLED
        );

        Order savedOrder = orderRepository.save(order);
        portfolioWebSocketHandler.notifyUserPortfolioChanged(email);
        return new OrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String email) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(OrderResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return new OrderResponse(order);
    }
}
