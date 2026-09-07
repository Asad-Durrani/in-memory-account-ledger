package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Money;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reconstructs ledger history using fresh state for each invocation. */
public final class LedgerReplay {
    private LedgerReplay() {}

    public static ReplayResult replay(
            List<Account> accounts, LedgerSettings settings, List<EventRecord> eventRecords) {
        var state = new ReplayState();
        var accountsById = new HashMap<String, Account>();
        for (Account account : accounts) {
            accountsById.put(account.accountId(), account);
        }
        var seenEventIds = new HashSet<String>();

        for (EventRecord event : eventRecords) {
            Money amount = switch (event.details()) {
                case EventRecord.Details.Credit credit -> credit.amount();
                case EventRecord.Details.Debit debit -> debit.amount();
                default -> throw new UnsupportedOperationException(
                        "Event type is not implemented yet: "
                                + event.details().getClass().getSimpleName());
            };
            ReplayError.Reason rejection = validate(event, amount, accountsById, settings, seenEventIds);
            if (rejection != null) {
                state.reject(new ReplayError(event, rejection));
                continue;
            }

            boolean debit = event.details() instanceof EventRecord.Details.Debit;
            Money signedAmount = debit
                    ? new Money(amount.currency(), amount.amount().negate()) : amount;
            state.appendLedgerEntry(new LedgerEntry(
                    event.eventId() + (debit ? ":debit" : ":credit"), event.accountId(),
                    signedAmount, event.valueDate(), new LedgerEntry.Source.InputEvent(event.eventId())));
        }
        return state.toResult(accounts);
    }

    /** Report the first validation failure; the first nonblank ID reserves that identity. */
    private static ReplayError.Reason validate(
            EventRecord event, Money amount, Map<String, Account> accounts,
            LedgerSettings settings, Set<String> seenEventIds) {
        if (event.eventId().isBlank()) {
            return ReplayError.Reason.INVALID_EVENT_ID;
        }
        if (!seenEventIds.add(event.eventId())) {
            return ReplayError.Reason.DUPLICATE_EVENT_ID;
        }
        Account account = accounts.get(event.accountId());
        if (account == null) {
            return ReplayError.Reason.UNKNOWN_ACCOUNT;
        }
        if (event.processingDay() < 1 || event.processingDay() > settings.closingDay()) {
            return ReplayError.Reason.INVALID_PROCESSING_DAY;
        }
        if (event.valueDate() < 1 || event.valueDate() > settings.closingDay()) {
            return ReplayError.Reason.INVALID_VALUE_DATE;
        }
        if (amount.currency() != account.currency()) {
            return ReplayError.Reason.CURRENCY_MISMATCH;
        }
        if (amount.amount().signum() <= 0) {
            return ReplayError.Reason.NON_POSITIVE_AMOUNT;
        }
        return null;
    }
}
