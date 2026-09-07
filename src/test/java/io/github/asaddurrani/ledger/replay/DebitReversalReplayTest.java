package io.github.asaddurrani.ledger.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

class DebitReversalReplayTest {
    // These posting-focused cases exclude fees; OverdraftAssessmentTest covers fee interactions.
    private static LedgerSettings withoutFees() {
        return new LedgerSettings(6, Money.of(Currency.AED, "0"),
                java.math.BigDecimal.ZERO, java.math.RoundingMode.HALF_EVEN);
    }

    private static Money aed(String amount) {
        return Money.of(Currency.AED, amount);
    }

    private static InMemoryLedger ledger() {
        return new InMemoryLedger(List.of(new Account("A", aed("0")),
                new Account("B", Money.of(Currency.BHD, "0"))), withoutFees());
    }

    private static EventRecord debit(String id, String amount) {
        return new EventRecord(id, "A", 5, 2, new EventRecord.Details.Debit(aed(amount)));
    }

    private static EventRecord reversal(String id, String target, int valueDate) {
        return new EventRecord(id, "A", 6, valueDate, new EventRecord.Details.DebitReversal(target));
    }

    private static List<ReplayError.Reason> reasons(ReplayResult result) {
        return result.errors().stream().map(ReplayError::reason).toList();
    }

    @Test
    void e9OffsetsE7WithoutChangingOriginalPostingOrEarlierReplayResult() {
        var ledger = ledger();
        ledger.appendEvent(debit("E7", "620"));
        var before = ledger.replay();
        ledger.appendEvent(reversal("E9", "E7", 2));
        var after = ledger.replay();

        assertEquals(aed("-620"), before.balanceOn("A", 2));
        assertEquals(aed("0"), after.balanceOn("A", 2));
        assertEquals(before.ledgerEntries(), after.ledgerEntries().subList(0, 1));
        assertEquals(new LedgerEntry("E9:reversal", "A", aed("620"), 2,
                new LedgerEntry.Source.InputEvent("E9")), after.ledgerEntries().getLast());
        assertEquals(List.of(), after.errors());
        assertEquals(after, ledger.replay());
        assertEquals(2, ledger.eventRecords().size());
    }

    @Test
    void suppliedReversalDateControlsWhenCreditAffectsBalances() {
        var ledger = ledger();
        ledger.appendEvent(debit("d", "10"));
        ledger.appendEvent(reversal("r", "d", 4));
        var result = ledger.replay();
        assertEquals(aed("0"), result.balanceOn("A", 1));
        assertEquals(aed("-10"), result.balanceOn("A", 2));
        assertEquals(aed("-10"), result.balanceOn("A", 3));
        assertEquals(aed("0"), result.balanceOn("A", 4));
    }

    @Test
    void repeatedReversalsCannotRefundTwiceAndReversalEntriesCannotBeReversed() {
        var ledger = ledger();
        ledger.appendEvent(debit("d", "10"));
        var reverse = reversal("r", "d", 2);
        ledger.appendEvent(reverse);
        ledger.appendEvent(reverse);
        ledger.appendEvent(reversal("second", "d", 2));
        ledger.appendEvent(reversal("reverse-reversal", "r", 2));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.DUPLICATE_EVENT_ID,
                ReplayError.Reason.DEBIT_ALREADY_REVERSED, ReplayError.Reason.INVALID_REVERSAL_TARGET),
                reasons(result));
        assertEquals(2, result.ledgerEntries().size());
        assertEquals(aed("0"), result.balanceOn("A", 6));
        assertEquals(result, ledger.replay());
    }

    @Test
    void onlyPreviouslyAcceptedDirectDebitsOnSameAccountAreEligible() {
        var ledger = ledger();
        ledger.appendEvent(reversal("early", "d", 2));
        ledger.appendEvent(debit("d", "10"));
        ledger.appendEvent(new EventRecord("cross-account", "B", 6, 2,
                new EventRecord.Details.DebitReversal("d")));
        ledger.appendEvent(debit("rejected", "-1"));
        ledger.appendEvent(reversal("reverse-rejected", "rejected", 2));
        ledger.appendEvent(new EventRecord("credit", "A", 1, 1, new EventRecord.Details.Credit(aed("100"))));
        ledger.appendEvent(reversal("reverse-credit", "credit", 2));
        ledger.appendEvent(new EventRecord("auth", "A", 2, 2,
                new EventRecord.Details.Authorization("hold", aed("10"))));
        ledger.appendEvent(reversal("reverse-auth", "auth", 2));
        ledger.appendEvent(new EventRecord("settle", "A", 4, 4,
                new EventRecord.Details.Settlement("hold", aed("10"))));
        ledger.appendEvent(reversal("reverse-settlement", "settle", 4));
        ledger.appendEvent(reversal("blank", " ", 2));
        ledger.appendEvent(reversal("valid", "d", 2));
        var result = ledger.replay();

        assertEquals(List.of(ReplayError.Reason.INVALID_REVERSAL_TARGET,
                ReplayError.Reason.INVALID_REVERSAL_TARGET, ReplayError.Reason.NON_POSITIVE_AMOUNT,
                ReplayError.Reason.INVALID_REVERSAL_TARGET, ReplayError.Reason.INVALID_REVERSAL_TARGET,
                ReplayError.Reason.INVALID_REVERSAL_TARGET, ReplayError.Reason.INVALID_REVERSAL_TARGET,
                ReplayError.Reason.INVALID_REVERSAL_TARGET), reasons(result));
        assertEquals(4, result.ledgerEntries().size());
        assertEquals(aed("90"), result.balanceOn("A", 6));
        assertEquals(Money.of(Currency.BHD, "0"), result.balanceOn("B", 6));
        assertEquals(13, ledger.eventRecords().size());
        assertEquals(result, ledger.replay());
    }

    @Test
    void invalidReversalEnvelopeDoesNotConsumeDebit() {
        var ledger = ledger();
        ledger.appendEvent(debit("d", "10"));
        ledger.appendEvent(new EventRecord("unknown", "missing", 6, 2,
                new EventRecord.Details.DebitReversal("d")));
        ledger.appendEvent(new EventRecord("bad-day", "A", 0, 2,
                new EventRecord.Details.DebitReversal("d")));
        ledger.appendEvent(reversal("bad-value", "d", 7));
        ledger.appendEvent(reversal("", "d", 2));
        ledger.appendEvent(reversal("valid", "d", 2));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.UNKNOWN_ACCOUNT, ReplayError.Reason.INVALID_PROCESSING_DAY,
                ReplayError.Reason.INVALID_VALUE_DATE, ReplayError.Reason.INVALID_EVENT_ID), reasons(result));
        assertEquals(aed("0"), result.balanceOn("A", 6));
        assertEquals(2, result.ledgerEntries().size());
    }

    @Test
    void bhdReversalUsesExactAmountAndCurrencyFromItsDebit() {
        var ledger = ledger();
        ledger.appendEvent(new EventRecord("d", "B", 5, 2,
                new EventRecord.Details.Debit(Money.of(Currency.BHD, "1.001"))));
        ledger.appendEvent(new EventRecord("r", "B", 6, 2, new EventRecord.Details.DebitReversal("d")));
        var result = ledger.replay();
        assertEquals(Money.of(Currency.BHD, "1.001"), result.ledgerEntries().getLast().amount());
        assertEquals(Money.of(Currency.BHD, "0"), result.balanceOn("B", 2));
        assertEquals(List.of(), result.errors());
    }
}
