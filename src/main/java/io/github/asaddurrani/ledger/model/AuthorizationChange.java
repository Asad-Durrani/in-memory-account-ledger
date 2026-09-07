package io.github.asaddurrani.ledger.model;

import java.util.Objects;

/** Immutable authorization decision retained in submission order. */
public record AuthorizationChange(int processingDay, String eventId, Authorization authorization) {
    public AuthorizationChange {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(authorization, "authorization");
    }
}
