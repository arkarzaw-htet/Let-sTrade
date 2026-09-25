package com.letsTrade.demo.common.config;

import com.letsTrade.demo.crypto.entity.Cryptocurrency;
import com.letsTrade.demo.crypto.repository.CryptoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final CryptoRepository cryptoRepository;

    public DataInitializer(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public void run(String... args) {
        if (cryptoRepository.count() == 0) {
            log.info("Seeding initial cryptocurrency data...");
            List<Cryptocurrency> defaultCryptos = List.of(
                    new Cryptocurrency("BTCUSDT", "Bitcoin", "BTC", "USDT"),
                    new Cryptocurrency("ETHUSDT", "Ethereum", "ETH", "USDT"),
                    new Cryptocurrency("SOLUSDT", "Solana", "SOL", "USDT"),
                    new Cryptocurrency("BNBUSDT", "BNB", "BNB", "USDT"),
                    new Cryptocurrency("DOGEUSDT", "Dogecoin", "DOGE", "USDT"),
                    new Cryptocurrency("ADAUSDT", "Cardano", "ADA", "USDT"),
                    new Cryptocurrency("XRPUSDT", "XRP", "XRP", "USDT")
            );
            cryptoRepository.saveAll(defaultCryptos);
            log.info("Successfully seeded {} cryptocurrencies.", defaultCryptos.size());
        }
    }
}
