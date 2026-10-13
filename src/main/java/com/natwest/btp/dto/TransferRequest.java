package com.natwest.btp.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
		@NotNull(message = "sourceAccountId is required.") UUID sourceAccountId,
		@NotNull(message = "destinationAccountId is required.") UUID destinationAccountId,
		@NotNull(message = "Amount is required.") 
		@DecimalMin(value = "0.01", inclusive = true, message = "Amount must be grater then zero.")
		@Digits(integer = 17, fraction = 2, message = "Amount must have at most two decimal places.")
		BigDecimal amount
) {
}