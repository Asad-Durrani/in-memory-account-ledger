# Numerical constants

This inventory distinguishes specified domain values from design constants.
It covers the policies reviewed to date and will expand during implementation.

## Specified values

| Constant | Value | Purpose |
| --- | --- | --- |
| Daily interest rate | 0.04% = 0.0004 | Applied to positive daily closing balances |
| Overdraft fee | AED 25.00 | Fee assessed for a negative AED daily closing balance |
| AED decimal places | 2 | Currency amount and daily accrual precision |
| BHD decimal places | 3 | Currency amount and daily accrual precision |
| Scenario duration | 6 accounting days | Interest capitalizes at end of Day 6 |
| E10 credit | BHD 10.000 | Total conserved across its instalment postings |
| E10 instalment count | 3 | Number of postings requested for the credit |

These values are supplied, not selected. Smaller alternatives would change the
specified domain behavior. Capitalization follows the complete E1–E10 replay and
Day 6's accrual calculation; each account receives one credit equal to the sum of
its rounded daily accruals, value-dated Day 6.

No BHD overdraft amount or exchange rate is supplied or chosen. Negative-BHD
assessment remains unsupported as documented in `AMBIGUITIES.md`.

## Chosen values

No implementation constants have been introduced. Daily interest uses `HALF_EVEN`
as the chosen rounding policy; its alternatives and rationale are documented in
`AMBIGUITIES.md`. A rounding mode is not a numerical magnitude, so a smaller or
halved value does not apply. Illustrative balances and accruals in the decision
documents are derived results, not additional chosen constants.

Instalment remainders are allocated to the earliest instalments. Each receives
at most one extra minor unit. That indivisible unit is dictated by currency
precision: BHD 0.001 here, so half a unit cannot be posted. The resulting E10
amounts (BHD 3.334, BHD 3.333, BHD 3.333) are derived, not chosen constants.
