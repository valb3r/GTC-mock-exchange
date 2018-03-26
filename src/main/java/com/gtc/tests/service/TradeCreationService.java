package com.gtc.tests.service;

import com.gtc.tests.domain.ExchangeConfig;
import com.gtc.tests.domain.ExchangeKey;
import com.gtc.tests.domain.Order;
import com.gtc.tests.dto.TradeDto;
import com.gtc.tests.dto.TradingCurrency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@Service
@RequiredArgsConstructor
public class TradeCreationService {

    private final WalletService walletService;
    private final OrderBook orderBook;
    private final SubsRegistry subscribers;
    private final ConfigRegistry configRegistry;

    public Order createTrade(String exchangeId, String client, TradeDto trade) {
        TradingCurrency currency = trade.isSell() ? trade.getPair().getFrom() : trade.getPair().getTo();

        BigDecimal amount = getAmount(trade);

        ExchangeKey key = new ExchangeKey(exchangeId, trade.getPair());

        checkTrade(configRegistry.get(key), trade);

        Order result = orderBook.addOrder(
                key,
                new Order(
                        trade.getId(),
                        client,
                        trade.getPrice(),
                        trade.isSell() ? trade.getAmount().negate() : trade.getAmount(),
                        trade.isSell(),
                        Order.Status.OPEN,
                        trade.getPair()
                ));

        walletService.withdraw(exchangeId, client, currency, amount);

        subscribers.notifyOrder(key, result.getPrice(), result.getAmount(), result.isSell());
        return result;
    }

    public Order cancelTrade(String exchangeId, String client, String id) {
        Order cancelled = orderBook.cancelOrder(exchangeId, client, id);
        BigDecimal amount = getAmount(cancelled);

        TradingCurrency redeem = cancelled.getPair().getTo();
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            redeem = cancelled.getPair().getFrom();
        }
        
        walletService.deposit(exchangeId, client, redeem, amount.abs());

        return cancelled;
    }

    private static BigDecimal getAmount(TradeDto trade) {
        return trade.isSell() ? trade.getAmount() :
                trade.getAmount().multiply(trade.getPrice());
    }

    private static BigDecimal getAmount(Order order) {
        return order.getAmount().compareTo(BigDecimal.ZERO) < 0 ? order.getAmount() :
                order.getAmount().multiply(order.getPrice());
    }

    private void checkTrade(ExchangeConfig config, TradeDto tradeDto) {
        Assert.isTrue(tradeDto.getPrice().compareTo(config.getMinPrice()) >= 0, "Price too small");
        Assert.isTrue(tradeDto.getAmount().compareTo(config.getMinAmount()) >= 0, "Amount too small");
        Assert.isTrue(
                0 == tradeDto.getPrice().remainder(config.getPricePrec(), MathContext.DECIMAL64).compareTo(BigDecimal.ZERO),
                "Price precision too high"
        );
        Assert.isTrue(
                0 == tradeDto.getAmount().remainder(config.getAmountPrec(), MathContext.DECIMAL64).compareTo(BigDecimal.ZERO),
                "Amount precision too high"
        );
    }
}
