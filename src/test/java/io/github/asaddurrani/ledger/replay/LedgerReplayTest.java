package io.github.asaddurrani.ledger.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LedgerReplayTest {
    private final Account aed = new Account("ACC-001", Money.of(Currency.AED, "0"));
    private final Account bhd = new Account("ACC-002", Money.of(Currency.BHD, "0.001"));

    private InMemoryLedger ledger() {
        return new InMemoryLedger(List.of(aed, bhd), LedgerSettings.forWindow(6));
    }

    private static EventRecord credit(String id, String accountId, int processingDay, int valueDate,
            Currency currency, String amount) {
        return new EventRecord(id, accountId, processingDay, valueDate,
                new EventRecord.Details.Credit(Money.of(currency, amount)));
    }

    @Test
    void backdatedDebitPreservesEarlierResultsAndEntryPrefixWithDeterministicReplay() {
        var ledger = ledger();
        ledger.appendEvent(credit("E1", aed.accountId(), 1, 1, Currency.AED, "1200"));
        ledger.appendEvent(new EventRecord("E2", aed.accountId(), 1, 1,
                new EventRecord.Details.Debit(Money.of(Currency.AED, "950"))));
        ledger.appendEvent(credit("E4", aed.accountId(), 3, 3, Currency.AED, "400"));
        var before = ledger.replay();
        assertEquals(Money.of(Currency.AED, "250"), before.balanceOn(aed.accountId(), 2));

        ledger.appendEvent(new EventRecord("E7", aed.accountId(), 5, 2,
                new EventRecord.Details.Debit(Money.of(Currency.AED, "620"))));
        var after = ledger.replay();

        assertEquals(Money.of(Currency.AED, "250"), after.balanceOn(aed.accountId(), 1));
        assertEquals(Money.of(Currency.AED, "-370"), after.balanceOn(aed.accountId(), 2));
        assertEquals(Money.of(Currency.AED, "30"), after.balanceOn(aed.accountId(), 3));
        assertEquals(Money.of(Currency.AED, "30"), after.balanceOn(aed.accountId(), 6));
        assertEquals(Money.of(Currency.AED, "250"), before.balanceOn(aed.accountId(), 2));
        assertEquals(before.ledgerEntries(), after.ledgerEntries().subList(0, 3));
        assertEquals(new LedgerEntry("E7:debit", aed.accountId(), Money.of(Currency.AED, "-620"),
                2, new LedgerEntry.Source.InputEvent("E7")), after.ledgerEntries().get(3));
        assertEquals(after, ledger.replay());
        assertEquals(List.of(), after.errors());
        assertEquals(4, after.ledgerEntries().size());
        assertEquals(4, ledger.eventRecords().size());
    }

    @Test
    void submissionOrderIsIndependentOfDatesAndBalancesAreIsolatedByAccount() {
        var ledger = ledger();
        ledger.appendEvent(credit("late", aed.accountId(), 6, 6, Currency.AED, "10"));
        ledger.appendEvent(credit("early", bhd.accountId(), 5, 5, Currency.BHD, "10.001"));
        ledger.appendEvent(credit("future", aed.accountId(), 2, 4, Currency.AED, "2"));
        var result = ledger.replay();

        assertEquals(List.of("late:credit", "early:credit", "future:credit"),
                result.ledgerEntries().stream().map(LedgerEntry::entryId).toList());
        assertEquals(Money.of(Currency.AED, "0"), result.balanceOn(aed.accountId(), 3));
        assertEquals(Money.of(Currency.AED, "2"), result.balanceOn(aed.accountId(), 4));
        assertEquals(Money.of(Currency.AED, "12"), result.balanceOn(aed.accountId(), 6));
        assertEquals(Money.of(Currency.AED, "12"), result.balanceOn(aed.accountId(), 7));
        assertEquals(Money.of(Currency.BHD, "0.001"), result.balanceOn(bhd.accountId(), 4));
        assertEquals(Money.of(Currency.BHD, "10.002"), result.balanceOn(bhd.accountId(), 5));
        assertThrows(IllegalArgumentException.class, () -> result.balanceOn("missing", 1));
        assertThrows(IllegalArgumentException.class, () -> result.balanceOn(aed.accountId(), 0));
    }

    @Test
    void invalidSubmissionsAreRetainedAndReportedInOrderWhileValidEventsStillBook() {
        var ledger = ledger();
        var invalid = List.of(
                credit(" ", aed.accountId(), 1, 1, Currency.AED, "1"),
                credit("unknown", "missing", 1, 1, Currency.AED, "1"),
                credit("day-zero", aed.accountId(), 0, 1, Currency.AED, "1"),
                credit("day-seven", aed.accountId(), 7, 1, Currency.AED, "1"),
                credit("value-zero", aed.accountId(), 1, 0, Currency.AED, "1"),
                credit("value-seven", aed.accountId(), 1, 7, Currency.AED, "1"),
                credit("currency", aed.accountId(), 1, 1, Currency.BHD, "1"),
                credit("zero", aed.accountId(), 1, 1, Currency.AED, "0"),
                credit("negative", aed.accountId(), 1, 1, Currency.AED, "-1"),
                new EventRecord("negative-debit", aed.accountId(), 1, 1,
                        new EventRecord.Details.Debit(Money.of(Currency.AED, "-1"))));
        invalid.forEach(ledger::appendEvent);
        var valid = credit("valid", aed.accountId(), 1, 1, Currency.AED, "3");
        ledger.appendEvent(valid);
        var result = ledger.replay();

        assertEquals(invalid, result.errors().stream().map(ReplayError::event).toList());
        assertEquals(List.of(
                ReplayError.Reason.INVALID_EVENT_ID, ReplayError.Reason.UNKNOWN_ACCOUNT,
                ReplayError.Reason.INVALID_PROCESSING_DAY, ReplayError.Reason.INVALID_PROCESSING_DAY,
                ReplayError.Reason.INVALID_VALUE_DATE, ReplayError.Reason.INVALID_VALUE_DATE,
                ReplayError.Reason.CURRENCY_MISMATCH, ReplayError.Reason.NON_POSITIVE_AMOUNT,
                ReplayError.Reason.NON_POSITIVE_AMOUNT, ReplayError.Reason.NON_POSITIVE_AMOUNT),
                result.errors().stream().map(ReplayError::reason).toList());
        assertEquals(1, result.ledgerEntries().size());
        assertEquals(Money.of(Currency.AED, "3"), result.balanceOn(aed.accountId(), 1));
        var submitted = new ArrayList<>(invalid);
        submitted.add(valid);
        assertEquals(submitted, ledger.eventRecords());
        assertEquals(result, ledger.replay());
    }

    @Test
    void duplicateIdsCannotBookTwiceEvenWhenFirstSubmissionWasRejected() {
        var ledger = ledger();
        var valid = credit("same", aed.accountId(), 1, 1, Currency.AED, "3");
        ledger.appendEvent(valid);
        ledger.appendEvent(valid);
        ledger.appendEvent(credit("same", bhd.accountId(), 1, 1, Currency.BHD, "4"));
        ledger.appendEvent(credit("invalid-first", "missing", 1, 1, Currency.AED, "2"));
        ledger.appendEvent(credit("invalid-first", aed.accountId(), 1, 1, Currency.AED, "2"));
        var result = ledger.replay();

        assertEquals(1, result.ledgerEntries().size());
        assertEquals(List.of(ReplayError.Reason.DUPLICATE_EVENT_ID,
                ReplayError.Reason.DUPLICATE_EVENT_ID, ReplayError.Reason.UNKNOWN_ACCOUNT,
                ReplayError.Reason.DUPLICATE_EVENT_ID),
                result.errors().stream().map(ReplayError::reason).toList());
        assertEquals(result, ledger.replay());
        assertEquals(5, ledger.eventRecords().size());
    }

    static Stream<EventRecord.Details> unsupportedDetails() {
        var amount = Money.of(Currency.AED, "1");
        return Stream.of(new EventRecord.Details.InstalmentCredit(amount, 3));
    }

    @ParameterizedTest
    @MethodSource("unsupportedDetails")
    void unsupportedEventsFailExplicitlyWithoutChangingHistoryOrEarlierResults(EventRecord.Details details) {
        var ledger = ledger();
        ledger.appendEvent(credit("E1", aed.accountId(), 1, 1, Currency.AED, "10"));
        var before = ledger.replay();
        ledger.appendEvent(new EventRecord("unsupported", aed.accountId(), 2, 2, details));
        var history = ledger.eventRecords();

        var error = assertThrows(UnsupportedOperationException.class, ledger::replay);
        assertEquals("Event type is not implemented yet: " + details.getClass().getSimpleName(),
                error.getMessage());
        assertThrows(UnsupportedOperationException.class, ledger::replay);
        assertEquals(history, ledger.eventRecords());
        assertEquals(Money.of(Currency.AED, "10"), before.balanceOn(aed.accountId(), 2));
    }

    @Test
    void resultCopiesEveryCollectionAndExposesImmutableData() {
        var accounts = new ArrayList<>(List.of(aed));
        var entries = new ArrayList<LedgerEntry>();
        var errors = new ArrayList<ReplayError>();
        var event = credit("bad", "missing", 1, 1, Currency.AED, "1");
        errors.add(new ReplayError(event, ReplayError.Reason.UNKNOWN_ACCOUNT));
        var result = new ReplayResult(accounts, entries, errors, List.of());
        accounts.clear();
        entries.add(new LedgerEntry("later", aed.accountId(), Money.of(Currency.AED, "1"),
                1, new LedgerEntry.Source.InputEvent("later")));
        errors.clear();

        assertEquals(List.of(aed), result.accounts());
        assertEquals(List.of(), result.ledgerEntries());
        assertEquals(List.of(new ReplayError(event, ReplayError.Reason.UNKNOWN_ACCOUNT)), result.errors());
        assertThrows(UnsupportedOperationException.class, () -> result.accounts().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.ledgerEntries().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.errors().clear());
    }
}
