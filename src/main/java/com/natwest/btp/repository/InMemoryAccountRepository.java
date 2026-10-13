package com.natwest.btp.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.natwest.btp.model.Account;

@Repository
public class InMemoryAccountRepository {

    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();
    
    public Account save(Account account)
    {
    	accounts.put(account.getId(), account);
    	return account;
    }
    
    public Optional<Account> findById(UUID accountId){
    	return Optional.ofNullable(accounts.get(accountId));
    }
}