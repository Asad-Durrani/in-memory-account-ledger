# Rejected claims and approaches

Letters A–H identify the acceptance criteria in their listed order. B, F, G, and H
are rejected for the reasons below. F depends on the selected fee-retention policy.

## Accepted criteria

| Criterion | Reason |
| --- | --- |
| A — Day 2 balance is AED -370.00 before fees, evaluated at end of Day 5 | AED 1,200.00 - AED 950.00 - AED 620.00 = AED -370.00; E9 has not yet been processed. |
| C — Accept Auth-A's Day 4 settlement | Auth-A is approved and active when E5 is processed; later backdating does not rewrite that decision. |
| D — Reject settlement for an unknown authorization | E6 has no valid authorization reference and produces no debit posting. |
| E — An approved Auth-B hold reduces available balance only | Holds do not change ledger balance. This is conditional: E8 is actually rejected for insufficient available funds. |

## B — E7 causes exactly one overdraft fee, on Day 2

Rejected. Closing balances include all postings with value dates on or before
the accounting day, so E7 affects subsequent days as well as Day 2.

With the assessment timing documented in `AMBIGUITIES.md`, the balances before
each day's fee, including earlier days' fees, are:

| Day | Balance before that day's fee | Fee | Closing balance |
| --- | ---: | ---: | ---: |
| 2 | AED -370.00 | AED 25.00 | AED -395.00 |
| 3 | AED 5.00 | AED 0.00 | AED 5.00 |
| 4 | AED -180.00 | AED 25.00 | AED -205.00 |
| 5 | AED -205.00 | AED 25.00 | AED -230.00 |

Before E9, three fees have been assessed, totaling AED 75.00.

## F — After E9, all balances and fees return to their pre-E7 values

Rejected under the fee-retention policy documented in `AMBIGUITIES.md`. E9 adds
an AED 620.00 credit with value date Day 2; the AED 75.00 in booked fees remains.
Day 5's reconstructed closing balance becomes AED 390.00 rather than AED 465.00,
excluding interest capitalization.

The requirements do not explicitly settle fee compensation. Append-only permits
refunds through new credits, so it alone does not disprove this claim. This
rejection depends on the explicit policy to retain fees without refunds.

## G — Each of E10's three instalments must be BHD 3.334

Rejected. Three postings of BHD 3.334 total BHD 10.002, exceeding the supplied
BHD 10.000 credit by BHD 0.002. Exact equality between all three instalments is
impossible at BHD precision while conserving that total.

Using the allocation convention in `AMBIGUITIES.md`, post BHD 3.334, BHD 3.333,
and BHD 3.333. Conservation requires distributing the remainder; placing it in
the first instalment is the selected allocation convention.

## H — Discard a difference between rounded accruals and the capitalized total

Rejected. The requirements explicitly state that rounded daily accruals must sum
exactly to the capitalized total. Capitalize their sum; do not independently round
an aggregate and discard the difference.

After E1–E10, including retained fees and using Day 6's balance before
capitalization, half-even rounding gives AED daily accruals of 0.10, 0.09, 0.25,
0.17, 0.16, and 0.16, totaling AED 0.93.
The exact unrounded accruals sum to AED 0.918, which rounds to AED 0.92 instead.
Using AED 0.92 would violate the required equality. The rejection follows directly
from the sum requirement and does not depend on choosing this rounding mode.

## Approach considered and rejected — truncating daily interest

Truncating each positive daily accrual to currency precision was considered during
design. This would satisfy the sum requirement if
capitalization used the sum of truncated accruals, but would reduce positive
interest whenever digits were discarded, even when the next higher amount was
closer. Half-even was selected for nearest rounding without a fixed direction on
ties. Truncation was rejected on policy grounds rather than for violating an
explicit requirement. It was not implemented.
