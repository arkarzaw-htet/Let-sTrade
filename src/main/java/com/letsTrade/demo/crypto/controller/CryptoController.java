package com.letsTrade.demo.crypto.controller;

import com.letsTrade.demo.crypto.dto.CryptoResponse;
import com.letsTrade.demo.crypto.service.CryptoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/cryptocurrencies", "/api/cryptos"})
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping
    public List<CryptoResponse> getAllCryptos() {
        return cryptoService.getAllCryptos();
    }

    @GetMapping("/{symbol}")
    public CryptoResponse getCryptoBySymbol(@PathVariable String symbol) {
        return cryptoService.getCryptoBySymbol(symbol);
    }
}