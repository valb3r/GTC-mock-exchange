package com.gtc.tests.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Data
public class BookDto {

    private final Kind kind;
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal amount;

    public BookDto(boolean isSell, String symbol, BigDecimal price, BigDecimal amount) {
        this.kind = isSell ? Kind.SELL : Kind.BUY;
        this.symbol = symbol;
        this.price = price;
        this.amount = amount;
    }

    public enum Kind {
        SELL,
        BUY
    }
}
