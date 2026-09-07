package io.github.asaddurrani.ledger;

import io.github.asaddurrani.ledger.model.AppendOnlyJournal;
import io.github.asaddurrani.ledger.model.InMemoryJournal;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import java.util.List;

/**
 * Mutable state owned by one replay invocation. Ledger entries are append-only:
 * later events may add value-dated entries but never mutate or remove earlier entries.
 */
final class ReplayState {
    private final AppendOnlyJournal<LedgerEntry> ledgerEntries = new InMemoryJournal<>();

    void appendLedgerEntry(LedgerEntry entry) {
        ledgerEntries.append(entry);
    }

    List<LedgerEntry> ledgerEntries() {
        return ledgerEntries.records();
    }

    ReplayResult toResult() {
        return new ReplayResult(ledgerEntries());
    }
}
