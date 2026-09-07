package io.github.asaddurrani.ledger.report;

import io.github.asaddurrani.ledger.InMemoryLedger;
import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import io.github.asaddurrani.ledger.replay.ReplayResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Runnable replay of the original six-day event stream. */
public final class ScenarioReport {
    private ScenarioReport() {}

    private static Money aed(String amount) { return Money.of(Currency.AED, amount); }

    public static List<EventRecord> events() {
        return List.of(
                new EventRecord("E1", "ACC-001", 1, 1, new EventRecord.Details.Credit(aed("1200"))),
                new EventRecord("E2", "ACC-001", 1, 1, new EventRecord.Details.Debit(aed("950"))),
                new EventRecord("E3", "ACC-001", 2, 2, new EventRecord.Details.Authorization("Auth-A", aed("200"))),
                new EventRecord("E4", "ACC-001", 3, 3, new EventRecord.Details.Credit(aed("400"))),
                new EventRecord("E5", "ACC-001", 4, 4, new EventRecord.Details.Settlement("Auth-A", aed("185"))),
                new EventRecord("E6", "ACC-001", 4, 4, new EventRecord.Details.Settlement("Auth-Z", aed("180"))),
                new EventRecord("E7", "ACC-001", 5, 2, new EventRecord.Details.Debit(aed("620"))),
                new EventRecord("E8", "ACC-001", 5, 5, new EventRecord.Details.Authorization("Auth-B", aed("90"))),
                new EventRecord("E9", "ACC-001", 6, 2, new EventRecord.Details.DebitReversal("E7")),
                new EventRecord("E10", "ACC-002", 5, 5,
                        new EventRecord.Details.InstalmentCredit(Money.of(Currency.BHD, "10.000"), 3)));
    }

    public static ReplayResult replay() {
        var ledger = new InMemoryLedger(List.of(new Account("ACC-001", aed("0")),
                new Account("ACC-002", Money.of(Currency.BHD, "0"))), LedgerSettings.forWindow(6));
        events().forEach(ledger::appendEvent);
        return ledger.replay();
    }

    public static void main(String[] args) {
        System.out.print(render(replay(), 6));
    }

    public static String render(ReplayResult result, int closingDay) {
        if (closingDay < 1) {
            throw new IllegalArgumentException("Closing day must be at least Day 1");
        }
        var output = new StringBuilder("Closing balances: revised after the complete replay; Day 6 includes capitalization.\n"
                + "Fees: accounting day assessed. Authorizations: decisions through processing day.\n"
                + "Errors: rejected events grouped by processing day. Interest: rounded daily accrual.\n");
        for (int day = 1; day <= closingDay; day++) {
            final int reportDay = day;
            output.append("\nDay ").append(day).append('\n');
            for (Account account : result.accounts()) {
                String id = account.accountId();
                String fees = result.feeAssessments().stream()
                        .filter(f -> f.accountId().equals(id) && f.accountingDay() == reportDay)
                        .map(f -> f.feeAmount().map(ScenarioReport::format).orElse("UNSUPPORTED")
                                + " (balance at assessment " + format(f.balanceBefore()) + ")")
                        .collect(Collectors.joining(", "));
                var states = new LinkedHashMap<String, Authorization>();
                // Filter by processing day, retaining submission order even for late inputs.
                for (var change : result.authorizationChanges()) {
                    var authorization = change.authorization();
                    if (change.processingDay() <= day && authorization.accountId().equals(id)) {
                        states.put(authorization.authorizationId(), authorization);
                    }
                }
                String authorizations = states.values().stream()
                        .map(a -> a.authorizationId() + "=" + a.status()
                                + (a.status() == Authorization.Status.APPROVED
                                        ? " (hold " + format(a.holdAmount()) + ")" : ""))
                        .collect(Collectors.joining(", "));
                String errors = result.errors().stream()
                        .filter(e -> e.event().accountId().equals(id) && e.event().processingDay() == reportDay)
                        .map(e -> e.event().eventId() + "=" + e.reason())
                        .collect(Collectors.joining(", "));
                String interest = result.interestAccruals().stream()
                        .filter(a -> a.accountId().equals(id) && a.accountingDay() == reportDay)
                        .map(a -> a.amount().map(ScenarioReport::format).orElse("UNFINALIZED"))
                        .collect(Collectors.joining(", "));
                output.append("  ").append(id).append(" | closing ").append(format(result.balanceOn(id, day)))
                        .append(" | fees ").append(orNone(fees))
                        .append(" | interest ").append(orNone(interest))
                        .append("\n    authorizations: ").append(orNone(authorizations))
                        .append("\n    errors: ").append(orNone(errors)).append('\n');
            }
        }
        return output.toString();
    }

    private static String orNone(String value) { return value.isEmpty() ? "none" : value; }

    private static String format(Money money) {
        return money.currency() + " " + money.amount().toPlainString();
    }
}
