package io.github.asaddurrani.ledger.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Immutable policy parameters fixed for the lifetime of a ledger. */
public record LedgerSettings(
        int closingDay, Money overdraftFee, BigDecimal dailyInterestRate,
        RoundingMode interestRoundingMode) {
    public LedgerSettings {
        Objects.requireNonNull(overdraftFee, "overdraftFee");
        Objects.requireNonNull(dailyInterestRate, "dailyInterestRate");
        Objects.requireNonNull(interestRoundingMode, "interestRoundingMode");
        if (closingDay < 1) {
            throw new IllegalArgumentException("Accounting window must include at least Day 1");
        }
        if (overdraftFee.currency() != Currency.AED) {
            throw new IllegalArgumentException("Only an AED overdraft fee policy is supported");
        }
        if (overdraftFee.amount().signum() < 0 || dailyInterestRate.signum() < 0) {
            throw new IllegalArgumentException("Overdraft fee and daily interest rate must be nonnegative");
        }
    }

    /** Specified fee and daily rate, with the selected half-even interest policy. */
    public static LedgerSettings forWindow(int closingDay) {
        // The specification defines currency precision but not tie-breaking behavior.
        // HALF_EVEN is the selected deterministic rounding policy and is documented
        // as an ambiguity rather than treated as a supplied requirement.
        return new LedgerSettings(closingDay, Money.of(Currency.AED, "25.00"),
                new BigDecimal("0.0004"), RoundingMode.HALF_EVEN);
    }
}
