# Numerical constants

This inventory covers numerical business-rule parameters and implementation
constants. Event amounts, instalment counts, and replay dates are scenario inputs;
tool versions belong in the build configuration.

## Specified values

| Constant | Value | Purpose |
| --- | --- | --- |
| Daily interest rate | 0.04% = 0.0004 | Applied to positive daily closing balances |
| Overdraft fee | AED 25.00 | Fee assessed for a negative AED daily closing balance |
| AED decimal places | 2 | Currency amount and daily accrual precision |
| BHD decimal places | 3 | Currency amount and daily accrual precision |

These values are specified business rules, not implementation choices. Smaller
alternatives would change the required behavior. Minor units derive from currency
precision rather than separate constants.

No BHD overdraft amount or exchange rate is supplied or chosen. Negative-BHD
assessment remains unsupported as documented in `AMBIGUITIES.md`.

## Chosen values

No additional numerical limits or tuning constants have been chosen. Journal
insertion order preserves the event stream without a separate sequence counter.
The accounting window starts at Day 1, following the numbered-day convention;
its closing day is supplied in immutable `LedgerSettings` at construction and
must be at least Day 1. `LedgerSettings.forWindow` supplies the specified AED fee
and daily interest rate above. Fee amounts and rates must be nonnegative; zero
represents no charge or accrual, so no arbitrary positive minimum is imposed.

Rounding and allocation policies are documented in `AMBIGUITIES.md`.
