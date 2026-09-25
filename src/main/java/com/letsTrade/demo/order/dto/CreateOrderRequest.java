package com.letsTrade.demo.order.dto;

import com.letsTrade.demo.order.enums.OrderSide;

import java.math.BigDecimal;

public class CreateOrderRequest {

    private String symbol;
    private OrderSide side;
    private BigDecimal quantity;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String symbol, OrderSide side, BigDecimal quantity) {
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
