package com.gtc.tests.controller;

import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.domain.Order;
import com.gtc.tests.dto.TradingCurrency;
import com.gtc.tests.dto.TradingPair;
import com.gtc.tests.service.OrderBook;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@RestController
@RequiredArgsConstructor
public class TradeInfoController {

    private final OrderBook orderBook;

    @GetMapping("/trade/{exchange}/{client}/orders")
    public List<Order> getAll(
            @PathVariable(("exchange")) String exchangeId,
            @PathVariable("client") String client,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        return all ?
                orderBook.allClientOrders(exchangeId, client)
                : orderBook.activeClientOrders(exchangeId, client);
    }

    @GetMapping("/trade/{exchange}/{client}/orders/{id}")
    public Order getById(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("client") String client,
            @PathVariable("id") String id) {
        return orderBook.byId(exchangeId, client, id)
                .orElseThrow(() -> new IllegalStateException("No trade"));
    }

    @GetMapping("/trade/{exchange}/pair/{from}/{to}")
    public List<Order> getOrdersOnExchange(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("from") TradingCurrency from,
            @PathVariable("to") TradingCurrency to,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        ExchangeKey key = new ExchangeKey(exchangeId, TradingPair.builder().from(from).to(to).build());
        return all ? orderBook.allOrders(key) : orderBook.activeOrders(key);
    }
}
