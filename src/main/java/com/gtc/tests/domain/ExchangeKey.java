package com.gtc.tests.domain;

import com.gtc.tests.dto.TradingPair;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class ExchangeKey {

    private final String exchangeId;
    private final TradingPair pair;
}
