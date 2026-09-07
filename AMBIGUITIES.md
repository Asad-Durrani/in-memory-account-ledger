# Ambiguities

This document records unspecified domain policies, the selected interpretations,
their alternatives, and observable consequences. Cases outside the six-day
scenario are identified separately.

Resolved status refers to policy decisions, not completed implementation. Financial
event processing remains unimplemented; scenario consequences below are expected
results derived from the selected policies.

## Overdraft reassessment timing after a backdated posting

Status: resolved.

### What is ambiguous

The rules specify the assessment frequency (once per day), the basis (negative
closing ledger balance), and the fee's value date (the day assessed). They do not
explicitly specify when a backdated posting triggers reassessment of closed days:
immediately during event processing or at a subsequent day-close boundary.

Reevaluating all affected accounting days follows from the balance and fee rules;
that scope is not itself an ambiguity.

### Plausible interpretations

- Reassess affected closed days immediately after the backdated posting, before
  processing the next input event; assess the current day at its normal close.
- Defer reassessment of affected closed days until the current day's close,
  together with assessment of the current day.

### Chosen interpretation and rationale

Reassess affected, already-closed days immediately after processing a backdated
monetary posting, proceeding chronologically so earlier fee postings contribute
to later daily balances. Complete this reassessment before the next input event.
Assess the current day at its normal close, after that day's inputs.

This corrects completed days using newly available information before subsequent
operational decisions, while avoiding a closing-balance assessment of the current
day before its inputs have finished. It does not retroactively change operational
decisions already made or permit a second fee for an already-assessed day.

### Observable consequence

After E7, reassess Days 2–4 before processing E8. Book AED 25.00 for Day 2 and
AED 25.00 for Day 4; Day 3 remains positive at AED 5.00. Before E8, the ledger
balance is therefore AED -205.00, compared with AED -155.00 if reassessment were
deferred. Auth-B is rejected under either timing interpretation.

At Day 5 close, assess its AED 25.00 fee, producing an AED -230.00 closing balance
before E9. The three fees total AED 75.00. Assessment timing changes when fees
become visible to subsequent processing, even though both interpretations yield
the same fees and authorization outcome in this scenario by Day 5 close.

## Previously booked fees after reversal

Status: resolved.

### What is ambiguous

The requirements specify append-only history but do not define whether a reversal
must compensate fees previously triggered by the reversed debit. Append-only
prohibits deleting fees; it does not prohibit refunding them through new credits.

### Plausible interpretations

- Preserve legitimately assessed fees and reverse only the referenced debit.
- Preserve the fee postings but append compensating credits when the reversal
  removes the negative balances that triggered them.

### Chosen interpretation and rationale

Preserve the booked fees without compensating credits. E9 offsets E7's AED 620.00
debit, value-dated Day 2. Fees were valid when assessed using the information then
available; a fee-refund policy would be an additional business rule not defined in
the requirements. This is a policy choice, not a consequence of append-only alone.

### Observable consequence

The fees for Days 2, 4, and 5 remain, totaling AED 75.00. After E9, Day 5's
reconstructed closing balance is AED 390.00, compared with AED 465.00 without E7
and its fees. These amounts exclude interest capitalization. Claim F is rejected
under this policy; a compensating-refund policy would change that conclusion.

## Daily interest recalculation before capitalization

Status: resolved.

The requirements define daily interest on positive closing balances and capitalization
at the end of Day 6, but do not state whether backdated postings revise earlier
accruals. Plausible interpretations are to preserve accruals calculated at each
original day close or to recalculate them from revised accounting balances.

Decision: recalculate affected daily accruals before capitalization, using revised
closing balances including retained fees. Apply the same rule to backdated debits
and reversal credits. Treat daily accruals as unbooked calculations; only the
capitalized total becomes a financial posting. This ties interest to value-dated
balances without mutating booked records. Accrual representation is a design
choice rather than an explicit requirement.

Consequence: E7 reduces earlier accruals; E9 restores accruals on the resulting
positive balances, reduced by retained fees. Half-even rounding and a Day 6 balance
before capitalization yield daily AED accruals of 0.10, 0.09,
0.25, 0.17, 0.16, and 0.16, totaling AED 0.93. Complete E1–E10 and daily fee
assessments before finalizing accruals. Calculate Day 6's interest before appending
one capitalization credit per account, value-dated Day 6, equal to the sum of its
rounded daily accruals.

## Interest rounding mode

Status: resolved.

The requirements specify currency precision but not the rounding mode. Alternatives
considered were half-even, half-up, half-down, and truncation.

