package com.gtc.tests.dto;

import com.gtc.tests.domain.SubsType;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Data
public class BaseSubscribeDto {

    @NotNull
    private SubsType type;

    @NotEmpty
    private String symbol;

    @NotEmpty
    private String exchangeId;
}
