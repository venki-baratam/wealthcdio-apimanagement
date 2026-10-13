package com.natwest.btp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
		UUID transactionId,
		TransactionResponse debit,
		TransactionResponse credit,
		BigDecimal amount,
		Instant timestamp
) {
}