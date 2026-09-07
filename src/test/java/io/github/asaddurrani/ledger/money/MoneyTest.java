package io.github.asaddurrani.ledger.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class MoneyTest {
    @ParameterizedTest
    @CsvSource({
            "AED, 10, 10.00",
            "AED, 10.000, 10.00",
            "AED, -0.010, -0.01",
            "BHD, 10, 10.000",
            "BHD, 10.0000, 10.000",
            "BHD, 0.001, 0.001"
    })
    void normalizesRepresentableAmountsWithoutChangingTheirValue(
            Currency currency, String input, String expected) {
        Money money = Money.of(currency, input);

        assertEquals(currency, money.currency());
        assertEquals(new BigDecimal(expected), money.amount());
    }

    @ParameterizedTest
    @CsvSource({
            "AED, 10.001",
            "AED, -0.001",
            "BHD, 10.0001",
            "BHD, -0.0001"
    })
    void rejectsAmountsThatWouldRequireRounding(Currency currency, String amount) {
        assertThrows(IllegalArgumentException.class, () -> Money.of(currency, amount));
    }

    @ParameterizedTest
    @CsvSource({
            "AED, 0.10, 0.20, 0.30",
            "BHD, 0.001, 0.002, 0.003"
    })
    void addsExactlyWithoutChangingOperands(
            Currency currency, String initialAmount, String addedAmount, String expected) {
        Money initial = Money.of(currency, initialAmount);
        Money addition = Money.of(currency, addedAmount);

        Money result = initial.add(addition);

        assertEquals(Money.of(currency, expected), result);
        assertEquals(Money.of(currency, initialAmount), initial);
        assertEquals(Money.of(currency, addedAmount), addition);
    }

    @ParameterizedTest
    @CsvSource({
            "AED, 0.00, 0.01, -0.01",
            "BHD, 0.000, 0.001, -0.001"
    })
    void subtractionCanProduceAnExactNegativeBalance(
            Currency currency, String initialAmount, String debitAmount, String expected) {
        Money initial = Money.of(currency, initialAmount);
        Money debit = Money.of(currency, debitAmount);

        Money result = initial.subtract(debit);

        assertEquals(Money.of(currency, expected), result);
        assertEquals(Money.of(currency, initialAmount), initial);
        assertEquals(Money.of(currency, debitAmount), debit);
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void numericalEqualityDoesNotDependOnInputScale(Currency currency) {
        Money integerAmount = Money.of(currency, "10");
        Money decimalAmount = Money.of(currency, "10.00");

        assertEquals(integerAmount, decimalAmount);
        assertEquals(integerAmount.hashCode(), decimalAmount.hashCode());
        assertEquals(0, integerAmount.compareTo(decimalAmount));
        assertEquals(Money.of(currency, "0"), integerAmount.subtract(decimalAmount));
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void ordersAmountsNumericallyIncludingNegativeBalances(Currency currency) {
        Money negative = Money.of(currency, "-2");
        Money zero = Money.of(currency, "0");
        Money positive = Money.of(currency, "10");

        assertTrue(negative.compareTo(zero) < 0);
        assertTrue(positive.compareTo(zero) > 0);
        assertTrue(zero.compareTo(zero) == 0);
    }

    @Test
    void currenciesCannotBeMixedOrTreatedAsEqual() {
        Money aed = Money.of(Currency.AED, "10");
        Money bhd = Money.of(Currency.BHD, "10");

        assertNotEquals(aed, bhd);
        assertThrows(IllegalArgumentException.class, () -> aed.add(bhd));
        assertThrows(IllegalArgumentException.class, () -> aed.subtract(bhd));
        assertThrows(IllegalArgumentException.class, () -> aed.compareTo(bhd));
    }

    @Test
    void requiresBothCurrencyAndAmount() {
        assertThrows(NullPointerException.class, () -> new Money(null, BigDecimal.ZERO));
        assertThrows(NullPointerException.class, () -> new Money(Currency.AED, null));
    }
}
