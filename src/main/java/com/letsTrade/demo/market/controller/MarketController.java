package com.letsTrade.demo.market.controller;

import com.letsTrade.demo.market.dto.MarketPriceResponse;
import com.letsTrade.demo.market.dto.MarketStatsResponse;
import com.letsTrade.demo.market.service.MarketService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/{symbol}/price")
    public MarketPriceResponse getPrice(@PathVariable String symbol) {
        return marketService.getPrice(symbol);
    }

    @GetMapping("/{symbol}/stats")
    public MarketStatsResponse getStats(@PathVariable String symbol) {
        return marketService.getStats(symbol);
    }
}
