package com.gtc.tests.domain;

import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.gtc.tests.dto.TradingCurrency;
import com.gtc.tests.dto.TradingPair;
import lombok.*;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "version"})
public class Order {

    private static final AtomicLong VERSION_GENERATOR = new AtomicLong();

    public static final SimpleAttribute<Order, String> ID = attribute("id", Order::getId);
    public static final SimpleAttribute<Order, BigDecimal> PRICE = attribute("price", Order::getPrice);
    public static final SimpleAttribute<Order, BigDecimal> AMOUNT = attribute("amount", Order::getAmount);
    public static final SimpleAttribute<Order, String> CLIENT = attribute("client", Order::getClientId);
    public static final SimpleAttribute<Order, Long> TSTMP = attribute("tmstmp", Order::getTimestamp);
    public static final SimpleAttribute<Order, Status> STATUS = attribute("status", Order::getStatus);

    private final long version = VERSION_GENERATOR.getAndIncrement();

    private final String id;
    private final String clientId;
    private final BigDecimal price;
    private final BigDecimal amount;
    private final Status status;
    private final TradingPair pair;
    private final long timestamp = System.currentTimeMillis();

    public enum Status {

        OPEN,
        CANCELLED,
        DONE
    }
}
