package com.natwest.btp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record AmountRequest(
		@NotNull(message = "Amount is required.")
		@DecimalMin(value = "0.01", inclusive = true, message = "Amount must be grater then zero.")
		@Digits(integer = 17, fraction = 2, message = "Amount must have at most two decimal places.")
		BigDecimal amount
) {
}