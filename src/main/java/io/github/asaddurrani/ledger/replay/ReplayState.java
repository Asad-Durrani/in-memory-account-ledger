package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.AuthorizationChange;
import io.github.asaddurrani.ledger.model.AppendOnlyJournal;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.FeeAssessment;
import io.github.asaddurrani.ledger.model.InMemoryJournal;
import io.github.asaddurrani.ledger.model.InterestAccrual;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.money.Money;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mutable state owned by one replay invocation. Ledger entries are append-only:
 * later events may add value-dated entries but never mutate or remove earlier entries.
 */
final class ReplayState {
    private final AppendOnlyJournal<LedgerEntry> ledgerEntries = new InMemoryJournal<>();

    private int closedThrough;
    private record FeeKey(String accountId, int day) {}
    private final Map<FeeKey, FeeAssessment> feeAssessments = new LinkedHashMap<>();

    private record AuthorizationKey(String accountId, String authorizationId) {}

    private final Map<AuthorizationKey, Authorization> authorizations = new LinkedHashMap<>();
    private final Map<String, EventRecord> acceptedDebits = new HashMap<>();
    private final Set<String> reversedDebits = new HashSet<>();
    private final List<InterestAccrual> interestAccruals = new ArrayList<>();
    private final List<AuthorizationChange> authorizationChanges = new ArrayList<>();
    private final List<ReplayError> errors = new ArrayList<>();

    int closedThrough() {
        return closedThrough;
    }

    void closedThrough(int day) {
        closedThrough = day;
    }

    FeeAssessment feeAssessment(String accountId, int day) {
        return feeAssessments.get(new FeeKey(accountId, day));
    }

    void recordFeeAssessment(FeeAssessment assessment) {
        feeAssessments.put(new FeeKey(assessment.accountId(), assessment.accountingDay()), assessment);
    }

    boolean hasUnsupportedFeeAssessment(String accountId) {
        return feeAssessments.values().stream()
                .anyMatch(assessment -> assessment.accountId().equals(accountId) && assessment.feeAmount().isEmpty());
    }

    void recordInterestAccrual(InterestAccrual accrual) {
        interestAccruals.add(accrual);
    }

    void reject(ReplayError error) {
        errors.add(error);
    }

    void appendLedgerEntry(LedgerEntry entry) {
        ledgerEntries.append(entry);
    }

    List<LedgerEntry> ledgerEntries() {
        return ledgerEntries.records();
    }

    void recordAcceptedDebit(EventRecord event) {
        acceptedDebits.put(event.eventId(), event);
    }

    EventRecord acceptedDebit(String eventId) {
        return acceptedDebits.get(eventId);
    }

    boolean isReversed(String eventId) {
        return reversedDebits.contains(eventId);
    }

    void markReversed(String eventId) {
        reversedDebits.add(eventId);
    }

    Authorization authorization(String accountId, String authorizationId) {
        return authorizations.get(new AuthorizationKey(accountId, authorizationId));
    }

    void putAuthorization(EventRecord event, Authorization authorization) {
        authorizationChanges.add(new AuthorizationChange(event.processingDay(), event.eventId(), authorization));
        authorizations.put(new AuthorizationKey(authorization.accountId(), authorization.authorizationId()),
                authorization);
    }

    Money availableOn(Account account, int day) {
        return ReplayResult.balanceOn(account, day, ledgerEntries())
                .subtract(ReplayResult.activeHolds(account, List.copyOf(authorizations.values())));
    }

    ReplayResult toResult(List<Account> accounts) {
        return new ReplayResult(accounts, ledgerEntries(), errors, List.copyOf(authorizations.values()),
                List.copyOf(feeAssessments.values()), interestAccruals, authorizationChanges);
    }
}
