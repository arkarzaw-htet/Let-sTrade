package com.letsTrade.demo.wallet.service;

import com.letsTrade.demo.common.exception.ResourceNotFoundException;
import com.letsTrade.demo.portfolio.websocket.PortfolioWebSocketHandler;
import com.letsTrade.demo.user.entity.User;
import com.letsTrade.demo.user.repository.UserRepository;
import com.letsTrade.demo.wallet.dto.AssetBalanceResponse;
import com.letsTrade.demo.wallet.dto.DepositRequest;
import com.letsTrade.demo.wallet.dto.WalletResponse;
import com.letsTrade.demo.wallet.entity.Wallet;
import com.letsTrade.demo.wallet.repository.WalletRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PortfolioWebSocketHandler portfolioWebSocketHandler;

    public WalletService(
            WalletRepository walletRepository,
            UserRepository userRepository,
            @Lazy PortfolioWebSocketHandler portfolioWebSocketHandler
    ) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.portfolioWebSocketHandler = portfolioWebSocketHandler;
    }

    @Transactional
    public Wallet createInitialWallet(User user) {
        Wallet wallet = new Wallet(user);
        wallet.setOrUpdateBalance("USDT", new BigDecimal("10000.00"));
        wallet.setOrUpdateBalance("BTC", BigDecimal.ZERO);
        wallet.setOrUpdateBalance("ETH", BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet getOrCreateWalletEntity(String email) {
        return walletRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
                    return createInitialWallet(user);
                });
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletByEmail(String email) {
        Wallet wallet = getOrCreateWalletEntity(email);
        return toWalletResponse(wallet);
    }

    @Transactional
    public WalletResponse deposit(String email, DepositRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        if (request.getAsset() == null || request.getAsset().trim().isEmpty()) {
            throw new IllegalArgumentException("Asset symbol is required");
        }

        String asset = request.getAsset().trim().toUpperCase();
        Wallet wallet = getOrCreateWalletEntity(email);
        wallet.addBalance(asset, request.getAmount());
        Wallet saved = walletRepository.save(wallet);

        // Notify real-time portfolio websocket
        portfolioWebSocketHandler.notifyUserPortfolioChanged(email);

        return toWalletResponse(saved);
    }

    public WalletResponse toWalletResponse(Wallet wallet) {
        List<AssetBalanceResponse> assetResponses = wallet.getBalances().stream()
                .map(b -> new AssetBalanceResponse(b.getAsset(), b.getQuantity()))
                .toList();
        return new WalletResponse(assetResponses);
    }
}
