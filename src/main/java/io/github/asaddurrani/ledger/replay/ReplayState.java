package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.AppendOnlyJournal;
import io.github.asaddurrani.ledger.model.InMemoryJournal;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.money.Money;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable state owned by one replay invocation. Ledger entries are append-only:
 * later events may add value-dated entries but never mutate or remove earlier entries.
 */
final class ReplayState {
    private final AppendOnlyJournal<LedgerEntry> ledgerEntries = new InMemoryJournal<>();

    private record AuthorizationKey(String accountId, String authorizationId) {}

    private final Map<AuthorizationKey, Authorization> authorizations = new LinkedHashMap<>();
    private final List<ReplayError> errors = new ArrayList<>();

    void reject(ReplayError error) {
        errors.add(error);
    }

    void appendLedgerEntry(LedgerEntry entry) {
        ledgerEntries.append(entry);
    }

    List<LedgerEntry> ledgerEntries() {
        return ledgerEntries.records();
    }

    Authorization authorization(String accountId, String authorizationId) {
        return authorizations.get(new AuthorizationKey(accountId, authorizationId));
    }

    void putAuthorization(Authorization authorization) {
        authorizations.put(new AuthorizationKey(authorization.accountId(), authorization.authorizationId()),
                authorization);
    }

    Money availableOn(Account account, int day) {
        return ReplayResult.balanceOn(account, day, ledgerEntries())
                .subtract(ReplayResult.activeHolds(account, List.copyOf(authorizations.values())));
    }

    ReplayResult toResult(List<Account> accounts) {
        return new ReplayResult(accounts, ledgerEntries(), errors, List.copyOf(authorizations.values()));
    }
}
