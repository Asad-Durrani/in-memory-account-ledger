package io.github.asaddurrani.ledger.model;

import io.github.asaddurrani.ledger.money.Currency;
import io.github.asaddurrani.ledger.money.Money;
import java.util.Objects;

/** Account definition; calculated balances and journal history do not belong here. */
public record Account(String accountId, Money openingBalance) {
    public Account {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(openingBalance, "openingBalance");
    }

    public Currency currency() {
        return openingBalance.currency();
    }
}
