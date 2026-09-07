package io.github.asaddurrani.ledger.model;

import io.github.asaddurrani.ledger.money.Money;
import java.util.Objects;

/** A recognized monetary change; positive amounts credit and negative amounts debit. */
public record LedgerEntry(
        String entryId, String accountId, Money amount, int valueDate, Source source) {
    public LedgerEntry {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(source, "source");
    }

    public sealed interface Source {
        record InputEvent(String eventId) implements Source {
            public InputEvent {
                Objects.requireNonNull(eventId, "eventId");
            }
        }

        record OverdraftFee(int accountingDay) implements Source {}

        record InterestCapitalization(int throughDay) implements Source {}
    }
}
