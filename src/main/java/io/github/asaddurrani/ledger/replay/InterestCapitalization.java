package io.github.asaddurrani.ledger.replay;

import io.github.asaddurrani.ledger.model.Account;
import io.github.asaddurrani.ledger.model.InterestAccrual;
import io.github.asaddurrani.ledger.model.LedgerEntry;
import io.github.asaddurrani.ledger.model.LedgerSettings;
import io.github.asaddurrani.ledger.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Finalizes revised daily accruals after all inputs and fees, then books their exact sum. */
final class InterestCapitalization {
    private InterestCapitalization() {}

    static void capitalize(ReplayState state, List<Account> accounts, LedgerSettings settings) {
        var entriesBeforeCapitalization = state.ledgerEntries();
        for (Account account : accounts) {
            boolean unresolvedFees = state.hasUnsupportedFeeAssessment(account.accountId());
            Money total = Money.of(account.currency(), "0");
            for (int day = 1; day <= settings.closingDay(); day++) {
                Money balance = ReplayResult.balanceOn(account, day, entriesBeforeCapitalization);
                if (unresolvedFees) {
                    state.recordInterestAccrual(new InterestAccrual(account.accountId(), day, balance, Optional.empty()));
                    continue;
                }
                BigDecimal rounded = balance.amount().max(BigDecimal.ZERO)
                        .multiply(settings.dailyInterestRate())
                        .setScale(account.currency().decimalPlaces(), settings.interestRoundingMode());
                Money accrual = new Money(account.currency(), rounded);
                state.recordInterestAccrual(new InterestAccrual(account.accountId(), day, balance, Optional.of(accrual)));
                total = total.add(accrual);
            }
            if (!unresolvedFees && total.amount().signum() > 0) {
                state.appendLedgerEntry(new LedgerEntry(account.accountId() + ":interest:" + settings.closingDay(),
                        account.accountId(), total, settings.closingDay(),
                        new LedgerEntry.Source.InterestCapitalization(settings.closingDay())));
            }
        }
    }
}
