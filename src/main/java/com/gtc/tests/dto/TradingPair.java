package com.gtc.tests.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Data
@Builder
public class TradingPair {

    private TradingCurrency from;
    private TradingCurrency to;

    @Override
    public String toString() {
        return from.getCode() + "_" + to.getCode();
    }
}
