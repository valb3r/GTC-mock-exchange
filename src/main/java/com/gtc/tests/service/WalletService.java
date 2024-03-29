package com.gtc.tests.service;

import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.dto.TradingCurrency;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final Map<Key, Map<TradingCurrency, BigDecimal>> byClientByCurrencyWallet = new ConcurrentHashMap<>();

    private final ConfigRegistry configRegistry;

    public void deposit(String exchangeId, String client, TradingCurrency currency, BigDecimal amount) {
        byClientByCurrencyWallet
                .computeIfAbsent(new Key(exchangeId, client), id -> new ConcurrentHashMap<>())
                .compute(currency, (key, avail) -> {
                    log.info("Deposit {}.{}.{} amount {} (avail {})", exchangeId, client, currency, amount, avail);
                    return null == avail ? amount : avail.add(amount);
                });
    }

    public void withdraw(String exchangeId, String client, TradingCurrency currency, BigDecimal amount) {

        byClientByCurrencyWallet
                .get(new Key(exchangeId, client))
                .compute(currency, (key, avail) -> {
                    if (null == avail || amount.compareTo(avail) > 0) {
                        throw new IllegalStateException("Insufficient funds");
                    }

                    log.info("Withdraw {}.{}.{} amount {} (avail {})", exchangeId, client, currency, amount, avail);
                    return avail.subtract(amount);
                });
    }

    public void depositWithFee(ExchangeKey key, String client, TradingCurrency currency, BigDecimal amount) {
        BigDecimal coef = BigDecimal.ONE.subtract(configRegistry.get(key).getTradeFeePct().scaleByPowerOfTen(-2));
        deposit(key.getExchangeId(), client, currency, amount.multiply(coef));
    }

    public Map<TradingCurrency, BigDecimal> balances(String exchangeId, String client) {
        return byClientByCurrencyWallet.get(new Key(exchangeId, client));
    }

    @Data
    private static class Key {

        private final String exchangeId;
        private final String client;
    }
}
