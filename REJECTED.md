# Rejected acceptance criteria

Criteria are numbered in their supplied order. Criteria 2, 6, 7, and 8 are
rejected below; criterion 6 depends on the explicitly selected fee-retention
policy.

## 2 — E7 causes exactly one overdraft fee to be assessed, on Day 2

Rejected. A backdated debit affects every subsequent closing balance, not only
its value date. Reassessing chronologically and including earlier fees gives:

| Day | AED balance before that day's fee | AED fee |
| --- | ---: | ---: |
| 2 | -370.00 | 25.00 |
| 3 | 5.00 | 0.00 |
| 4 | -180.00 | 25.00 |
| 5 | -205.00 | 25.00 |

Days 2, 4, and 5 each require a fee. The total is AED 75.00 before E9.

## 6 — After E9, all balances and fees return to their pre-E7 values

Rejected under the selected policy: reversing a debit does not refund its
previously assessed fees. E9 credits AED 620.00 to offset E7, leaving the three
fees totaling AED 75.00. Day 5's revised balance is AED 390.00 rather than
AED 465.00, before interest.

The specification provides no fee-refund rule. Append-only history would permit
separate compensating credits, so this rejection follows from retaining valid
assessments, not from a claim that append-only forbids refunds.

## 7 — The three BHD instalments in E10 must each be BHD 3.334

Rejected. Three BHD 3.334 postings total BHD 10.002, exceeding E10's credit by
BHD 0.002. Split the credit into BHD 3.334, 3.333, and 3.333 to conserve
BHD 10.000. Exactly equal shares are impossible at the required precision.

## 8 — If the rounded daily interest accruals do not sum to the capitalized total, the remainder is discarded

Rejected. Capitalization must equal the exact sum of rounded daily accruals.
The AED daily amounts are 0.10, 0.09, 0.25, 0.17, 0.16, and 0.16, totaling
AED 0.93. Independently rounding the unrounded aggregate of AED 0.918 would
produce AED 0.92. Book AED 0.93; discarding the difference violates the explicit
sum requirement.

## Abandoned approach — account-local journals

Initially implemented separate account journals and a shared sequence counter to
recover global submission order. Replaced them with one ledger-owned event journal,
which preserves the supplied order directly, including processing dates that move
backward, without coordinating separate histories.
