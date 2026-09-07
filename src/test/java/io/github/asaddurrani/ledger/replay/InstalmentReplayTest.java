package io.github.asaddurrani.ledger.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InstalmentReplayTest {
    private static InMemoryLedger ledger(Currency currency) {
        return new InMemoryLedger(List.of(new Account("A", Money.of(currency, "0"))), LedgerSettings.forWindow(6));
    }

    private static EventRecord instalments(String id, Currency currency, String total, int count) {
        return new EventRecord(id, "A", 5, 5,
                new EventRecord.Details.InstalmentCredit(Money.of(currency, total), count));
    }

    @Test
    void e10AllocatesRemainderFirstAndPreservesEarlierResultAndSubmissionOrder() {
        var ledger = ledger(Currency.BHD);
        ledger.appendEvent(new EventRecord("earlier", "A", 6, 6,
                new EventRecord.Details.Credit(Money.of(Currency.BHD, "1"))));
        var before = ledger.replay();
        var e10 = instalments("E10", Currency.BHD, "10.000", 3);
        ledger.appendEvent(e10);
        var result = ledger.replay();
        assertEquals(List.of(
                new LedgerEntry("E10:instalment:1", "A", Money.of(Currency.BHD, "3.334"), 5,
                        new LedgerEntry.Source.InputEvent("E10")),
                new LedgerEntry("E10:instalment:2", "A", Money.of(Currency.BHD, "3.333"), 5,
                        new LedgerEntry.Source.InputEvent("E10")),
                new LedgerEntry("E10:instalment:3", "A", Money.of(Currency.BHD, "3.333"), 5,
                        new LedgerEntry.Source.InputEvent("E10"))), result.ledgerEntries().subList(1, 4));
        assertEquals(Money.of(Currency.BHD, "0"), result.balanceOn("A", 4));
        assertEquals(Money.of(Currency.BHD, "10"), result.balanceOn("A", 5));
        assertEquals(Money.of(Currency.BHD, "11"), result.balanceOn("A", 6));
        assertEquals(before.ledgerEntries(), result.ledgerEntries().subList(0, 1));
        assertEquals(Money.of(Currency.BHD, "0"), before.balanceOn("A", 5));
        assertEquals(result, ledger.replay());
        assertEquals(List.of(), result.errors());

        ledger.appendEvent(e10);
        var duplicate = ledger.replay();
        assertEquals(result.ledgerEntries(), duplicate.ledgerEntries());
        assertEquals(ReplayError.Reason.DUPLICATE_EVENT_ID, duplicate.errors().getFirst().reason());
    }

    @ParameterizedTest
    @CsvSource({"AED,1.00,3,0.34,0.33", "BHD,0.008,3,0.003,0.002",
            "AED,0.03,3,0.01,0.01", "BHD,0.001,1,0.001,0.001",
            "AED,1.20,3,0.40,0.40", "BHD,10000000000000000000.001,2,5000000000000000000.001,5000000000000000000.000"})
    void allocationIsExactPositiveAndDiffersByAtMostOneMinorUnit(
            Currency currency, String total, int count, String first, String last) {
        var ledger = ledger(currency);
        ledger.appendEvent(instalments("split", currency, total, count));
        var result = ledger.replay();
        var entries = result.ledgerEntries();
        assertEquals(count, entries.size());
        assertEquals(Money.of(currency, first), entries.getFirst().amount());
        assertEquals(Money.of(currency, last), entries.getLast().amount());
        assertEquals(Money.of(currency, total), result.balanceOn("A", 5));
        var minorUnit = java.math.BigDecimal.ONE.movePointLeft(currency.decimalPlaces());
        assertTrue(entries.getFirst().amount().amount().subtract(entries.getLast().amount().amount())
                .compareTo(minorUnit) <= 0);
        assertTrue(entries.stream().allMatch(entry -> entry.amount().amount().signum() > 0));
        assertEquals(List.of(), result.errors());
    }

    @ParameterizedTest
    @CsvSource({"BHD,0.002,3,ZERO_VALUE_INSTALMENT", "AED,0.01,2,ZERO_VALUE_INSTALMENT",
            "BHD,10,0,INVALID_INSTALMENT_COUNT", "BHD,10,-1,INVALID_INSTALMENT_COUNT",
            "BHD,0,3,NON_POSITIVE_AMOUNT", "BHD,-1,3,NON_POSITIVE_AMOUNT",
            "BHD,0.001,2147483647,ZERO_VALUE_INSTALMENT"})
    void invalidSplitsBookNothingRetainHistoryAndAllowLaterValidEvents(
            Currency currency, String total, int count, ReplayError.Reason reason) {
        var ledger = ledger(currency);
        var invalid = instalments("invalid", currency, total, count);
        ledger.appendEvent(invalid);
        var rejected = ledger.replay();
        assertEquals(List.of(), rejected.ledgerEntries());
        assertEquals(List.of(new ReplayError(invalid, reason)), rejected.errors());
        ledger.appendEvent(instalments("valid", currency, "1", 1));
        var result = ledger.replay();
        assertEquals(1, result.ledgerEntries().size());
        assertEquals(Money.of(currency, "1"), result.balanceOn("A", 5));
        assertEquals(invalid, ledger.eventRecords().getFirst());
        assertEquals(result, ledger.replay());
    }

    @Test
    void commonValidationStillRejectsCurrencyAccountAndDatesBeforePostingAnyShares() {
        var ledger = ledger(Currency.AED);
        ledger.appendEvent(instalments("currency", Currency.BHD, "10", 3));
        var details = new EventRecord.Details.InstalmentCredit(Money.of(Currency.AED, "10"), 3);
        ledger.appendEvent(new EventRecord("account", "missing", 5, 5, details));
        ledger.appendEvent(new EventRecord("processing", "A", 7, 5, details));
        ledger.appendEvent(new EventRecord("value", "A", 5, 0, details));
        ledger.appendEvent(new EventRecord(" ", "A", 5, 5, details));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.CURRENCY_MISMATCH, ReplayError.Reason.UNKNOWN_ACCOUNT,
                ReplayError.Reason.INVALID_PROCESSING_DAY, ReplayError.Reason.INVALID_VALUE_DATE,
                ReplayError.Reason.INVALID_EVENT_ID), result.errors().stream().map(ReplayError::reason).toList());
        assertEquals(List.of(), result.ledgerEntries());
    }
}
