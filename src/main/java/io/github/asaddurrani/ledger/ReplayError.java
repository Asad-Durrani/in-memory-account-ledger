package io.github.asaddurrani.ledger;

import io.github.asaddurrani.ledger.model.EventRecord;
import java.util.Objects;

/** A rejected submission retained alongside the entries produced by replay. */
public record ReplayError(EventRecord event, Reason reason) {
    public ReplayError {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(reason, "reason");
    }

    public enum Reason {
        INVALID_EVENT_ID,
        DUPLICATE_EVENT_ID,
        UNKNOWN_ACCOUNT,
        INVALID_PROCESSING_DAY,
        INVALID_VALUE_DATE,
        CURRENCY_MISMATCH,
        NON_POSITIVE_AMOUNT
    }
}
