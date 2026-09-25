package com.letsTrade.demo.portfolio.service;

import com.letsTrade.demo.market.service.MarketService;
import com.letsTrade.demo.portfolio.dto.PortfolioHoldingResponse;
import com.letsTrade.demo.portfolio.dto.PortfolioResponse;
import com.letsTrade.demo.wallet.entity.AssetBalance;
import com.letsTrade.demo.wallet.entity.Wallet;
import com.letsTrade.demo.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final WalletService walletService;
    private final MarketService marketService;

    public PortfolioService(WalletService walletService, MarketService marketService) {
        this.walletService = walletService;
        this.marketService = marketService;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(String email) {
        Wallet wallet = walletService.getOrCreateWalletEntity(email);

        BigDecimal totalValueUsdt = BigDecimal.ZERO;
        List<PortfolioHoldingResponse> holdings = new ArrayList<>();

        for (AssetBalance balance : wallet.getBalances()) {
            String asset = balance.getAsset().toUpperCase();
            BigDecimal quantity = balance.getQuantity();

            BigDecimal priceUsdt;
            if ("USDT".equalsIgnoreCase(asset)) {
                priceUsdt = BigDecimal.ONE;
            } else {
                try {
                    priceUsdt = marketService.getCurrentPrice(asset + "USDT");
                } catch (Exception e) {
                    log.warn("Could not fetch price for {}USDT: {}", asset, e.getMessage());
                    priceUsdt = BigDecimal.ZERO;
                }
            }

            BigDecimal valueUsdt = quantity.multiply(priceUsdt).setScale(2, RoundingMode.HALF_UP);
            totalValueUsdt = totalValueUsdt.add(valueUsdt);

            holdings.add(new PortfolioHoldingResponse(
                    asset,
                    quantity,
                    priceUsdt,
                    valueUsdt
            ));
        }

        return new PortfolioResponse(totalValueUsdt.setScale(2, RoundingMode.HALF_UP), holdings);
    }
}
