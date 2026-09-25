package com.letsTrade.demo.wallet.dto;

import java.math.BigDecimal;

public class DepositRequest {

    private String asset;
    private BigDecimal amount;

    public DepositRequest() {
    }

    public DepositRequest(String asset, BigDecimal amount) {
        this.asset = asset;
        this.amount = amount;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
