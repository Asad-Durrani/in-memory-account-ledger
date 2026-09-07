package io.github.asaddurrani.ledger;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.AppendOnlyJournal;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.InMemoryJournal;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.replay.LedgerReplay;
import io.github.asaddurrani.ledger.replay.ReplayResult;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Owns account definitions, settings, and the submitted append-only event stream. */
public final class InMemoryLedger {
    private final List<Account> accounts;
    private final LedgerSettings settings;
    private final AppendOnlyJournal<EventRecord> eventRecords = new InMemoryJournal<>();

    /** Settings are fixed at construction; the window starts at Day 1. */
    public InMemoryLedger(List<Account> accounts, LedgerSettings settings) {
        this.accounts = List.copyOf(accounts);
        this.settings = Objects.requireNonNull(settings, "settings");
        var accountIds = new HashSet<String>();
        for (Account account : this.accounts) {
            if (!accountIds.add(account.accountId())) {
                throw new IllegalArgumentException("Duplicate account ID: " + account.accountId());
            }
        }
    }

    public void appendEvent(EventRecord record) {
        eventRecords.append(record);
    }

    /**
     * Reconstructs the ledger history that would have been booked by processing
     * submitted immutable event history in insertion order. Each invocation uses
     * fresh state, so the same history must produce the same result without duplicates.
     * All modeled input event types are supported.
     * Overdraft fees are assessed through the closing day; interest remains pending.
     */
    public ReplayResult replay() {
        return LedgerReplay.replay(accounts, settings, eventRecords.records());
    }

    public List<Account> accounts() {
        return accounts;
    }

    public LedgerSettings settings() {
        return settings;
    }

    public List<EventRecord> eventRecords() {
        return eventRecords.records();
    }
}
