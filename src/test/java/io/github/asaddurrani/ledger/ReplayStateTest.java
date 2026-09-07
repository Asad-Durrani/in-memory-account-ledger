package io.github.asaddurrani.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.asaddurrani.ledger.model.Currency;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.Money;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplayStateTest {
    private final LedgerEntry entry = new LedgerEntry(
            "entry-1", "ACC-001", Money.of(Currency.AED, "1"),
            1, new LedgerEntry.Source.InputEvent("E1"));

    @Test
    void separateReplayStatesDoNotShareEntriesAndEarlierResultsRemainUnchanged() {
        var first = new ReplayState();
        var second = new ReplayState();
        var beforeAppend = first.toResult(List.of());

        first.appendLedgerEntry(entry);

        assertEquals(List.of(entry), first.toResult(List.of()).ledgerEntries());
        assertEquals(List.of(), second.toResult(List.of()).ledgerEntries());
        assertEquals(List.of(), beforeAppend.ledgerEntries());
        assertThrows(UnsupportedOperationException.class, () -> first.ledgerEntries().clear());
    }

    @Test
    void resultDefensivelyCopiesSuppliedEntries() {
        var supplied = new ArrayList<>(List.of(entry));
        var result = new ReplayResult(List.of(), supplied, List.of());

        supplied.clear();

        assertEquals(List.of(entry), result.ledgerEntries());
        assertThrows(UnsupportedOperationException.class, () -> result.ledgerEntries().clear());
    }
}
