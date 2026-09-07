package io.github.asaddurrani.ledger;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.EventRecord;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import java.util.List;

/** Reconstructs ledger history using fresh state for each invocation. */
final class LedgerReplay {
    private LedgerReplay() {}

    static ReplayResult replay(
            List<Account> accounts, LedgerSettings settings, List<EventRecord> eventRecords) {
        var state = new ReplayState();

        // Event interpretation will be implemented in the next increment.
        // Reject nonempty history explicitly rather than silently ignoring events.
        if (!eventRecords.isEmpty()) {
            throw new UnsupportedOperationException("Financial event replay is not implemented yet");
        }

        return state.toResult();
    }
}
