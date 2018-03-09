package com.gtc.tests.controller;

import com.gtc.tests.dto.DepositDto;
import com.gtc.tests.dto.TradingCurrency;
import com.gtc.tests.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final WalletService walletService;

    @GetMapping("/account/{exchange}/{client}")
    public Map<TradingCurrency, BigDecimal> balances(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("client") String client) {
        return walletService.balances(exchangeId, client);
    }

    @PutMapping("/account/{exchange}/{client}")
    public void deposit(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("client") String client,
            @RequestBody @Valid DepositDto deposit) {
        walletService.deposit(exchangeId, client, deposit.getCurrency(), deposit.getAmount());
    }

    @PutMapping("/account/{exchange}/{client}/withdraw")
    public void withdraw(
            @PathVariable("exchange") String exchangeId,
            @PathVariable("client") String client,
            @RequestBody @Valid DepositDto deposit) {
        walletService.withdraw(exchangeId, client, deposit.getCurrency(), deposit.getAmount());
    }
}
