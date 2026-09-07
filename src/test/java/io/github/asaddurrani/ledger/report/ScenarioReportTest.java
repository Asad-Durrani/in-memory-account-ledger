package io.github.asaddurrani.ledger.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.asaddurrani.ledger.model.Authorization;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScenarioReportTest {
    @Test
    void reportsEveryDayWithRevisedBalancesAndHistoricalDecisions() {
        String report = ScenarioReport.render(ScenarioReport.replay(), 6);
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
