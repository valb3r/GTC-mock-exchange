package com.gtc.tests.service;

import com.gtc.tests.dto.TradingCurrency;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Service
public class WalletService {

    private final Map<Key, Map<TradingCurrency, BigDecimal>> byClientByCurrencyWallet = new ConcurrentHashMap<>();

    public void deposit(String exchangeId, String client, TradingCurrency currency, BigDecimal amount) {
        byClientByCurrencyWallet
                .computeIfAbsent(new Key(exchangeId, client), id -> new ConcurrentHashMap<>())
                .compute(currency, (key, avail) ->
                    null == avail ? amount : avail.add(amount)
                );
    }

    public void withdraw(String exchangeId, String client, TradingCurrency currency, BigDecimal amount) {

        byClientByCurrencyWallet
                .get(new Key(exchangeId, client))
                .compute(currency, (key, avail) -> {
                    if (null == avail || amount.compareTo(avail) > 0) {
                        throw new IllegalStateException("Insufficient funds");
                    }

                    return avail.subtract(amount);
                });
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
