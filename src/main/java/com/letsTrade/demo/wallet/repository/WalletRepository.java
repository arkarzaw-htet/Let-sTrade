package com.letsTrade.demo.wallet.repository;

import com.letsTrade.demo.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    @Query("SELECT w FROM Wallet w JOIN w.user u WHERE u.email = :email")
    Optional<Wallet> findByUserEmail(@Param("email") String email);
}
