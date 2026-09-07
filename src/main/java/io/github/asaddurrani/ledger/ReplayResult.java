package io.github.asaddurrani.ledger;

import io.github.asaddurrani.ledger.model.LedgerEntry;
import java.util.List;

/** Immutable ledger history produced by one replay. */
public record ReplayResult(List<LedgerEntry> ledgerEntries) {
    public ReplayResult {
        ledgerEntries = List.copyOf(ledgerEntries);
    }
}
