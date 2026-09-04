package com.tradeflow.engine;

import com.tradeflow.model.Transaction;
import com.tradeflow.model.TransactionType;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TransactionManager {
    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    public Transaction recordTransaction(String accountId, String orderId, String symbol,
                                         TransactionType type, int quantity, double price, double totalAmount,
                                         double fee, String status) {
        String txId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction tx = new Transaction(txId, accountId, orderId, symbol, type, quantity, price, totalAmount, fee, status);
        transactions.add(tx);
        return tx;
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactions.stream()
                .filter(t -> accountId.equals(t.getAccountId()))
                .collect(Collectors.toList());
    }
}
