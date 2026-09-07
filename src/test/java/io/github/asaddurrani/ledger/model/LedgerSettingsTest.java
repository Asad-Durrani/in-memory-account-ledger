package io.github.asaddurrani.ledger.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.asaddurrani.ledger.InMemoryLedger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class LedgerSettingsTest {
    @Test
    void windowFactoryUsesTheSpecifiedFeeAndDailyRateWithHalfEvenRounding() {
        var settings = LedgerSettings.forWindow(6);

        assertEquals(6, settings.closingDay());
        assertEquals(Money.of(Currency.AED, "25"), settings.overdraftFee());
        assertEquals(new BigDecimal("0.0004"), settings.dailyInterestRate());
        assertEquals(RoundingMode.HALF_EVEN, settings.interestRoundingMode());
    }

    @Test
    void differentlyConfiguredLedgerDoesNotChangeExistingLedgerSettings() {
        var accounts = List.of(new Account("ACC-001", Money.of(Currency.AED, "0")));
        var originalSettings = LedgerSettings.forWindow(6);
        var original = new InMemoryLedger(accounts, originalSettings);
        var otherSettings = new LedgerSettings(3, Money.of(Currency.AED, "0"),
                BigDecimal.ZERO, RoundingMode.HALF_EVEN);
        var other = new InMemoryLedger(accounts, otherSettings);

        assertEquals(originalSettings, original.settings());
        assertEquals(otherSettings, other.settings());
        assertEquals(6, original.settings().closingDay());
        assertEquals(Money.of(Currency.AED, "25"), original.settings().overdraftFee());
    }

    @Test
    void rejectsInvalidWindowNegativeParametersAndUnsupportedFeeCurrency() {
        assertThrows(IllegalArgumentException.class, () -> LedgerSettings.forWindow(0));
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerSettings(6, Money.of(Currency.AED, "-1"),
                        BigDecimal.ZERO, RoundingMode.HALF_EVEN));
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerSettings(6, Money.of(Currency.AED, "25"),
                        new BigDecimal("-0.0004"), RoundingMode.HALF_EVEN));
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerSettings(6, Money.of(Currency.BHD, "25"),
                        BigDecimal.ZERO, RoundingMode.HALF_EVEN));
    }
}
