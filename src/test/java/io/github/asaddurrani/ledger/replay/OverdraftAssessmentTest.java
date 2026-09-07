package io.github.asaddurrani.ledger.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.FeeAssessment;
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

class OverdraftAssessmentTest {
    // Keep these tests focused on postings/fees; InterestCapitalizationTest covers interest integration.
    private static LedgerSettings withoutInterest(int closingDay) {
        return new LedgerSettings(closingDay, Money.of(Currency.AED, "25"),
                java.math.BigDecimal.ZERO, java.math.RoundingMode.HALF_EVEN);
    }

    private static Money aed(String amount) { return Money.of(Currency.AED, amount); }

    private static InMemoryLedger ledger(String opening, int closingDay) {
        return new InMemoryLedger(List.of(new Account("A", aed(opening))), withoutInterest(closingDay));
    }

    private static EventRecord credit(String id, int processingDay, int valueDate, String amount) {
        return new EventRecord(id, "A", processingDay, valueDate, new EventRecord.Details.Credit(aed(amount)));
    }

    private static EventRecord debit(String id, int processingDay, int valueDate, String amount) {
        return new EventRecord(id, "A", processingDay, valueDate, new EventRecord.Details.Debit(aed(amount)));
    }

    private static List<LedgerEntry> fees(ReplayResult result) {
        return result.ledgerEntries().stream().filter(e -> e.source() instanceof LedgerEntry.Source.OverdraftFee).toList();
    }

    private static List<EventRecord> scenario() {
        return List.of(credit("E1", 1, 1, "1200"), debit("E2", 1, 1, "950"),
                new EventRecord("E3", "A", 2, 2, new EventRecord.Details.Authorization("Auth-A", aed("200"))),
                credit("E4", 3, 3, "400"),
                new EventRecord("E5", "A", 4, 4, new EventRecord.Details.Settlement("Auth-A", aed("185"))),
                new EventRecord("E6", "A", 4, 4, new EventRecord.Details.Settlement("Auth-Z", aed("180"))),
                debit("E7", 5, 2, "620"),
                new EventRecord("E8", "A", 5, 5, new EventRecord.Details.Authorization("Auth-B", aed("90"))),
                new EventRecord("E9", "A", 6, 2, new EventRecord.Details.DebitReversal("E7")),
                new EventRecord("E10", "B", 5, 5,
                        new EventRecord.Details.InstalmentCredit(Money.of(Currency.BHD, "10"), 3)));
    }

