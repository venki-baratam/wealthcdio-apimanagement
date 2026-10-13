package com.natwest.btp.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.natwest.btp.dto.AccountResponse;
import com.natwest.btp.dto.AmountRequest;
import com.natwest.btp.dto.BalanceResponse;
import com.natwest.btp.dto.CreateAccountRequest;
import com.natwest.btp.dto.TransactionResponse;
import com.natwest.btp.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody(required = false) CreateAccountRequest request) {
        BigDecimal openingBalance = request == null || request.openingBalance() == null 
        		? BigDecimal.ZERO
        		: request.openingBalance();
        AccountResponse response =  accountService.createAccount(openingBalance);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }
    
    @PostMapping("/{id}/deposit")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(id, request.amount()));
    }
    
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(id, request.amount()));
    }
    
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getTransactions(id));
    }
}