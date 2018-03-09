package com.gtc.tests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.dto.BaseSubscribeDto;
import com.gtc.tests.dto.TradingCurrency;
import com.gtc.tests.dto.TradingPair;
import com.gtc.tests.service.SubsRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.validation.Validator;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubsController extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final Validator validator;
    private final SubsRegistry registry;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        BaseSubscribeDto subs = mapper.readValue(message.asBytes(), BaseSubscribeDto.class);
        if (!validator.validate(subs).isEmpty()) {
            throw new IllegalArgumentException("Invalid subs");
        }

        String[] symbols = subs.getSymbol().split("_");
        registry.subscribe(
                session,
                subs.getType(),
                new ExchangeKey(
                        subs.getExchangeId(),
                        TradingPair.builder()
                                .from(TradingCurrency.fromCode(symbols[0]))
                                .to(TradingCurrency.fromCode(symbols[1]))
                                .build()
                )
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        registry.unsubscribe(session);
    }
}
