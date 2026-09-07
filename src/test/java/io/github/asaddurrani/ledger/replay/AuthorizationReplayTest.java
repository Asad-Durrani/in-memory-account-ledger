package io.github.asaddurrani.ledger.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AuthorizationReplayTest {
    // These posting-focused cases exclude fees; OverdraftAssessmentTest covers fee interactions.
    private static LedgerSettings withoutFees() {
        return new LedgerSettings(6, Money.of(Currency.AED, "0"),
                java.math.BigDecimal.ZERO, java.math.RoundingMode.HALF_EVEN);
    }

    private static final String ACCOUNT = "ACC-001";

    private static Money aed(String amount) {
        return Money.of(Currency.AED, amount);
    }

    private static InMemoryLedger ledger(String opening) {
        return new InMemoryLedger(List.of(new Account(ACCOUNT, aed(opening))), LedgerSettings.forWindow(6));
    }

    private static EventRecord auth(String id, String authId, String amount) {
        return new EventRecord(id, ACCOUNT, 2, 2, new EventRecord.Details.Authorization(authId, aed(amount)));
    }

    private static EventRecord settle(String id, String authId, String amount) {
        return new EventRecord(id, ACCOUNT, 4, 4, new EventRecord.Details.Settlement(authId, aed(amount)));
    }

    private static List<ReplayError.Reason> reasons(ReplayResult result) {
        return result.errors().stream().map(ReplayError::reason).toList();
    }

    @Test
    void suppliedAuthorizationAndSettlementReleaseWholeHoldAndRejectUnknownReference() {
        var ledger = ledger("0");
        ledger.appendEvent(new EventRecord("E1", ACCOUNT, 1, 1, new EventRecord.Details.Credit(aed("1200"))));
        ledger.appendEvent(new EventRecord("E2", ACCOUNT, 1, 1, new EventRecord.Details.Debit(aed("950"))));
        ledger.appendEvent(auth("E3", "Auth-A", "200"));
        var held = ledger.replay();
        assertEquals(aed("250"), held.balanceOn(ACCOUNT, 2));
        assertEquals(aed("50"), held.availableOn(ACCOUNT, 2));
        assertEquals(2, held.ledgerEntries().size());

        ledger.appendEvent(new EventRecord("E4", ACCOUNT, 3, 3, new EventRecord.Details.Credit(aed("400"))));
        ledger.appendEvent(settle("E5", "Auth-A", "185"));
        ledger.appendEvent(settle("E6", "Auth-Z", "180"));
        var result = ledger.replay();
        assertEquals(aed("465"), result.balanceOn(ACCOUNT, 4));
        assertEquals(aed("465"), result.availableOn(ACCOUNT, 4));
        assertEquals(Authorization.Status.SETTLED, result.authorizations().getFirst().status());
        assertEquals(new LedgerEntry("E5:settlement", ACCOUNT, aed("-185"), 4,
                new LedgerEntry.Source.InputEvent("E5")), result.ledgerEntries().getLast());
        assertEquals(List.of(ReplayError.Reason.UNKNOWN_AUTHORIZATION), reasons(result));
        assertEquals("E6", result.errors().getFirst().event().eventId());
        assertEquals(held.ledgerEntries(), result.ledgerEntries().subList(0, 2));
        assertEquals(Authorization.Status.APPROVED, held.authorizations().getFirst().status());
        assertEquals(aed("50"), held.availableOn(ACCOUNT, 2));
        assertEquals(result, ledger.replay());
        assertEquals(6, ledger.eventRecords().size());
    }

    @Test
    void approvalIncludesOtherHoldsAndAllowsExactlyZeroAvailable() {
        var ledger = ledger("100");
        ledger.appendEvent(auth("a", "A", "60"));
        ledger.appendEvent(auth("b", "B", "40"));
        ledger.appendEvent(auth("c", "C", "0.01"));
        var result = ledger.replay();
        assertEquals(List.of(Authorization.Status.APPROVED, Authorization.Status.APPROVED,
                Authorization.Status.REJECTED), result.authorizations().stream().map(Authorization::status).toList());
        assertEquals(aed("100"), result.balanceOn(ACCOUNT, 6));
        assertEquals(aed("0"), result.availableOn(ACCOUNT, 6));
        assertEquals(List.of(), result.ledgerEntries());
        assertEquals(List.of(ReplayError.Reason.INSUFFICIENT_AVAILABLE_BALANCE), reasons(result));
    }

    @Test
    void rejectedAndSettledAuthorizationIdsCannotBeReusedOrSettledAgain() {
        var ledger = ledger("100");
        ledger.appendEvent(auth("a", "A", "101"));
        ledger.appendEvent(auth("b", "A", "1"));
        ledger.appendEvent(settle("c", "A", "1"));
        ledger.appendEvent(auth("d", "B", "50"));
        ledger.appendEvent(auth("e", "B", "1"));
        ledger.appendEvent(settle("f", "B", "50"));
        ledger.appendEvent(settle("g", "B", "1"));
        ledger.appendEvent(auth("h", "B", "1"));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.INSUFFICIENT_AVAILABLE_BALANCE,
                ReplayError.Reason.DUPLICATE_AUTHORIZATION_ID, ReplayError.Reason.AUTHORIZATION_NOT_ACTIVE,
                ReplayError.Reason.DUPLICATE_AUTHORIZATION_ID, ReplayError.Reason.AUTHORIZATION_NOT_ACTIVE,
                ReplayError.Reason.DUPLICATE_AUTHORIZATION_ID), reasons(result));
        assertEquals(2, result.authorizations().size());
        assertEquals(1, result.ledgerEntries().size());
        assertEquals(aed("50"), result.availableOn(ACCOUNT, 6));
    }

    @ParameterizedTest
    @CsvSource({"0,NON_POSITIVE_AMOUNT", "-1,NON_POSITIVE_AMOUNT", "51,SETTLEMENT_EXCEEDS_HOLD"})
    void invalidSettlementPreservesHoldAndAllowsLaterValidSettlement(String amount, ReplayError.Reason reason) {
        var ledger = ledger("100");
        ledger.appendEvent(auth("a", "A", "50"));
        ledger.appendEvent(settle("bad", "A", amount));
        var rejected = ledger.replay();
        assertEquals(List.of(reason), reasons(rejected));
        assertEquals(aed("50"), rejected.availableOn(ACCOUNT, 4));
        assertEquals(List.of(), rejected.ledgerEntries());
        ledger.appendEvent(settle("good", "A", "40"));
        assertEquals(aed("60"), ledger.replay().availableOn(ACCOUNT, 4));
        assertEquals(Authorization.Status.APPROVED, rejected.authorizations().getFirst().status());
    }

    @Test
    void backdatedDebitDoesNotRevokeApprovalOrBlockPreviouslyAuthorizedSettlement() {
        var ledger = new InMemoryLedger(List.of(new Account(ACCOUNT, aed("100"))), withoutFees());
        ledger.appendEvent(auth("a", "A", "100"));
        ledger.appendEvent(new EventRecord("debit", ACCOUNT, 5, 1, new EventRecord.Details.Debit(aed("200"))));
        ledger.appendEvent(settle("s", "A", "90"));
        var result = ledger.replay();
        assertEquals(List.of(), result.errors());
        assertEquals(Authorization.Status.SETTLED, result.authorizations().getFirst().status());
        assertEquals(aed("-190"), result.balanceOn(ACCOUNT, 4));
        assertEquals(result, ledger.replay());
    }

    @Test
    void laterBackdatingDoesNotApproveEarlierRejectionAndFutureCreditsDoNotFundCurrentHolds() {
        var ledger = ledger("0");
        ledger.appendEvent(new EventRecord("future", ACCOUNT, 1, 6, new EventRecord.Details.Credit(aed("100"))));
        ledger.appendEvent(auth("a", "A", "1"));
        ledger.appendEvent(new EventRecord("backdated", ACCOUNT, 5, 1, new EventRecord.Details.Credit(aed("100"))));
        ledger.appendEvent(settle("s", "A", "1"));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.INSUFFICIENT_AVAILABLE_BALANCE,
                ReplayError.Reason.AUTHORIZATION_NOT_ACTIVE), reasons(result));
        assertEquals(Authorization.Status.REJECTED, result.authorizations().getFirst().status());
        assertEquals(aed("200"), result.availableOn(ACCOUNT, 6));
    }

    @Test
    void authorizationIdsAreScopedToAccountAndCurrenciesCannotCross() {
        var ledger = new InMemoryLedger(List.of(new Account(ACCOUNT, aed("100")),
                new Account("BHD", Money.of(Currency.BHD, "10"))), LedgerSettings.forWindow(6));
        ledger.appendEvent(auth("a", "shared", "50"));
        ledger.appendEvent(new EventRecord("b", "BHD", 2, 2,
                new EventRecord.Details.Authorization("shared", Money.of(Currency.BHD, "5.001"))));
        ledger.appendEvent(new EventRecord("wrong-currency", ACCOUNT, 4, 4,
                new EventRecord.Details.Settlement("shared", Money.of(Currency.BHD, "1"))));
        ledger.appendEvent(auth("only-a", "only-a", "1"));
        ledger.appendEvent(new EventRecord("wrong-account", "BHD", 4, 4,
                new EventRecord.Details.Settlement("only-a", Money.of(Currency.BHD, "1"))));
        ledger.appendEvent(new EventRecord("settle-b", "BHD", 4, 4,
                new EventRecord.Details.Settlement("shared", Money.of(Currency.BHD, "4.999"))));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.CURRENCY_MISMATCH, ReplayError.Reason.UNKNOWN_AUTHORIZATION),
                reasons(result));
        assertEquals(aed("49"), result.availableOn(ACCOUNT, 4));
        assertEquals(Money.of(Currency.BHD, "5.001"), result.availableOn("BHD", 4));
        assertEquals(result, ledger.replay());
    }

    @Test
    void invalidAuthorizationIsReservedButBlankIdentityAndDuplicateEventsDoNotCreateHolds() {
        var ledger = ledger("100");
        ledger.appendEvent(auth("invalid", "A", "-1"));
        ledger.appendEvent(auth("retry", "A", "1"));
        ledger.appendEvent(auth("blank", " ", "1"));
        ledger.appendEvent(settle("blank-settle", " ", "1"));
        var valid = auth("valid", "B", "10");
        ledger.appendEvent(valid);
        ledger.appendEvent(valid);
        ledger.appendEvent(auth("valid", "C", "10"));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.NON_POSITIVE_AMOUNT, ReplayError.Reason.DUPLICATE_AUTHORIZATION_ID,
                ReplayError.Reason.INVALID_AUTHORIZATION_ID, ReplayError.Reason.INVALID_AUTHORIZATION_ID,
                ReplayError.Reason.DUPLICATE_EVENT_ID, ReplayError.Reason.DUPLICATE_EVENT_ID), reasons(result));
        assertEquals(2, result.authorizations().size());
        assertEquals(aed("90"), result.availableOn(ACCOUNT, 4));
    }

    @Test
    void invalidDatesAndCurrencyCannotChangeActiveHold() {
        var ledger = ledger("100");
        ledger.appendEvent(auth("a", "A", "50"));
        ledger.appendEvent(new EventRecord("bad-day", ACCOUNT, 7, 4,
                new EventRecord.Details.Settlement("A", aed("40"))));
        ledger.appendEvent(new EventRecord("bad-value", ACCOUNT, 4, 0,
                new EventRecord.Details.Settlement("A", aed("40"))));
        ledger.appendEvent(new EventRecord("bad-auth-currency", ACCOUNT, 2, 2,
                new EventRecord.Details.Authorization("B", Money.of(Currency.BHD, "1"))));
        var result = ledger.replay();
        assertEquals(List.of(ReplayError.Reason.INVALID_PROCESSING_DAY, ReplayError.Reason.INVALID_VALUE_DATE,
                ReplayError.Reason.CURRENCY_MISMATCH), reasons(result));
        assertEquals(aed("50"), result.availableOn(ACCOUNT, 4));
        assertEquals(List.of(), result.ledgerEntries());
    }

    @Test
    void authorizationResultsAreDefensivelyCopiedAndImmutable() {
        var supplied = new ArrayList<>(List.of(new Authorization(ACCOUNT, "A", "a", aed("1"),
                Authorization.Status.APPROVED)));
        var result = new ReplayResult(List.of(new Account(ACCOUNT, aed("10"))), List.of(), List.of(), supplied, List.of());
        supplied.clear();
        assertEquals(1, result.authorizations().size());
        assertEquals(aed("9"), result.availableOn(ACCOUNT, 1));
        assertThrows(UnsupportedOperationException.class, () -> result.authorizations().clear());
    }
}
