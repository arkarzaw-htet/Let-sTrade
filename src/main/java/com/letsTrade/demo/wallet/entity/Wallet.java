package com.letsTrade.demo.wallet.entity;

import com.letsTrade.demo.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AssetBalance> balances = new ArrayList<>();

    public Wallet() {
    }

    public Wallet(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<AssetBalance> getBalances() {
        return balances;
    }

    public void setBalances(List<AssetBalance> balances) {
        this.balances = balances;
    }

    public Optional<AssetBalance> findBalance(String asset) {
        String clean = asset.trim().toUpperCase();
        return balances.stream()
                .filter(b -> b.getAsset().equalsIgnoreCase(clean))
                .findFirst();
    }

    public BigDecimal getBalanceQuantity(String asset) {
        return findBalance(asset)
                .map(AssetBalance::getQuantity)
                .orElse(BigDecimal.ZERO);
    }

    public void setOrUpdateBalance(String asset, BigDecimal quantity) {
        String clean = asset.trim().toUpperCase();
        Optional<AssetBalance> existing = findBalance(clean);
        if (existing.isPresent()) {
            existing.get().setQuantity(quantity);
        } else {
            AssetBalance newBalance = new AssetBalance(this, clean, quantity);
            this.balances.add(newBalance);
        }
    }

    public void addBalance(String asset, BigDecimal amount) {
        BigDecimal current = getBalanceQuantity(asset);
        setOrUpdateBalance(asset, current.add(amount));
    }

    public void deductBalance(String asset, BigDecimal amount) {
        BigDecimal current = getBalanceQuantity(asset);
        setOrUpdateBalance(asset, current.subtract(amount));
    }
}
