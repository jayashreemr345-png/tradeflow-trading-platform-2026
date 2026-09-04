package com.tradeflow.engine;

import com.tradeflow.model.TraderAccount;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager {
    private final Map<String, TraderAccount> accounts = new ConcurrentHashMap<>();

    public TraderAccount createAccount(String accountId, String traderName, double initialDeposit) {
        TraderAccount account = new TraderAccount(accountId, traderName, initialDeposit);
        accounts.put(accountId, account);
        return account;
    }

    public TraderAccount getAccount(String accountId) {
        return accounts.get(accountId);
    }
}
