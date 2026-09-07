package io.github.asaddurrani.ledger.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryJournalTest {
    @Test
    void eventHistoryPreservesInsertionOrderAndRepeatedRecords() {
        AppendOnlyJournal<EventRecord> journal = new InMemoryJournal<>();
        var reversal = new EventRecord("E9", "ACC-001", 6, 2,
                new EventRecord.Details.DebitReversal("E7"));
        var instalments = new EventRecord("E10", "ACC-002", 5, 5,
                new EventRecord.Details.InstalmentCredit(Money.of(Currency.BHD, "10"), 3));

        journal.append(reversal);
        journal.append(instalments);
        journal.append(reversal);

        assertEquals(List.of(reversal, instalments, reversal), journal.records());
    }

    @Test
    void monetaryHistoryCannotBeUpdatedOrDeletedThroughItsSnapshot() {
        AppendOnlyJournal<LedgerEntry> journal = new InMemoryJournal<>();
        var debit = new LedgerEntry("E7-debit", "ACC-001", Money.of(Currency.AED, "-620"),
                2, new LedgerEntry.Source.InputEvent("E7"));
        var reversal = new LedgerEntry("E9-credit", "ACC-001", Money.of(Currency.AED, "620"),
                2, new LedgerEntry.Source.InputEvent("E9"));
        journal.append(debit);
        var snapshot = journal.records();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.set(0, reversal));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(reversal));
        var iterator = snapshot.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);

        journal.append(reversal);

        assertEquals(List.of(debit), snapshot);
        assertEquals(List.of(debit, reversal), journal.records());
    }

    @Test
    void nullAppendDoesNotChangeExistingHistory() {
        AppendOnlyJournal<EventRecord> journal = new InMemoryJournal<>();
        var event = new EventRecord("E1", "ACC-001", 1, 1,
                new EventRecord.Details.Credit(Money.of(Currency.AED, "1200")));
        journal.append(event);

        assertThrows(NullPointerException.class, () -> journal.append(null));
        assertEquals(List.of(event), journal.records());
    }
}
