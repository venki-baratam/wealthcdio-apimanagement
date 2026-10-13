package com.natwest.btp.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BinaryOperator;

public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        return new Money(amount.setScale(SCALE, RoundingMode.HALF_UP));
    }

    public static Money zero() {
        return Money.of(BigDecimal.ZERO);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Money add(Money other) {
        return combine(other, BigDecimal::add);
    }

    public Money subtract(Money other) {
        return combine(other, BigDecimal::subtract);
    }

    private Money combine(Money other, BinaryOperator<BigDecimal> operation) {
        Objects.requireNonNull(other, "other must not be null");
        return Money.of(operation.apply(this.amount, other.amount));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isLessThan(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money other)) {
            return false;
        }
        return this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}