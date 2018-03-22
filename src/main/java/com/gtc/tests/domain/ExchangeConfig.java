package com.gtc.tests.domain;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by Valentyn Berezin on 22.03.18.
 */
@Data
public class ExchangeConfig {

    private static final String VERY_PRECISE =
            "0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";

    @NotNull
    @DecimalMin("0")
    private BigDecimal tradeFeePct = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0")
    private BigDecimal minAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0")
    private BigDecimal minPrice = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(VERY_PRECISE)
    private BigDecimal amountPrec = BigDecimal.ONE.scaleByPowerOfTen(-60);

    @NotNull
    @DecimalMin(VERY_PRECISE)
    private BigDecimal pricePrec = BigDecimal.ONE.scaleByPowerOfTen(-60);
}
