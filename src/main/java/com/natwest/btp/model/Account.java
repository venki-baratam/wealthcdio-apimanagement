package com.natwest.btp.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final Instant createdAt;
    private BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();    

    public Account(UUID id, BigDecimal openingBalance ,Instant createdAt) {
        this.id = id;
        this.balance = openingBalance;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
    	this.balance = balance;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void applyTransaction(Transaction transaction) {
    	transactions.add(transaction);
    }
    
    public List<Transaction> gettransactions() {
    	return List.copyOf(transactions);
    }
    
    
}