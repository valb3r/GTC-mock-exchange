package com.gtc.tests.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static com.gtc.tests.service.Const.MIN;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Data
public class DepositDto {

    @NotNull
    private TradingCurrency currency;

    @DecimalMin(MIN)
    private BigDecimal amount;
}
