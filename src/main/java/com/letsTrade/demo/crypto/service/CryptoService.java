package com.letsTrade.demo.crypto.service;

import com.letsTrade.demo.common.exception.ResourceNotFoundException;
import com.letsTrade.demo.crypto.dto.CryptoResponse;
import com.letsTrade.demo.crypto.entity.Cryptocurrency;
import com.letsTrade.demo.crypto.repository.CryptoRepository;
import com.letsTrade.demo.market.dto.MarketStatsResponse;
import com.letsTrade.demo.market.service.MarketService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoService {

    private final CryptoRepository cryptoRepository;
    private final MarketService marketService;

    public CryptoService(CryptoRepository cryptoRepository, MarketService marketService) {
        this.cryptoRepository = cryptoRepository;
        this.marketService = marketService;
    }

    public List<CryptoResponse> getAllCryptos() {
        return cryptoRepository.findAll()
                .stream()
                .map(crypto -> new CryptoResponse(
                        crypto.getSymbol(),
                        crypto.getName(),
                        crypto.getBaseAsset(),
                        crypto.getQuoteAsset()
                ))
                .toList();
    }

    public CryptoResponse getCryptoBySymbol(String symbol) {
        String cleanSymbol = symbol.trim().toUpperCase();
        Cryptocurrency crypto = cryptoRepository.findBySymbol(cleanSymbol)
                .orElseThrow(() -> new ResourceNotFoundException("Cryptocurrency not found: " + cleanSymbol));

        MarketStatsResponse stats = marketService.getStats(cleanSymbol);
        return new CryptoResponse(
                crypto.getSymbol(),
                crypto.getName(),
                crypto.getBaseAsset(),
                crypto.getQuoteAsset(),
                stats.getPrice(),
                stats.getPriceChangePercent()
        );
    }
}