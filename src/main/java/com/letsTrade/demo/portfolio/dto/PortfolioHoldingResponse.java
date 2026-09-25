package com.letsTrade.demo.portfolio.dto;

import java.math.BigDecimal;

public class PortfolioHoldingResponse {

    private String asset;
    private BigDecimal quantity;
    private BigDecimal priceUsdt;
    private BigDecimal valueUsdt;

    public PortfolioHoldingResponse() {
    }

    public PortfolioHoldingResponse(String asset, BigDecimal quantity, BigDecimal priceUsdt, BigDecimal valueUsdt) {
        this.asset = asset;
        this.quantity = quantity;
        this.priceUsdt = priceUsdt;
        this.valueUsdt = valueUsdt;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceUsdt() {
        return priceUsdt;
    }

    public void setPriceUsdt(BigDecimal priceUsdt) {
        this.priceUsdt = priceUsdt;
    }

    public BigDecimal getValueUsdt() {
        return valueUsdt;
    }

    public void setValueUsdt(BigDecimal valueUsdt) {
        this.valueUsdt = valueUsdt;
    }
}
