package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
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
                case EventRecord.Details.Authorization authorization -> authorization.holdAmount();
                case EventRecord.Details.Settlement settlement -> settlement.amount();
                default -> throw new UnsupportedOperationException(
                        "Event type is not implemented yet: "
                                + event.details().getClass().getSimpleName());
            };
            ReplayError.Reason rejection = validate(event, amount, accountsById, settings, seenEventIds);
            if (rejection != null) {
                retainRejectedAuthorization(event, accountsById, state, rejection);
                state.reject(new ReplayError(event, rejection));
                continue;
            }

            switch (event.details()) {
                case EventRecord.Details.Credit credit -> appendPosting(state, event, amount, "credit", false);
                case EventRecord.Details.Debit debit -> appendPosting(state, event, amount, "debit", true);
                case EventRecord.Details.Authorization authorization ->
                        authorize(state, accountsById.get(event.accountId()), event, authorization);
                case EventRecord.Details.Settlement settlement -> settle(state, event, settlement);
                default -> throw new AssertionError("Unsupported event passed dispatch");
            }
        }
        return state.toResult(accounts);
    }

    private static void appendPosting(
            ReplayState state, EventRecord event, Money amount, String type, boolean debit) {
        Money signedAmount = debit ? new Money(amount.currency(), amount.amount().negate()) : amount;
        state.appendLedgerEntry(new LedgerEntry(event.eventId() + ":" + type, event.accountId(),
                signedAmount, event.valueDate(), new LedgerEntry.Source.InputEvent(event.eventId())));
    }

    private static void authorize(ReplayState state, Account account, EventRecord event,
            EventRecord.Details.Authorization details) {
        if (details.authorizationId().isBlank()) {
            state.reject(new ReplayError(event, ReplayError.Reason.INVALID_AUTHORIZATION_ID));
            return;
        }
        if (state.authorization(event.accountId(), details.authorizationId()) != null) {
            state.reject(new ReplayError(event, ReplayError.Reason.DUPLICATE_AUTHORIZATION_ID));
            return;
        }
        // Operational decisions use entries known now, through the processing day,
        // and all currently active holds. Later backdating does not rewrite them.
        boolean approved = state.availableOn(account, event.processingDay())
                .compareTo(details.holdAmount()) >= 0;
        state.putAuthorization(new Authorization(event.accountId(), details.authorizationId(),
                event.eventId(), details.holdAmount(),
                approved ? Authorization.Status.APPROVED : Authorization.Status.REJECTED));
        if (!approved) {
            state.reject(new ReplayError(event, ReplayError.Reason.INSUFFICIENT_AVAILABLE_BALANCE));
        }
    }

    private static void settle(ReplayState state, EventRecord event, EventRecord.Details.Settlement details) {
        if (details.authorizationId().isBlank()) {
            state.reject(new ReplayError(event, ReplayError.Reason.INVALID_AUTHORIZATION_ID));
            return;
        }
        Authorization authorization = state.authorization(event.accountId(), details.authorizationId());
        if (authorization == null) {
            state.reject(new ReplayError(event, ReplayError.Reason.UNKNOWN_AUTHORIZATION));
        } else if (authorization.status() != Authorization.Status.APPROVED) {
            state.reject(new ReplayError(event, ReplayError.Reason.AUTHORIZATION_NOT_ACTIVE));
        } else if (details.amount().compareTo(authorization.holdAmount()) > 0) {
            state.reject(new ReplayError(event, ReplayError.Reason.SETTLEMENT_EXCEEDS_HOLD));
        } else {
            appendPosting(state, event, details.amount(), "settlement", true);
            state.putAuthorization(new Authorization(authorization.accountId(), authorization.authorizationId(),
                    authorization.eventId(), authorization.holdAmount(), Authorization.Status.SETTLED));
        }
    }

    /** Reserve identifiable rejected authorizations without replacing an existing hold. */
    private static void retainRejectedAuthorization(EventRecord event, Map<String, Account> accounts,
            ReplayState state, ReplayError.Reason reason) {
        if (event.details() instanceof EventRecord.Details.Authorization details
                && reason != ReplayError.Reason.INVALID_EVENT_ID
                && reason != ReplayError.Reason.DUPLICATE_EVENT_ID
                && accounts.containsKey(event.accountId())
                && !details.authorizationId().isBlank()
                && state.authorization(event.accountId(), details.authorizationId()) == null) {
            state.putAuthorization(new Authorization(event.accountId(), details.authorizationId(),
                    event.eventId(), details.holdAmount(), Authorization.Status.REJECTED));
        }
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
