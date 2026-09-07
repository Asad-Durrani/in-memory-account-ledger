# Rejected claims and approaches

Letters follow the acceptance criteria in `ORIGINAL_PROMPT`. Reject B, F, G, and H;
F depends on the [selected fee policy](AMBIGUITIES.md#fees-after-reversal).
The calculations below are policy-derived expectations.

## B — E7 causes exactly one overdraft fee, on Day 2

Rejected: backdating affects subsequent closing balances too. With chronological
reassessment, including preceding fees:

| Day | AED balance before that day's fee | AED fee |
| --- | ---: | ---: |
| 2 | -370.00 | 25.00 |
| 3 | 5.00 | 0.00 |
| 4 | -180.00 | 25.00 |
| 5 | -205.00 | 25.00 |

Three fees total AED 75.00 before E9.

## F — E9 restores all balances and fees to their pre-E7 values

Rejected under the fee-retention policy: E9 offsets AED 620.00 but leaves the
AED 75.00 fees. Day 5 becomes AED 390.00 instead of AED 465.00, before interest.
Append-only permits refunds through new credits, so this rejection is a policy
consequence, not an unconditional implication of the specification.

## G — Each instalment is BHD 3.334

Rejected: three such entries total BHD 10.002, creating BHD 0.002. Use 3.334,
3.333, and 3.333 to conserve BHD 10.000. Exact equality is impossible at BHD precision.

## H — Discard the interest rounding remainder

Rejected: the specification explicitly requires capitalization to equal the sum
of rounded daily accruals. Under the selected policies those accruals sum to
AED 0.93; rounding their unrounded aggregate instead gives AED 0.92. Capitalize
the daily sum. Discarding a difference violates the requirement.

## Replaced approach — account-local journals

Initially implemented account-local journals plus a shared sequence counter to
recover global submission order. Replaced them with one ledger-owned event journal:
it preserves the required order directly, including nonmonotonic processing dates,
without coordinating separate histories.
