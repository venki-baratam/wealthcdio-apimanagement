package com.natwest.btp.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(UUID accountId, BigDecimal balance, Instant createdAt) {
}