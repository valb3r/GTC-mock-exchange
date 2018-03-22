package com.gtc.tests.controller;

import com.gtc.tests.domain.ExchangeConfig;
import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.dto.TradingCurrency;
import com.gtc.tests.dto.TradingPair;
import com.gtc.tests.service.ConfigRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Valentyn Berezin on 22.03.18.
 */
@RestController
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigRegistry configRegistry;

    @GetMapping("/config/{exchange}/{from}/{to}")
    public ExchangeConfig config(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("from") TradingCurrency from,
            @PathVariable("to") TradingCurrency to) {
        return configRegistry.get(new ExchangeKey(
                exchangeId,
                TradingPair.builder()
                        .from(from)
                        .to(to)
                        .build()
        ));
    }

    @PutMapping("/config/{exchange}/{from}/{to}")
    public void setConfig(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("from") TradingCurrency from,
            @PathVariable("to") TradingCurrency to,
            @Valid @RequestBody ExchangeConfig config) {
        configRegistry.put(
                new ExchangeKey(
                        exchangeId,
                        TradingPair.builder()
                                .from(from)
                                .to(to)
                                .build()
                ),
                config
        );
    }
}
