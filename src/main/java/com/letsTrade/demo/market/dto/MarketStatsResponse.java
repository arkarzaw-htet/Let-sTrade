package com.letsTrade.demo.market.dto;

import java.math.BigDecimal;

public class MarketStatsResponse {
    private String symbol;
    private BigDecimal price;
    private BigDecimal priceChangePercent;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;

    public MarketStatsResponse() {
    }

    public MarketStatsResponse(String symbol, BigDecimal price, BigDecimal priceChangePercent, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal volume) {
        this.symbol = symbol;
        this.price = price;
        this.priceChangePercent = priceChangePercent;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
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

    public BigDecimal getPriceChangePercent() {
        return priceChangePercent;
    }

    public void setPriceChangePercent(BigDecimal priceChangePercent) {
        this.priceChangePercent = priceChangePercent;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }
}
