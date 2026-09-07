package io.github.asaddurrani.ledger.model;

import io.github.asaddurrani.ledger.money.Money;
import java.util.Objects;
import java.util.Optional;

/**
 * Final daily interest calculation before capitalization. An empty amount means
 * unresolved fee assessment prevents finalizing interest; it is not zero interest.
 */
public record InterestAccrual(
        String accountId, int accountingDay, Money balanceBeforeCapitalization, Optional<Money> amount) {
    public InterestAccrual {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(balanceBeforeCapitalization, "balanceBeforeCapitalization");
        Objects.requireNonNull(amount, "amount");
    }
}
