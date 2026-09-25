package com.letsTrade.demo.wallet.dto;

import java.util.List;

public class WalletResponse {

    private List<AssetBalanceResponse> assets;

    public WalletResponse() {
    }

    public WalletResponse(List<AssetBalanceResponse> assets) {
        this.assets = assets;
    }

    public List<AssetBalanceResponse> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetBalanceResponse> assets) {
        this.assets = assets;
    }
}
