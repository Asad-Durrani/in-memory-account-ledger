package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.Authorization;
import io.github.asaddurrani.ledger.model.AuthorizationChange;
import io.github.asaddurrani.ledger.model.InterestAccrual;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.FeeAssessment;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;

/** Immutable entries and rejections produced by one replay, with its account definitions. */
public record ReplayResult(
        List<Account> accounts, List<LedgerEntry> ledgerEntries, List<ReplayError> errors,
        List<Authorization> authorizations, List<FeeAssessment> feeAssessments,
        List<InterestAccrual> interestAccruals, List<AuthorizationChange> authorizationChanges) {
    public ReplayResult {
        accounts = List.copyOf(accounts);
        ledgerEntries = List.copyOf(ledgerEntries);
        errors = List.copyOf(errors);
        authorizations = List.copyOf(authorizations);
        feeAssessments = List.copyOf(feeAssessments);
        interestAccruals = List.copyOf(interestAccruals);
        authorizationChanges = List.copyOf(authorizationChanges);
    }

    /**
     * Balance as known after this replay, including entries value-dated on or before
     * the requested day. This is not the balance known when that day originally closed.
     * Days after the window carry forward the entries in this result only.
     */
    public Money balanceOn(String accountId, int day) {
        if (day < 1) {
            throw new IllegalArgumentException("Balance day must be at least Day 1");
        }
        return balanceOn(account(accountId), day, ledgerEntries);
    }

    private Account account(String accountId) {
        return accounts.stream()
                .filter(candidate -> candidate.accountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown account: " + accountId));
    }

    static Money balanceOn(Account account, int day, List<LedgerEntry> entries) {
        Money balance = account.openingBalance();
        for (LedgerEntry entry : entries) {
            if (entry.accountId().equals(account.accountId()) && entry.valueDate() <= day) {
                balance = balance.add(entry.amount());
            }
        }
        return balance;
    }

    static Money activeHolds(Account account, List<Authorization> authorizations) {
        Money total = Money.of(account.currency(), "0");
        for (Authorization authorization : authorizations) {
            if (authorization.accountId().equals(account.accountId())
                    && authorization.status() == Authorization.Status.APPROVED) {
                total = total.add(authorization.holdAmount());
            }
        }
        return total;
    }
}
