package io.github.asaddurrani.ledger.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(Currency currency, BigDecimal amount) implements Comparable<Money> {
    public Money {
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(amount, "amount");
        try {
            amount = amount.setScale(currency.decimalPlaces(), RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Amount cannot be represented at " + currency + " precision: " + amount,
                    exception);
        }
    }

    public static Money of(Currency currency, String amount) {
        return new Money(currency, new BigDecimal(amount));
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(currency, amount.add(other.amount));
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(currency, amount.subtract(other.amount));
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other");
        if (currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + currency + " and " + other.currency);
        }
    }
}
