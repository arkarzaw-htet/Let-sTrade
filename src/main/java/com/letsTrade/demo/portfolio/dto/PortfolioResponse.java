package com.letsTrade.demo.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {

    private BigDecimal totalValueUsdt;
    private List<PortfolioHoldingResponse> holdings;

    public PortfolioResponse() {
    }

    public PortfolioResponse(BigDecimal totalValueUsdt, List<PortfolioHoldingResponse> holdings) {
        this.totalValueUsdt = totalValueUsdt;
        this.holdings = holdings;
    }

    public BigDecimal getTotalValueUsdt() {
        return totalValueUsdt;
    }

    public void setTotalValueUsdt(BigDecimal totalValueUsdt) {
        this.totalValueUsdt = totalValueUsdt;
    }

    public List<PortfolioHoldingResponse> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<PortfolioHoldingResponse> holdings) {
        this.holdings = holdings;
    }
}
