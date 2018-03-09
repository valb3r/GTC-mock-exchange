package com.gtc.tests.service;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.TransactionalIndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Service
@RequiredArgsConstructor
public class OrderBook {

    private final Map<ExchangeKey, IndexedCollection<Order>> books = new ConcurrentHashMap<>();
    private final Map<ExchangeKey, AtomicReference<BigDecimal>> tickers = new ConcurrentHashMap<>();

    public Order addOrder(ExchangeKey key, Order order) {
        books.computeIfAbsent(key, id -> buildOrderCollection()).add(order);
        return order;
    }

    public Order cancelOrder(String exchangeId, String client, String id) {
        IndexedCollection<Order> orders = orders(exchangeId);

        Order order;
        Order updated;
        try (ResultSet<Order> res = orders.retrieve(and(
                not(equal(Order.STATUS, Order.Status.CANCELLED)),
                equal(Order.ID, id),
                equal(Order.CLIENT, client)))) {
            order = res.uniqueResult();
            updated = order.toBuilder().status(Order.Status.CANCELLED).build();
        }

        orders.remove(order);
        orders.add(updated);
        return updated;
    }

    public void updateOrder(ExchangeKey key, Order oldVer, Order newVer) {
        IndexedCollection<Order> orders = books.get(key);
        if (null == orders) {
            return;
        }

        orders.remove(oldVer);
        orders.add(newVer);
    }

    public void updateTicker(ExchangeKey key, BigDecimal value) {
        tickers.computeIfAbsent(key, id -> new AtomicReference<>(new BigDecimal(0))).set(value);
    }

    public BigDecimal ticker(ExchangeKey key) {
        return tickers.get(key).get();
    }

    public List<Order> activeOrders(ExchangeKey key) {
        return executeQuery(
                key,
                and(
                        equal(Order.STATUS, Order.Status.OPEN),
                        not(equal(Order.AMOUNT, BigDecimal.ZERO))
                ),
                queryOptions(orderBy(ascending(Order.TSTMP)))
        );
    }

    public List<Order> matchingOrders(ExchangeKey key, Order order) {
        boolean isSell = BigDecimal.ZERO.compareTo(order.getAmount()) > 0;
        return executeQuery(
                key,
                and(
                        isSell ? greaterThan(Order.AMOUNT, BigDecimal.ZERO) : lessThan(Order.AMOUNT, BigDecimal.ZERO),
                        isSell ? greaterThan(Order.PRICE, order.getPrice()) : lessThan(Order.PRICE, order.getPrice()),
                        not(equal(Order.AMOUNT, BigDecimal.ZERO)),
                        equal(Order.STATUS, Order.Status.OPEN)
                ),
                queryOptions(orderBy(ascending(Order.TSTMP)))
        );
    }

    public Optional<Order> byId(String exchangeId, String client, String id) {
        try (ResultSet<Order> res = orders(exchangeId).retrieve(
                and(equal(Order.ID, id), equal(Order.CLIENT, client)))) {
            return Optional.ofNullable(res.uniqueResult());
        }
    }

    public List<Order> activeClientOrders(String exchangeId, String client) {
        try (ResultSet<Order> res = orders(exchangeId).retrieve(
                and(
                        equal(Order.CLIENT, client),
                        not(equal(Order.AMOUNT, BigDecimal.ZERO)),
                        equal(Order.STATUS, Order.Status.OPEN)
                ), queryOptions(orderBy(descending(Order.TSTMP))))) {
            return StreamSupport.stream(res.spliterator(), false).collect(Collectors.toList());
        }
    }

    public List<Order> allClientOrders(String exchangeId, String client) {
        try (ResultSet<Order> res = orders(exchangeId).retrieve(
                equal(Order.CLIENT, client),
                queryOptions(orderBy(descending(Order.TSTMP))))) {
            return StreamSupport.stream(res.spliterator(), false).collect(Collectors.toList());
        }
    }


    public Set<ExchangeKey> bookKeys() {
        return books.keySet();
    }

    private IndexedCollection<Order> orders(String exchangeId) {
        return books.entrySet().stream()
                .filter(it -> it.getKey().getExchangeId().equals(exchangeId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown exchange"));
    }

    private List<Order> executeQuery(ExchangeKey key, Query<Order> query, QueryOptions options) {
        IndexedCollection<Order> orders = books.get(key);
        if (null == orders) {
            return Collections.emptyList();
        }


        try (ResultSet<Order> res = orders.retrieve(query, options)) {
            return StreamSupport.stream(res.spliterator(), false).collect(Collectors.toList());
        }
    }

    private IndexedCollection<Order> buildOrderCollection() {
        IndexedCollection<Order> newColl = new TransactionalIndexedCollection<>(Order.class);
        newColl.addIndex(UniqueIndex.onAttribute((Order.ID)));
        newColl.addIndex(NavigableIndex.onAttribute((Order.PRICE)));
        newColl.addIndex(NavigableIndex.onAttribute((Order.AMOUNT)));
        newColl.addIndex(HashIndex.onAttribute((Order.CLIENT)));
        newColl.addIndex(NavigableIndex.onAttribute((Order.TSTMP)));

        return newColl;
    }
}
