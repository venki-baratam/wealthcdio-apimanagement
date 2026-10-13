package com.natwest.btp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.natwest.btp.dto.TransferRequest;
import com.natwest.btp.dto.TransferResponse;
import com.natwest.btp.service.AccountService;

import jakarta.validation.Valid;

@RestController
public class TransferController {

    private final AccountService accountService;

    public TransferController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(
        		request.sourceAccountId(),
        		request.destinationAccountId(),
        		request.amount()
        		));
    }
}