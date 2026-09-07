package io.github.asaddurrani.ledger.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Run explicitly with ./mvnw -Dtest=IntentionalDesignFailure test.
 * The class name intentionally falls outside Surefire's default test patterns.
 * This is an executable failing assertion, not a disabled or expected-exception test.
 */
class IntentionalDesignFailure {
    @Test
    void recoveredBhdAccountCanFinalizeDailyInterest() {
        var ledger = new InMemoryLedger(
                List.of(new Account("ACC-002", Money.of(Currency.BHD, "0"))),
                LedgerSettings.forWindow(6));
        ledger.appendEvent(new EventRecord("debit", "ACC-002", 1, 1,
                new EventRecord.Details.Debit(Money.of(Currency.BHD, "1"))));
        ledger.appendEvent(new EventRecord("recovery", "ACC-002", 2, 2,
                new EventRecord.Details.Credit(Money.of(Currency.BHD, "100"))));

        var result = ledger.replay();
        assertTrue(result.errors().isEmpty(), "Both monetary inputs must be accepted");
        assertEquals(Money.of(Currency.BHD, "99.000"), result.balanceOn("ACC-002", 2));
        assertEquals(6, result.interestAccruals().size());

        // INTENTIONAL FAILURE: recovery to a positive balance does not restore this
        // design's ability to finalize interest. Day 1 needs a BHD overdraft policy,
        // but settings support only an AED fee; the unsupported assessment leaves
        // every daily accrual unfinalized, even after recovery. This asserts a desired
        // broader capability, not a rule supplied by the original prompt. A proper
        // extension needs explicit currency-specific fee policy, not a guessed BHD
        // amount or a silent waiver. No exact interest total is claimed without it.
        assertTrue(result.interestAccruals().stream().allMatch(accrual -> accrual.amount().isPresent()),
                "Design limitation: a recovered BHD account cannot finalize interest without a BHD fee policy");
    }
}
