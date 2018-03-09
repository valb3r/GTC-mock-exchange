package com.gtc.tests.controller;

import com.gtc.tests.domain.Order;
import com.gtc.tests.dto.TradeDto;
import com.gtc.tests.service.TradeCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@RestController
@RequiredArgsConstructor
public class TradeCreateDeleteController {

    private final TradeCreationService tradeSvc;

    @PutMapping("/trade/{exchange}/{client}")
    public Order createTrade(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("client") String client,
            @RequestBody @Valid TradeDto trade) {
        return tradeSvc.createTrade(exchangeId, client, trade);
    }

    @DeleteMapping("/trade/{exchange}/{client}/{id}")
    public Order cancelTrade(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("client") String client,
            @PathVariable("id") String tradeId) {
        return tradeSvc.cancelTrade(exchangeId, client, tradeId);
    }
}
