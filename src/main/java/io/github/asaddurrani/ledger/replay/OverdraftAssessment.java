package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.FeeAssessment;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Money;
import java.util.List;
import java.util.Optional;

/** Assesses days chronologically so each fee contributes to later closing balances. */
final class OverdraftAssessment {
    private OverdraftAssessment() {}

    static void closeThrough(ReplayState state, List<Account> accounts, LedgerSettings settings, int day) {
        while (state.closedThrough() < day) {
            int nextDay = state.closedThrough() + 1;
            for (Account account : accounts) {
                assess(state, account, settings, nextDay);
            }
            state.closedThrough(nextDay);
        }
    }

    static void reassess(ReplayState state, Account account, LedgerSettings settings, int valueDate) {
        for (int day = valueDate; day <= state.closedThrough(); day++) {
            assess(state, account, settings, day);
        }
    }

    private static void assess(ReplayState state, Account account, LedgerSettings settings, int day) {
        if (state.feeAssessment(account.accountId(), day) != null) {
            return; // Retain prior charges and unsupported assessments, including after reversal.
        }
        Money balance = ReplayResult.balanceOn(account, day, state.ledgerEntries());
        if (balance.amount().signum() >= 0) {
            return;
        }
        if (account.currency() != settings.overdraftFee().currency()) {
            state.recordFeeAssessment(new FeeAssessment(account.accountId(), day, balance, Optional.empty()));
            return;
        }
        Money fee = settings.overdraftFee();
        state.recordFeeAssessment(new FeeAssessment(account.accountId(), day, balance, Optional.of(fee)));
        if (fee.amount().signum() > 0) {
            state.appendLedgerEntry(new LedgerEntry(account.accountId() + ":overdraft:" + day,
                    account.accountId(), new Money(fee.currency(), fee.amount().negate()), day,
                    new LedgerEntry.Source.OverdraftFee(day)));
        }
    }
}
