package io.github.asaddurrani.ledger;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.Money;
import java.util.List;

/** Immutable entries and rejections produced by one replay, with its account definitions. */
public record ReplayResult(
        List<Account> accounts, List<LedgerEntry> ledgerEntries, List<ReplayError> errors) {
    public ReplayResult {
        accounts = List.copyOf(accounts);
        ledgerEntries = List.copyOf(ledgerEntries);
        errors = List.copyOf(errors);
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
        Account account = accounts.stream()
                .filter(candidate -> candidate.accountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown account: " + accountId));
        Money balance = account.openingBalance();
        for (LedgerEntry entry : ledgerEntries) {
            if (entry.accountId().equals(accountId) && entry.valueDate() <= day) {
                balance = balance.add(entry.amount());
            }
        }
        return balance;
    }
}
