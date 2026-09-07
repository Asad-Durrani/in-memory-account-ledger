package io.github.asaddurrani.ledger.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.InterestAccrual;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InterestCapitalizationTest {
    private static Money aed(String amount) { return Money.of(Currency.AED, amount); }

    private static List<LedgerEntry> capitalizations(ReplayResult result) {
        return result.ledgerEntries().stream()
                .filter(entry -> entry.source() instanceof LedgerEntry.Source.InterestCapitalization).toList();
    }

    private static List<Money> accruals(ReplayResult result, String account) {
        return result.interestAccruals().stream().filter(a -> a.accountId().equals(account))
                .map(a -> a.amount().orElseThrow()).toList();
    }

    private static EventRecord credit(String id, int processing, int value, String amount) {
        return new EventRecord(id, "A", processing, value, new EventRecord.Details.Credit(aed(amount)));
    }

    @Test
    void fullScenarioCapitalizesExactDailySumsAfterRetainedFeesAndLateE10() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("0")),
                new Account("B", Money.of(Currency.BHD, "0"))), LedgerSettings.forWindow(6));
        var events = List.of(credit("E1", 1, 1, "1200"),
                new EventRecord("E2", "A", 1, 1, new EventRecord.Details.Debit(aed("950"))),
                new EventRecord("E3", "A", 2, 2, new EventRecord.Details.Authorization("Auth-A", aed("200"))),
                credit("E4", 3, 3, "400"),
                new EventRecord("E5", "A", 4, 4, new EventRecord.Details.Settlement("Auth-A", aed("185"))),
                new EventRecord("E6", "A", 4, 4, new EventRecord.Details.Settlement("Auth-Z", aed("180"))),
                new EventRecord("E7", "A", 5, 2, new EventRecord.Details.Debit(aed("620"))),
                new EventRecord("E8", "A", 5, 5, new EventRecord.Details.Authorization("Auth-B", aed("90"))),
                new EventRecord("E9", "A", 6, 2, new EventRecord.Details.DebitReversal("E7")),
                new EventRecord("E10", "B", 5, 5,
                        new EventRecord.Details.InstalmentCredit(Money.of(Currency.BHD, "10"), 3)));
        events.forEach(ledger::appendEvent);
        var result = ledger.replay();
        assertEquals(List.of(aed("0.10"), aed("0.09"), aed("0.25"), aed("0.17"), aed("0.16"), aed("0.16")),
                accruals(result, "A"));
        assertEquals(List.of("0.000", "0.000", "0.000", "0.000", "0.004", "0.004"),
                accruals(result, "B").stream().map(m -> m.amount().toPlainString()).toList());
        assertEquals(List.of(
                new LedgerEntry("A:interest:6", "A", aed("0.93"), 6, new LedgerEntry.Source.InterestCapitalization(6)),
                new LedgerEntry("B:interest:6", "B", Money.of(Currency.BHD, "0.008"), 6,
                        new LedgerEntry.Source.InterestCapitalization(6))), capitalizations(result));
        for (LedgerEntry entry : capitalizations(result)) {
            assertEquals(entry.amount(), accruals(result, entry.accountId()).stream()
                    .reduce(Money.of(entry.amount().currency(), "0"), Money::add));
        }
        assertEquals(aed("390"), result.balanceOn("A", 5));
        assertEquals(aed("390.93"), result.balanceOn("A", 6));
        assertEquals(Money.of(Currency.BHD, "10.008"), result.balanceOn("B", 6));
        assertEquals(aed("390"), result.interestAccruals().get(5).balanceBeforeCapitalization());
        assertEquals(3, result.feeAssessments().size());
        assertEquals(2, result.errors().size());
        assertEquals(result, ledger.replay());
        assertEquals(events, ledger.eventRecords());
    }

    @ParameterizedTest
    @CsvSource({"AED,312.50,0.12", "AED,337.50,0.14", "BHD,1.250,0.000", "BHD,3.750,0.002",
            "AED,10000000000000000000.01,4000000000000000.00"})
    void roundsDailyToCurrencyPrecisionIncludingHalfEvenTiesAndLargeAmounts(
            Currency currency, String opening, String expected) {
        var result = new InMemoryLedger(List.of(new Account("A", Money.of(currency, opening))),
                LedgerSettings.forWindow(2)).replay();
        Money daily = Money.of(currency, expected);
        assertEquals(List.of(daily, daily), accruals(result, "A"));
        assertEquals(Money.of(currency, opening).add(daily).add(daily), result.balanceOn("A", 2));
        assertEquals(daily.amount().signum() > 0 ? 1 : 0, capitalizations(result).size());
    }

    @Test
    void configuredRateAndRoundingAreUsedWithoutDailyCompoundingOrHoldDeduction() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("100"))),
                new LedgerSettings(2, aed("25"), new BigDecimal("0.01"), RoundingMode.HALF_EVEN));
        ledger.appendEvent(new EventRecord("hold", "A", 1, 1,
                new EventRecord.Details.Authorization("auth", aed("100"))));
        var result = ledger.replay();
        assertEquals(List.of(aed("1"), aed("1")), accruals(result, "A"));
        assertEquals(aed("102"), result.balanceOn("A", 2));
        assertEquals(aed("2"), result.availableOn("A", 2));
        assertEquals(aed("100"), result.interestAccruals().getLast().balanceBeforeCapitalization());

        var halfUp = new InMemoryLedger(List.of(new Account("A", aed("312.50"))),
                new LedgerSettings(2, aed("25"), new BigDecimal("0.0004"), RoundingMode.HALF_UP)).replay();
        assertEquals(List.of(aed("0.13"), aed("0.13")), accruals(halfUp, "A"));
        assertEquals(aed("0.26"), capitalizations(halfUp).getFirst().amount());
    }

    @Test
    void backdatedDebitAndReversalRecalculateAccrualsWithoutChangingPreviousResults() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("0"))), LedgerSettings.forWindow(6));
        ledger.appendEvent(credit("credit", 1, 1, "100"));
        var before = ledger.replay();
        ledger.appendEvent(new EventRecord("debit", "A", 5, 2, new EventRecord.Details.Debit(aed("50"))));
        var reduced = ledger.replay();
        assertEquals(aed("0.14"), capitalizations(reduced).getFirst().amount());
        ledger.appendEvent(new EventRecord("reverse", "A", 6, 2, new EventRecord.Details.DebitReversal("debit")));
        var restored = ledger.replay();
        assertEquals(aed("0.24"), capitalizations(restored).getFirst().amount());
        assertEquals(aed("100.24"), before.balanceOn("A", 6));
        assertEquals(aed("50.14"), reduced.balanceOn("A", 6));
        assertEquals(1, capitalizations(restored).size());
        assertEquals(restored, ledger.replay());
    }

    @Test
    void positiveDaysEarnInterestButZeroAndNegativeDaysDoNotAndClosingFeeComesFirst() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("1000"))), LedgerSettings.forWindow(3));
        ledger.appendEvent(new EventRecord("debit", "A", 2, 2, new EventRecord.Details.Debit(aed("1001"))));
        var result = ledger.replay();
        assertEquals(List.of(aed("0.40"), aed("0"), aed("0")), accruals(result, "A"));
        assertEquals(aed("-51"), result.interestAccruals().getLast().balanceBeforeCapitalization());
        assertEquals(aed("-50.60"), result.balanceOn("A", 3));
        assertEquals(List.of(2, 3), result.feeAssessments().stream().map(a -> a.accountingDay()).toList());
    }

    @Test
    void futureValueDatesExcludeEarlierDaysAndZeroRateCreatesNoFinancialEntry() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("0"))), LedgerSettings.forWindow(3));
        ledger.appendEvent(credit("future", 1, 3, "100"));
        assertEquals(List.of(aed("0"), aed("0"), aed("0.04")), accruals(ledger.replay(), "A"));
        var zeroRate = new InMemoryLedger(List.of(new Account("A", aed("100"))),
                new LedgerSettings(3, aed("25"), BigDecimal.ZERO, RoundingMode.HALF_EVEN)).replay();
        assertEquals(List.of(aed("0"), aed("0"), aed("0")), accruals(zeroRate, "A"));
        assertEquals(List.of(), zeroRate.ledgerEntries());
        var zeroBalance = new InMemoryLedger(List.of(new Account("A", aed("0"))), LedgerSettings.forWindow(3)).replay();
        assertEquals(List.of(aed("0"), aed("0"), aed("0")), accruals(zeroBalance, "A"));
    }

    @Test
    void undefinedBhdFeePreventsItsInterestFinalizationButNotOtherAccounts() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("100")),
                new Account("B", Money.of(Currency.BHD, "0"))), LedgerSettings.forWindow(2));
        ledger.appendEvent(new EventRecord("debit", "B", 2, 1,
                new EventRecord.Details.Debit(Money.of(Currency.BHD, "1"))));
        ledger.appendEvent(new EventRecord("credit", "B", 2, 2,
                new EventRecord.Details.Credit(Money.of(Currency.BHD, "100"))));
        var result = ledger.replay();
        assertTrue(result.interestAccruals().stream().filter(a -> a.accountId().equals("B"))
                .allMatch(a -> a.amount().isEmpty()));
        assertEquals(1, capitalizations(result).size());
        assertEquals("A", capitalizations(result).getFirst().accountId());
        assertEquals(aed("100.08"), result.balanceOn("A", 2));
        assertEquals(Money.of(Currency.BHD, "99"), result.balanceOn("B", 2));
        assertEquals(List.of(), result.errors());
        assertEquals(2, ledger.eventRecords().size());
    }

    @Test
    void resultDefensivelyCopiesAccrualsAndExposesImmutableHistory() {
        var accrual = new InterestAccrual("A", 1, aed("100"), Optional.of(aed("0.04")));
        var supplied = new ArrayList<>(List.of(accrual));
        var result = new ReplayResult(List.of(), List.of(), List.of(), List.of(), List.of(), supplied);
        supplied.clear();
        assertEquals(List.of(accrual), result.interestAccruals());
        assertThrows(UnsupportedOperationException.class, () -> result.interestAccruals().clear());
    }
}
