package com.natwest.btp.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(

		UUID transactionId,
		UUID accountId,
		TransactionType type,
		BigDecimal amount,
		BigDecimal balanceAfter,
		UUID counterpartyAccountId,
		String description,
		Instant timestamp
){
}