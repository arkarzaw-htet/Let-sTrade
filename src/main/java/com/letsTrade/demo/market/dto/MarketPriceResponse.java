package com.letsTrade.demo.market.dto;

import java.math.BigDecimal;

public class MarketPriceResponse {
    private String symbol;
    private BigDecimal price;

    public MarketPriceResponse() {
    }

    public MarketPriceResponse(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
