package com.letsTrade.demo.crypto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cryptocurrencies")
public class Cryptocurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String baseAsset;

    @Column(nullable = false)
    private String quoteAsset;

    public Cryptocurrency() {
    }

    public Cryptocurrency(
            String symbol,
            String name,
            String baseAsset,
            String quoteAsset
    ) {
        this.symbol = symbol;
        this.name = name;
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
// getters and setters
}