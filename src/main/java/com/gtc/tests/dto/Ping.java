package com.gtc.tests.dto;

import lombok.Data;

/**
 * Created by Valentyn Berezin on 09.03.18.
 */
@Data
public class Ping {

    private long ping = System.currentTimeMillis();
}
