package io.github.asaddurrani.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import io.github.asaddurrani.ledger.replay.ReplayResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryLedgerTest {
    private final Account aed = new Account("ACC-001", Money.of(Currency.AED, "0"));
    private final Account bhd = new Account("ACC-002", Money.of(Currency.BHD, "0"));

    @Test
    void appendsAcrossAccountsInSubmissionOrder() {
        var ledger = new InMemoryLedger(List.of(aed, bhd), LedgerSettings.forWindow(6));
        var e7 = new EventRecord("E7", aed.accountId(), 5, 2,
                new EventRecord.Details.Debit(Money.of(Currency.AED, "620")));
        var e9 = new EventRecord("E9", aed.accountId(), 6, 2,
                new EventRecord.Details.DebitReversal("E7"));
        var e10 = new EventRecord("E10", bhd.accountId(), 5, 5,
                new EventRecord.Details.InstalmentCredit(Money.of(Currency.BHD, "10"), 3));

        ledger.appendEvent(e7);
        ledger.appendEvent(e9);
        ledger.appendEvent(e10);

        assertEquals(List.of(e7, e9, e10), ledger.eventRecords());
        assertEquals(Money.of(Currency.AED, "0"), aed.openingBalance());
        assertEquals(Currency.BHD, bhd.currency());
    }

    @Test
    void retainsUnknownReferencesAndRepeatedSubmissionsForReplay() {
        var ledger = new InMemoryLedger(List.of(aed), LedgerSettings.forWindow(6));
        var settlement = new EventRecord("E6", aed.accountId(), 4, 4,
                new EventRecord.Details.Settlement("Auth-Z", Money.of(Currency.AED, "180")));
        var unknownAccount = new EventRecord("unknown", "ACC-UNKNOWN", 4, 4,
                new EventRecord.Details.Credit(Money.of(Currency.AED, "1")));

        ledger.appendEvent(settlement);
        ledger.appendEvent(settlement);
        ledger.appendEvent(unknownAccount);

        assertEquals(List.of(settlement, settlement, unknownAccount), ledger.eventRecords());
    }

    @Test
    void historyIsImmutableAndEarlierSnapshotsSurviveLaterAppends() {
        var ledger = new InMemoryLedger(List.of(aed), LedgerSettings.forWindow(6));
        var authorization = new EventRecord("E3", aed.accountId(), 2, 2,
                new EventRecord.Details.Authorization("Auth-A", Money.of(Currency.AED, "200")));
        ledger.appendEvent(authorization);
        var snapshot = ledger.eventRecords();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);

        ledger.appendEvent(new EventRecord("E4", aed.accountId(), 3, 3,
                new EventRecord.Details.Credit(Money.of(Currency.AED, "400"))));

        assertEquals(List.of(authorization), snapshot);
        assertEquals(2, ledger.eventRecords().size());
    }

    @Test
    void accountDefinitionsAreOwnedByTheLedgerAndCannotBeReplacedExternally() {
        var suppliedAccounts = new ArrayList<>(List.of(aed, bhd));
        var ledger = new InMemoryLedger(suppliedAccounts, LedgerSettings.forWindow(6));
        suppliedAccounts.clear();

        assertEquals(List.of(aed, bhd), ledger.accounts());
        assertEquals(6, ledger.settings().closingDay());
        assertThrows(UnsupportedOperationException.class, () -> ledger.accounts().clear());
        assertThrows(IllegalArgumentException.class,
                () -> new InMemoryLedger(
                        List.of(aed, new Account(aed.accountId(), bhd.openingBalance())),
                        LedgerSettings.forWindow(6)));
        assertThrows(NullPointerException.class, () -> new InMemoryLedger(List.of(aed), null));
    }

    @Test
    void repeatedEmptyReplayReturnsEqualImmutableResults() {
        var ledger = new InMemoryLedger(List.of(aed), LedgerSettings.forWindow(6));

        var first = ledger.replay();
        var second = ledger.replay();

        assertEquals(new ReplayResult(List.of(aed), List.of(), List.of(), List.of()), first);
        assertEquals(first, second);
        assertThrows(UnsupportedOperationException.class, () -> first.ledgerEntries().clear());
        assertEquals(List.of(), ledger.eventRecords());
    }

    @Test
    void nullSubmissionLeavesExistingHistoryIntact() {
        var ledger = new InMemoryLedger(List.of(aed), LedgerSettings.forWindow(6));
        var credit = new EventRecord("E1", aed.accountId(), 1, 1,
                new EventRecord.Details.Credit(Money.of(Currency.AED, "1200")));
        ledger.appendEvent(credit);

        assertThrows(NullPointerException.class, () -> ledger.appendEvent(null));
        assertEquals(List.of(credit), ledger.eventRecords());
    }
}
