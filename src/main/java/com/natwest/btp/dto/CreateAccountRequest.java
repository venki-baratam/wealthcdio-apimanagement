package com.natwest.btp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public record CreateAccountRequest(
		@DecimalMin(value = "0.00", inclusive = true, message = "Opening balance cannot be nagative.")
		@Digits(integer = 17, fraction = 2, message = "Opening balance must have at most two decimal places.")
		BigDecimal openingBalance
) {
}