package com.natwest.btp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.natwest.btp.model.Transaction;

public record TransactionResponse(
		UUID transactionId,
		UUID accountId,
		String type,
		BigDecimal amount,
		BigDecimal balanceAfter,
		UUID counterpartyAccountId,
		String description,
		Instant timestamp
) {
	public TransactionResponse(Transaction transaction) {
		this(
				transaction.transactionId(),
				transaction.accountId(),
				transaction.type().name(),
				transaction.amount(),
				transaction.balanceAfter(),
				transaction.counterpartyAccountId(),
				transaction.description(),
				transaction.timestamp()
		);
	}
}