    @Test
    void fullScenarioRetainsThreeFeesAfterReversalAndLateE10WithoutDuplicatingOnReplay() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("0")),
                new Account("B", Money.of(Currency.BHD, "0"))), withoutInterest(6));
        scenario().forEach(ledger::appendEvent);
        var result = ledger.replay();
        assertEquals(List.of(2, 4, 5), fees(result).stream().map(LedgerEntry::valueDate).toList());
        assertEquals(aed("-75"), fees(result).stream().map(LedgerEntry::amount).reduce(aed("0"), Money::add));
        assertEquals(List.of(aed("-370"), aed("-180"), aed("-205")),
                result.feeAssessments().stream().map(FeeAssessment::balanceBefore).toList());
        assertEquals(List.of(aed("250"), aed("225"), aed("625"), aed("415"), aed("390"), aed("390")),
                java.util.stream.IntStream.rangeClosed(1, 6).mapToObj(day -> result.balanceOn("A", day)).toList());
        assertEquals(Money.of(Currency.BHD, "10"), result.balanceOn("B", 5));
        assertEquals(List.of(Authorization.Status.SETTLED, Authorization.Status.REJECTED),
                result.authorizations().stream().map(Authorization::status).toList());
        assertEquals(List.of(ReplayError.Reason.UNKNOWN_AUTHORIZATION, ReplayError.Reason.INSUFFICIENT_AVAILABLE_BALANCE),
                result.errors().stream().map(ReplayError::reason).toList());
        assertTrue(result.feeAssessments().stream().allMatch(a -> a.feeAmount().equals(Optional.of(aed("25")))));
        assertEquals(result, ledger.replay());
        assertEquals(scenario(), ledger.eventRecords());
    }

    @Test
    void e7ProducesThreeFeesByDayFiveAndEarlierResultsRemainImmutableAfterE9() {
        var ledger = ledger("0", 5);
        scenario().subList(0, 8).forEach(ledger::appendEvent);
        var result = ledger.replay();
        assertEquals(aed("-230"), result.balanceOn("A", 5));
        assertEquals(List.of(2, 4, 5), fees(result).stream().map(LedgerEntry::valueDate).toList());

        var longer = ledger("0", 6);
        scenario().subList(0, 9).forEach(longer::appendEvent);
        assertEquals(fees(result), fees(longer.replay()));
        assertEquals(aed("-230"), result.balanceOn("A", 5));
        assertThrows(UnsupportedOperationException.class, () -> result.feeAssessments().clear());
    }

    @Test
    void reassessmentHappensBeforeNextAuthorizationAndEarlierFeesCanMakeLaterDaysNegative() {
        var ledger = ledger("0", 3);
        ledger.appendEvent(credit("credit", 2, 2, "30"));
        ledger.appendEvent(debit("backdated", 3, 1, "10"));
        ledger.appendEvent(new EventRecord("auth", "A", 3, 3, new EventRecord.Details.Authorization("hold", aed("1"))));
        var result = ledger.replay();
        assertEquals(List.of(aed("-10"), aed("-5"), aed("-30")),
                result.feeAssessments().stream().map(FeeAssessment::balanceBefore).toList());
        assertEquals(Authorization.Status.REJECTED, result.authorizations().getFirst().status());
        assertEquals(aed("-55"), result.balanceOn("A", 3));
    }

    @Test
    void sameDayRecoveryAvoidsFeeAndHoldsAreNotPartOfClosingLedgerBalance() {
        var ledger = ledger("10", 2);
        ledger.appendEvent(new EventRecord("auth", "A", 1, 1, new EventRecord.Details.Authorization("hold", aed("10"))));
        ledger.appendEvent(debit("debit", 1, 1, "20"));
        ledger.appendEvent(credit("credit", 1, 1, "10"));
        var result = ledger.replay();
        assertEquals(aed("0"), result.balanceOn("A", 2));
        assertEquals(aed("-10"), result.availableOn("A", 2));
        assertEquals(List.of(), result.feeAssessments());
    }

    @Test
    void emptyHistoryAssessesNegativeOpeningBalancesAndSkippedDaysPerAccount() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("-1")),
                new Account("B", aed("-2")), new Account("C", aed("0"))), withoutInterest(3));
        var result = ledger.replay();
        assertEquals(6, fees(result).size());
        assertEquals(List.of("A", "B", "A", "B", "A", "B"),
                result.feeAssessments().stream().map(FeeAssessment::accountId).toList());
        assertEquals(aed("-76"), result.balanceOn("A", 3));
        assertEquals(aed("-77"), result.balanceOn("B", 3));
        assertEquals(aed("0"), result.balanceOn("C", 3));
        assertEquals(result, ledger.replay());
    }

    @Test
    void lateProcessingDatesNeverReopenDayAndReversalNeverDeletesFees() {
        var ledger = ledger("0", 4);
        ledger.appendEvent(credit("advance", 4, 4, "20"));
        ledger.appendEvent(debit("late", 2, 2, "10"));
        ledger.appendEvent(new EventRecord("reverse", "A", 3, 2, new EventRecord.Details.DebitReversal("late")));
        var result = ledger.replay();
        assertEquals(List.of(2, 3, 4), fees(result).stream().map(LedgerEntry::valueDate).toList());
        assertEquals(aed("-55"), result.balanceOn("A", 4));
        assertEquals(result, ledger.replay());
    }

    @Test
    void repeatedBackdatingAndRejectedInputsCannotChargeSameDayTwice() {
        var ledger = ledger("0", 3);
        ledger.appendEvent(debit("a", 3, 1, "1"));
        ledger.appendEvent(debit("b", 3, 1, "1"));
        ledger.appendEvent(debit("bad", 3, 1, "-100"));
        var result = ledger.replay();
        assertEquals(3, fees(result).size());
        assertEquals(aed("-77"), result.balanceOn("A", 3));
        assertEquals(ReplayError.Reason.NON_POSITIVE_AMOUNT, result.errors().getFirst().reason());
    }

    @Test
    void rejectedInputWithValidDayAdvancesCloseButOutOfWindowDayDoesNot() {
        var ledger = ledger("-1", 2);
        ledger.appendEvent(credit("out-of-window", 7, 1, "100"));
        ledger.appendEvent(credit("recover", 1, 1, "1"));
        assertEquals(List.of(), ledger.replay().feeAssessments());

        var second = ledger("-1", 2);
        second.appendEvent(new EventRecord("unknown", "missing", 2, 2, new EventRecord.Details.Credit(aed("1"))));
        second.appendEvent(credit("late-recovery", 1, 1, "100"));
        assertEquals(List.of(1), fees(second.replay()).stream().map(LedgerEntry::valueDate).toList());
    }

    @Test
    void unsupportedBhdAssessmentIsExplicitAndDoesNotRejectDebitOrInventFee() {
        var ledger = new InMemoryLedger(List.of(new Account("B", Money.of(Currency.BHD, "0"))),
                withoutInterest(3));
        var debit = new EventRecord("d", "B", 3, 1, new EventRecord.Details.Debit(Money.of(Currency.BHD, "1")));
        ledger.appendEvent(debit);
        ledger.appendEvent(new EventRecord("r", "B", 3, 1, new EventRecord.Details.DebitReversal("d")));
        var result = ledger.replay();
        assertEquals(List.of(1, 2), result.feeAssessments().stream().map(FeeAssessment::accountingDay).toList());
        assertTrue(result.feeAssessments().stream().allMatch(a -> a.feeAmount().isEmpty()));
        assertEquals(List.of(), fees(result));
        assertEquals(List.of(), result.errors());
        assertEquals(2, result.ledgerEntries().size());
        assertEquals(Money.of(Currency.BHD, "0"), result.balanceOn("B", 3));
        assertEquals(debit, ledger.eventRecords().getFirst());
        assertEquals(result, ledger.replay());
    }

    @Test
    void configuredFeeIsUsedAndZeroFeeDoesNotCreateZeroPosting() {
        var ledger = new InMemoryLedger(List.of(new Account("A", aed("-1"))),
                new LedgerSettings(2, aed("3"), BigDecimal.ZERO, RoundingMode.HALF_EVEN));
        assertEquals(aed("-7"), ledger.replay().balanceOn("A", 2));
        var zero = new InMemoryLedger(List.of(new Account("A", aed("-1"))),
                new LedgerSettings(2, aed("0"), BigDecimal.ZERO, RoundingMode.HALF_EVEN)).replay();
        assertEquals(List.of(), zero.ledgerEntries());
        assertEquals(2, zero.feeAssessments().size());
        assertTrue(zero.feeAssessments().stream().allMatch(a -> a.feeAmount().equals(Optional.of(aed("0")))));
    }

    @Test
    void resultDefensivelyCopiesFeeAssessments() {
        var assessments = new ArrayList<>(List.of(new FeeAssessment("A", 1, aed("-1"), Optional.of(aed("25")))));
        var result = new ReplayResult(List.of(), List.of(), List.of(), List.of(), assessments, List.of());
        assessments.clear();
        assertEquals(1, result.feeAssessments().size());
        assertThrows(UnsupportedOperationException.class, () -> result.feeAssessments().clear());
    }
}
