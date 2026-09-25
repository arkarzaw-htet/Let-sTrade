package com.letsTrade.demo.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "asset_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "asset"})
)
public class AssetBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private String asset;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal quantity;

    public AssetBalance() {
    }

    public AssetBalance(Wallet wallet, String asset, BigDecimal quantity) {
        this.wallet = wallet;
        this.asset = asset.toUpperCase();
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset != null ? asset.toUpperCase() : null;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