Decision: round each day's positive interest accrual to the account currency's
precision using round-to-nearest, ties-to-even (`HALF_EVEN`). Capitalize the exact
sum of those rounded amounts. Nearest rounding minimizes the rounding error for
each accrual; ties-to-even avoids always rounding exact ties in one direction.
This is a design policy rather than a prescribed banking convention.

Consequence: AED 0.125 rounds to AED 0.12, AED 0.135 to AED 0.14, and AED 0.129
to AED 0.13. For the scenario balances above, half-even produces AED 0.93 in total
interest; truncation would produce AED 0.90. The same rounding
rule applies to BHD at three decimal places. This decision concerns daily interest,
not how instalment remainders are allocated.

## Instalment remainder allocation

Status: resolved.

E10 requires three equal instalments totaling BHD 10.000, but exact
equality is impossible at three decimal places. The requirements do not specify which
instalment receives the remainder. Allocating it to the earliest or latest
instalments would both conserve the total.

Decision: divide the total in integer minor units by the instalment count. Give
each instalment the quotient, then distribute the remaining units one per
instalment in order, starting with the earliest. This conserves the total, keeps
amounts within one minor unit of each other, and gives a deterministic ordering
without additional allocation state. Choosing the earliest rather than latest
instalments is an allocation convention rather than an explicit requirement.

Consequence: E10's 10,000 BHD minor units divide into three shares of 3,333 with
one unit remaining. Post BHD 3.334, BHD 3.333, and BHD 3.333, all value-dated
Day 5, totaling BHD 10.000. The remainder's position changes individual posting
amounts but not daily balances or interest. Do not independently round each share
using the daily interest rounding policy.

## Overdraft assessment for a BHD account

Status: handling defined; BHD overdraft assessment is unsupported.

The specified overdraft fee is AED 25.00 per account, but ACC-002 holds
BHD. Neither a BHD fee amount nor an exchange-rate policy is defined. A separate
BHD fee or a converted AED fee would require additional business input; silently
treating the fee as BHD 25.000 or waiving it would also introduce a policy.

Decision: apply the specified fee to AED accounts. If assessment encounters a
negative BHD closing balance, raise a domain exception such as
`UndefinedOverdraftFeePolicy`, identifying the account, currency, and accounting
day. Do not book a guessed fee or report that assessment completed successfully.
The exception denotes incomplete assessment, not rejection of the debit: preserve
the input and any valid debit posting already recognized.

Consequence: the supplied replay is unaffected because ACC-002 never has a
negative closing balance. A negative-BHD scenario cannot complete fee assessment
until a policy is supplied. This is a candidate for the required intentionally
failing test, demonstrating unsupported assessment without inventing an expected
fee amount. A test expecting the exception would instead verify the chosen error
behavior and pass.

## Input amounts exceeding currency precision

Status: resolved.

Currency precision is specified, but handling of excess input digits is not.
Rounding them would silently change the supplied amount; rejecting every extra
digit would also reject harmless trailing zeros.

Decision: normalize amounts to currency precision without rounding. Accept values
that are exactly representable, including extra trailing zeros, and reject values
that would lose a nonzero fractional amount. The money value type supports zero
and negative amounts so it can represent ledger balances and arithmetic results.
Event-specific amount constraints belong to event processing.

Consequence: AED 10.000 becomes AED 10.00; AED 10.001 is rejected. BHD 10.001 is
valid. The supplied scenario is unaffected. Interest rounding remains a separate,
explicit calculation performed before constructing a posted monetary amount.

## Corrections received after capitalization

Status: outside scope.

Corrections arriving after interest capitalization are outside the six-day replay.
A future extension would need to choose between appending interest adjustments
and leaving finalized accruals unchanged. Neither behavior is defined here.
Pre-capitalization recalculation does not authorize changing an existing posting.

## Settings changes during a ledger's lifetime

Status: fixed settings; policy versioning is outside scope.

The scenario supplies a single fee and daily interest rate but does not define
changes to those parameters over time. Applying a new rate or fee to existing
history could change replay results and conflict with already recognized entries.
Alternatives are a fixed policy for the entire ledger or versioned policies with
explicit effective dates and rules for backdated activity.

Decision: supply immutable `LedgerSettings` at ledger construction and retain them
for its lifetime. The accounting window, overdraft fee, daily interest rate, and
interest rounding mode cannot be replaced during recording or replay. The scenario
uses AED 25.00, a daily rate of 0.0004, and `HALF_EVEN` rounding. No BHD fee is added.

Supporting changes within the same ledger would require policy versioning and a
decision about which version applies to backdated entries, fee reassessments, and
interest recalculations. Those capabilities are outside this exercise. Separate
ledger instances may be constructed with different settings; doing so does not
change the policy or history of an existing instance.
