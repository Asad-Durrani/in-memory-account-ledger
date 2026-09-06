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

No additional numerical limits or tuning constants have been chosen. Any introduced
during implementation must include a rationale and explain why a materially
smaller value was not selected. Rounding and allocation policies are documented in
`AMBIGUITIES.md`.
