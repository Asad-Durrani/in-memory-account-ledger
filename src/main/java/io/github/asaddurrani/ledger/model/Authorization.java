package io.github.asaddurrani.ledger.model;

import io.github.asaddurrani.ledger.money.Money;
import java.util.Objects;

/** Immutable authorization projection; only APPROVED authorizations hold funds. */
public record Authorization(
        String accountId, String authorizationId, String eventId, Money holdAmount, Status status) {
    public Authorization {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(authorizationId, "authorizationId");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(holdAmount, "holdAmount");
        Objects.requireNonNull(status, "status");
    }

    public enum Status {
        APPROVED, REJECTED, SETTLED
    }
}
