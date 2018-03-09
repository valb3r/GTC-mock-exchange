package com.gtc.tests.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static com.gtc.tests.service.Const.MAX_DECIMAL;
import static com.gtc.tests.service.Const.MIN;
import static com.gtc.tests.service.Const.PRECISION;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Data
public class TradeDto {

    @NotEmpty
    private String id;

    @NotNull
    private TradingPair pair;

    private boolean isSell;

    @DecimalMin(MIN)
    @Digits(integer = MAX_DECIMAL, fraction = PRECISION)
    private BigDecimal amount;

    @DecimalMin(MIN)
    @Digits(integer = MAX_DECIMAL, fraction = PRECISION)
    private BigDecimal price;
}
