package com.letsTrade.demo.wallet.controller;

import com.letsTrade.demo.wallet.dto.DepositRequest;
import com.letsTrade.demo.wallet.dto.WalletResponse;
import com.letsTrade.demo.wallet.service.WalletService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse getWallet(Authentication authentication) {
        String email = authentication.getName();
        return walletService.getWalletByEmail(email);
    }

    @PostMapping("/deposit")
    public WalletResponse deposit(
            Authentication authentication,
            @RequestBody DepositRequest request
    ) {
        String email = authentication.getName();
        return walletService.deposit(email, request);
    }
}
