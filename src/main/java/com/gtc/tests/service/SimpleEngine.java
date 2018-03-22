package com.gtc.tests.service;

import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Service
@RequiredArgsConstructor
public class SimpleEngine {

    private final OrderBook orderBook;
    private final SubsRegistry subscribedTo;
    private final WalletService walletService;

    @Scheduled(fixedDelayString = "${app.schedule.engineProcessMs}")
    public void processOrders() {
        orderBook.bookKeys().forEach(this::processBook);
    }

    private void processBook(ExchangeKey key) {
        orderBook.activeOrders(key).forEach(order -> {
            Map<Order, Order> updates = doSatisfy(key, order, orderBook.matchingOrders(key, order));
            if (!updates.isEmpty()) {
                updates.forEach((old, upd) -> orderBook.updateOrder(key, old, upd));
                orderBook.updateTicker(key, order.getPrice());

                updates.values().forEach(it -> subscribedTo.notifyOrder(key, it.getPrice(), it.getAmount()));
                subscribedTo.notifyTicker(key, orderBook.ticker(key));
            }
        });
    }

    private Map<Order, Order> doSatisfy(ExchangeKey key, Order target, List<Order> satisfiers) {
        if (satisfiers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Order, Order> toUpdate = new HashMap<>();

        BigDecimal amount = target.getAmount();
        for (Order with : satisfiers) {
            BigDecimal toTransfer = satisfyPair(amount, with);
            sendToWallet(key, target, toTransfer);
            sendToWallet(key, with, toTransfer);

            amount = amount.add(toTransfer);
            BigDecimal withAmount = with.getAmount().subtract(toTransfer);
            update(with, withAmount, toUpdate);
            if (BigDecimal.ZERO.equals(amount)) {
                break;
            }
        }

        update(target, amount, toUpdate);
        return toUpdate;
    }

    private BigDecimal satisfyPair(BigDecimal amount, Order with) {
        BigDecimal sat = amount.abs().min(with.getAmount().abs());
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return sat.negate();
        }

        return sat;
    }

    private static void update(Order order, BigDecimal newAmount, Map<Order, Order> updates) {
        Order.Status status = order.getStatus();
        if (BigDecimal.ZERO.compareTo(newAmount) == 0) {
            status = Order.Status.DONE;
        }

        Assert.isNull(
                updates.put(order, order.toBuilder().amount(newAmount).status(status).build()
        ),"Duplicate order update");
    }

    private void sendToWallet(ExchangeKey key, Order order, BigDecimal amount) {
        BigDecimal toProcess = amount.abs();
        // sell BTC in BTC/USD
        if (BigDecimal.ZERO.compareTo(order.getAmount()) > 0) {
            walletService.depositWithFee(key, order.getClientId(), key.getPair().getTo(),
                    toProcess.multiply(order.getPrice()));
            return;
        }
        // buy BTC in BTC/USD
        walletService.depositWithFee(key, order.getClientId(), key.getPair().getFrom(), toProcess);
    }
}
