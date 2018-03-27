package com.gtc.tests.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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

    @NotNull
    private BigDecimal amount;

    @NotNull
    private BigDecimal price;
}
