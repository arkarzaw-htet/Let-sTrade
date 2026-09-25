package com.letsTrade.demo.wallet.repository;

import com.letsTrade.demo.wallet.entity.AssetBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetBalanceRepository extends JpaRepository<AssetBalance, Long> {
    Optional<AssetBalance> findByWalletIdAndAsset(Long walletId, String asset);
}
