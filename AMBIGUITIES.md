# Ambiguities

The following specification gaps require choices that affect monetary results,
authorization decisions, or interpretation of the daily report.

## Backdated fee assessment and day boundaries

The specification requires daily overdraft assessment but does not say when a
backdated posting triggers reassessment. Reassess affected closed days immediately,
in accounting-day order, including earlier fees in later balances. Assess the
current day when processing advances to a later day, then close remaining days
after the event stream. A late input does not move the closing boundary backward.
This preserves the supplied E1–E10 order, including E10 after E9.

After E7, immediate reassessment books Day 2 and Day 4 fees, making AED -205.00
available before E8. Deferring reassessment until Day 5 closes would leave
AED -155.00 at that point. Auth-B is rejected under either interpretation.

## Fees after reversal

No rule specifies whether reversing a debit also refunds fees it triggered.
Retain assessed fees because E9 reverses E7's debit and supplies no fee refund.
E9 restores AED 620.00 while AED 75.00 in fees remains.

Append-only history does not itself require retention: separate refund credits
would also preserve every original entry. Fee retention is an explicit policy.

## Interest after backdated corrections

The specification does not say whether previously calculated daily accruals are
fixed or revised when backdated entries arrive. Recalculate them before
capitalization using the completed value-dated history, including retained fees.
This makes interest follow the corrected ledger rather than superseded balances.

Calculate Day 6's accrual before its capitalization credit and capitalize the
exact sum of rounded daily accruals. The resulting credits are AED 0.93 and
BHD 0.008.

## Rounding and excess precision

Currency precision is specified, but rounding mode and treatment of excess input
digits are not. Use `HALF_EVEN` for daily interest: AED 0.125 rounds to 0.12,
whereas `HALF_UP` would give 0.13. Half-even avoids consistently rounding ties
upward.

For input money, accept exact normalization and reject amounts requiring loss of
value. AED 10.000 is accepted; AED 10.001 is rejected. This keeps submitted
amounts intact while applying explicit rounding to calculated interest.

## Instalment remainder allocation

Three exactly equal amounts cannot total BHD 10.000 at three-decimal precision.
Allocate integer minor units equally and distribute remaining units to the
earliest instalments, producing BHD 3.334, 3.333, and 3.333. Giving the remainder
to the final instalment would also conserve the total; earliest-first fixes a
deterministic ordering.

Require every instalment to be positive, rejecting counts greater than the total
in minor units. Zero-valued shares could conserve the total, but the chosen policy
requires each instalment to represent an actual transfer.

## Event identity and retries

Event IDs are unique across the submitted stream. The first nonblank ID is
reserved even if its event is rejected. Repeated IDs are rejected; corrected
submissions require a new ID.

## Authorization decisions after backdating

The specification does not say whether a later correction revisits an earlier
authorization decision. Preserve the decision made when the authorization was
processed, using the then-known ledger balance through its processing day minus
active holds. Holds take effect immediately, including for future value dates.

Thus Auth-A's approval remains valid after E7 changes earlier balances. Settlement
uses that approval without repeating the available-balance check; E9 likewise
does not turn Auth-B's rejection into an approval.

## Settlement amount and hold lifecycle

E5 settles AED 185.00 against a hold of AED 200.00, but the specification does not
say whether the remainder stays held or permits another settlement. Treat one
accepted settlement as final and release the entire hold. Auth-A therefore leaves
no AED 15.00 residual hold. Reject subsequent settlements and amounts above the
original hold rather than treating the authorization as additional spending
permission.

Authorization IDs are scoped to an account and cannot be reused, including after
rejection or settlement. The unspecified identity lifecycle is resolved this way
to keep later settlement references unambiguous.

## Reversal scope

The event stream demonstrates reversal of a direct debit, without defining
reversals of other event types. Support full reversal of a previously accepted
direct debit on the same account. Credit and settlement reversals are rejected;
they need additional policies, such as whether reversing a settlement restores
its authorization hold.

## BHD overdraft fee

Only an AED fee is supplied; no BHD fee or exchange rate is defined. A negative
BHD closing balance produces an unsupported assessment while retaining the valid
debit. No fee is guessed or silently waived. Interest for that account remains
unfinalized because its fee-adjusted balances are unknown, even if later credits
restore a positive balance. The supplied event stream never requires a BHD fee.

## Replay results and finalization

The prompt does not define replay after additional submissions. Each replay builds
an independent projection; it does not commit or freeze the window. Submitted
events remain append-only and earlier results remain immutable. Additional events
may change derived fee and interest postings in the new projection.

## Daily report perspective

The report requirement does not specify whether earlier balances represent the
original day close or the completed replay. Report revised value-dated balances
after all inputs and fees, with capitalization affecting Day 6 only.

Report authorization decisions through each processing day, retaining their
submission order. Attribute errors to their input processing day and fees to the
accounting day assessed. The output labels these perspectives so revised balances
are not mistaken for the funds used in historical authorization decisions.
