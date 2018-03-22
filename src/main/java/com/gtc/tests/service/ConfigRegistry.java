package com.gtc.tests.service;

import com.gtc.tests.domain.ExchangeConfig;
import com.gtc.tests.domain.ExchangeKey;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Valentyn Berezin on 22.03.18.
 */
@Service
public class ConfigRegistry {

    private final Map<ExchangeKey, ExchangeConfig> config = new ConcurrentHashMap<>();

    public ExchangeConfig get(ExchangeKey key) {
        return config.computeIfAbsent(key, id -> new ExchangeConfig());
    }

    public void put(ExchangeKey key, ExchangeConfig cfg) {
        config.put(key, cfg);
    }
}
