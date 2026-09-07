# Numerical constants

## Required values

| Constant | Value | Why this value, not half |
| --- | --- | --- |
| Daily interest rate | 0.04% = 0.0004 | Specified rate; 0.02% would halve unrounded accruals. |
| Overdraft fee | AED 25.00 | Specified charge; AED 12.50 would undercharge each assessment. |
| AED decimal places | 2 | Required precision; one decimal place cannot represent fils. |
| BHD decimal places | 3 | Required precision; fewer places cannot represent all BHD minor units. |
| First accounting day | 1 | The supplied window begins at Day 1; fractional day labels have no meaning in this daily model. |
| Scenario closing day | 6 | The supplied window ends on Day 6; ending on Day 3 would omit events and capitalize early. |

The six-day window is a scenario input, not a hard-coded limit on the ledger.
Opening balances, event amounts, and the three-instalment count are copied from
the supplied event stream rather than selected numerical parameters.

## Derived values and conventions

- AED 0.01 and BHD 0.001 are the minor units derived from currency precision.
  Half a minor unit is not representable in stored money.
- Zero is the boundary for overdraft assessment and positive-balance interest.
  Authorization may leave exactly zero available. These boundaries follow the
  rules; halving zero changes nothing.
- Instalment positions start at 1. A remainder is distributed one whole minor
  unit at a time to the earliest positions, conserving the submitted total.
- At least one instalment is required, and every share must contain at least one
  minor unit. Fractional counts are meaningless; zero-valued transfers are
  excluded by policy.

Daily interest uses half-even rounding. Input money accepts only exact currency
precision. Neither policy introduces an additional numerical rate or tolerance.

## Configuration limits

The closing day must be at least 1. Fee amounts and interest rates must be
nonnegative; zero permits a configuration with no charge or accrual. No arbitrary
positive minimum, maximum balance, rounding tolerance, or tuning constant is set.
No BHD overdraft fee or exchange rate is chosen because neither is supplied.
