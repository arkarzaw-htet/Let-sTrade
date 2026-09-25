package com.letsTrade.demo.portfolio.controller;

import com.letsTrade.demo.portfolio.dto.PortfolioResponse;
import com.letsTrade.demo.portfolio.service.PortfolioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public PortfolioResponse getPortfolio(Authentication authentication) {
        String email = authentication.getName();
        return portfolioService.getPortfolio(email);
    }
}
