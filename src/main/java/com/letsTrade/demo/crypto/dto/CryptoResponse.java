package com.letsTrade.demo.crypto.dto;

import java.math.BigDecimal;

public class CryptoResponse {

    private String symbol;
    private String name;
    private String baseAsset;
    private String quoteAsset;
    private BigDecimal price;
    private BigDecimal priceChange24h;

    public CryptoResponse() {
    }

    public CryptoResponse(String symbol, String name, String baseAsset, String quoteAsset) {
        this.symbol = symbol;
        this.name = name;
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
    }

    public CryptoResponse(String symbol, String name, String baseAsset, String quoteAsset, BigDecimal price, BigDecimal priceChange24h) {
        this.symbol = symbol;
        this.name = name;
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        this.price = price;
        this.priceChange24h = priceChange24h;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseAsset() {
        return baseAsset;
    }

    public void setBaseAsset(String baseAsset) {
        this.baseAsset = baseAsset;
    }

    public String getQuoteAsset() {
        return quoteAsset;
    }

    public void setQuoteAsset(String quoteAsset) {
        this.quoteAsset = quoteAsset;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceChange24h() {
        return priceChange24h;
    }

    public void setPriceChange24h(BigDecimal priceChange24h) {
        this.priceChange24h = priceChange24h;
    }
}