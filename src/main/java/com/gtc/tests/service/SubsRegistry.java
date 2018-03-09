package com.gtc.tests.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.domain.SubsType;
import com.gtc.tests.dto.BookDto;
import com.gtc.tests.dto.Ping;
import com.gtc.tests.dto.TickerDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Valentyn Berezin on 09.03.18.
 */
@Service
@RequiredArgsConstructor
public class SubsRegistry {

    private final OrderBook orderBook;
    private final ObjectMapper mapper;

    private final Map<ExchangeKey, Map<SubsType, Set<Subs>>> keyToSessionId = new ConcurrentHashMap<>();

    public void subscribe(WebSocketSession session, SubsType subsType, ExchangeKey key) {
        keyToSessionId
                .computeIfAbsent(key, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(subsType, id -> ConcurrentHashMap.newKeySet())
                .add(new Subs(session.getId(), session));

        orderBook.activeOrders(key).forEach(it -> notifyOrder(key, it.getPrice(), it.getAmount()));
    }

    public void unsubscribe(WebSocketSession session) {
        keyToSessionId.forEach((key, subsMap) ->
                subsMap.forEach((type, subs) -> subs.remove(new Subs(session.getId(), session)))
        );
    }

    public void notifyTicker(ExchangeKey key, BigDecimal ticker) {
        notify(key, SubsType.TICKER, new TickerDto(key.getPair().toString(), ticker));
    }

    public void notifyOrder(ExchangeKey key, BigDecimal price, BigDecimal amount) {
        notify(key, SubsType.BOOK, new BookDto(key.getPair().toString(), price, amount));
    }

    @Scheduled(fixedRateString = "${app.schedule.pingMs}")
    public void ping() {
        keyToSessionId.entrySet().stream()
                .flatMap(it -> it.getValue().entrySet().stream())
                .flatMap(it -> it.getValue().stream())
                .distinct()
                .forEach(subs ->
                        doSend(subs.getSocketSession(), new Ping())
                );
    }

    private void notify(ExchangeKey key, SubsType subsType, Object payload) {
        keyToSessionId
                .getOrDefault(key, Collections.emptyMap())
                .getOrDefault(subsType, Collections.emptySet())
                .forEach(subs ->
                        doSend(subs.getSocketSession(), payload)
                );
    }

    @SneakyThrows
    private void doSend(WebSocketSession session, Object payload) {
        if (!session.isOpen()) {
            return;
        }

        session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
    }

    @Data
    @EqualsAndHashCode(of = "id")
    private static class Subs {

        private final String id;
        private final WebSocketSession socketSession;
    }
}
