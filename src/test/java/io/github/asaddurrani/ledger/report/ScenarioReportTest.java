package io.github.asaddurrani.ledger.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScenarioReportTest {
    @Test
    void reportsEveryDayWithRevisedBalancesAndHistoricalDecisions() {
        String report = ScenarioReport.render(ScenarioReport.replay(), 6);
        assertFalse(report.contains("Unassigned errors:"));
        String[] days = report.split("\nDay ");
        assertEquals(7, days.length);
        String[] aed = {"250.00", "225.00", "625.00", "415.00", "390.00", "390.93"};
        String[] bhd = {"0.000", "0.000", "0.000", "0.000", "10.000", "10.008"};
        for (int day = 1; day <= 6; day++) {
            assertTrue(days[day].contains("ACC-001 | closing AED " + aed[day - 1] + " |"));
            assertTrue(days[day].contains("ACC-002 | closing BHD " + bhd[day - 1] + " |"));
        }
        assertTrue(days[1].contains("authorizations: none"));
        assertTrue(days[2].contains("Auth-A=APPROVED (hold AED 200.00)"));
        assertTrue(days[3].contains("Auth-A=APPROVED (hold AED 200.00)"));
        assertTrue(days[4].contains("Auth-A=SETTLED"));
        assertTrue(days[4].contains("E6=UNKNOWN_AUTHORIZATION"));
        assertTrue(days[5].contains("Auth-B=REJECTED"));
        assertTrue(days[5].contains("E8=INSUFFICIENT_AVAILABLE_BALANCE"));
        assertTrue(days[6].contains("Auth-A=SETTLED, Auth-B=REJECTED"));
        assertTrue(days[6].contains("errors: none"));
        for (int day : List.of(2, 4, 5)) {
            assertTrue(days[day].contains("fees AED 25.00 (balance at assessment AED -"));
        }
        assertTrue(days[6].contains("interest AED 0.16"));
        assertTrue(days[6].contains("interest BHD 0.004"));
    }

    @Test
    void reportsEveryRejectionOnceIncludingUnknownAccountsAndUndisplayedDays() {
        var ledger = new InMemoryLedger(
                List.of(new Account("A", Money.of(Currency.AED, "0"))), LedgerSettings.forWindow(6));
        var zeroCredit = new EventRecord.Details.Credit(Money.of(Currency.AED, "0"));
        List.of(
                new EventRecord("daily", "A", 1, 1, zeroCredit),
                new EventRecord("unknown", "missing", 2, 2, zeroCredit),
                new EventRecord("before-window", "A", 0, 1, zeroCredit),
                new EventRecord("after-window", "A", 7, 1, zeroCredit),
                new EventRecord("both", "missing", 7, 1, zeroCredit),
                new EventRecord("last-day", "A", 6, 6, zeroCredit))
                .forEach(ledger::appendEvent);
        var result = ledger.replay();
        assertEquals(6, result.errors().size());

        for (int reportDays : List.of(5, 6)) {
            String report = ScenarioReport.render(result, reportDays);
            String[] sections = report.split("\nUnassigned errors:\n");
            assertEquals(2, sections.length);
            assertTrue(sections[0].contains("daily=NON_POSITIVE_AMOUNT"));
            assertFalse(sections[1].contains("daily="));
            assertTrue(sections[1].contains("unknown=UNKNOWN_ACCOUNT | account missing | processing day 2"));
            assertTrue(sections[1].contains("before-window=INVALID_PROCESSING_DAY | account A | processing day 0"));
            assertTrue(sections[1].contains("after-window=INVALID_PROCESSING_DAY | account A | processing day 7"));
            assertTrue(sections[1].contains("both=UNKNOWN_ACCOUNT | account missing | processing day 7"));
            assertEquals(reportDays == 5, sections[1].contains("last-day=NON_POSITIVE_AMOUNT"));
            for (var error : result.errors()) {
                String marker = error.event().eventId() + "=";
                assertEquals(1, report.lines().filter(line -> line.contains(marker)).count(), marker);
            }
        }
    }

    @Test
    void retainsImmutableTransitionsAndOriginalNonmonotonicEventOrder() {
        var result = ScenarioReport.replay();
        assertEquals(List.of("E3", "E5", "E8"),
                result.authorizationChanges().stream().map(c -> c.eventId()).toList());
        assertEquals(List.of(Authorization.Status.APPROVED, Authorization.Status.SETTLED, Authorization.Status.REJECTED),
                result.authorizationChanges().stream().map(c -> c.authorization().status()).toList());
        assertThrows(UnsupportedOperationException.class, () -> result.authorizationChanges().clear());
        assertEquals("E9", ScenarioReport.events().get(8).eventId());
        assertEquals(6, ScenarioReport.events().get(8).processingDay());
        assertEquals("E10", ScenarioReport.events().get(9).eventId());
        assertEquals(5, ScenarioReport.events().get(9).processingDay());
        assertEquals(result, ScenarioReport.replay());
    }
}
