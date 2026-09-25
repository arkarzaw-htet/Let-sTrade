package com.letsTrade.demo.crypto.repository;

import com.letsTrade.demo.crypto.entity.Cryptocurrency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoRepository
        extends JpaRepository<Cryptocurrency, Long> {

    Optional<Cryptocurrency> findBySymbol(String symbol);
}
