package io.github.asaddurrani.ledger.model;

import io.github.asaddurrani.ledger.money.Money;
import java.util.Objects;
import java.util.Optional;

/**
 * Assessment made when an account/day had a negative closing balance.
 * An empty feeAmount explicitly denotes unsupported currency policy, not a waived fee.
 */
public record FeeAssessment(
        String accountId, int accountingDay, Money balanceBefore, Optional<Money> feeAmount) {
    public FeeAssessment {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(balanceBefore, "balanceBefore");
        Objects.requireNonNull(feeAmount, "feeAmount");
    }
}
