package com.letsTrade.demo.order.dto;

import com.letsTrade.demo.order.entity.Order;
import com.letsTrade.demo.order.enums.OrderSide;
import com.letsTrade.demo.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {

    private Long id;
    private String symbol;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderResponse() {
    }

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.symbol = order.getSymbol();
        this.side = order.getSide();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }

    public OrderResponse(Long id, String symbol, OrderSide side, BigDecimal quantity, BigDecimal price, BigDecimal totalAmount, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
