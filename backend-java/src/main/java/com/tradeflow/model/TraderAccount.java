package com.tradeflow.model;

public class TraderAccount {
    private final String accountId;
    private final String traderName;
    private double cashBalance;
    private final Portfolio portfolio;

    public TraderAccount(String accountId, String traderName, double initialDeposit) {
        this.accountId = accountId;
        this.traderName = traderName;
        this.cashBalance = initialDeposit;
        this.portfolio = new Portfolio(accountId);
    }

    public synchronized boolean debitCash(double amount) {
        if (amount > cashBalance) {
            return false;
        }
        cashBalance -= amount;
        return true;
    }

    public synchronized void creditCash(double amount) {
        cashBalance += amount;
    }

    public String getAccountId() { return accountId; }
    public String getTraderName() { return traderName; }
    public synchronized double getCashBalance() { return cashBalance; }
    public Portfolio getPortfolio() { return portfolio; }
}
