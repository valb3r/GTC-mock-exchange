package com.gtc.tests.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Data
public class TickerDto {

    private final String symbol;
    private final BigDecimal price;
}
