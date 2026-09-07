# Ambiguities

Material gaps in the specification and the selected policies. Implementation
progress belongs in [WORKLOG.md](WORKLOG.md).

## Input validity and identity

### Duplicate event IDs

Duplicate handling is unspecified. The first nonblank ID reserves that identity
across accounts, even if the submission is rejected. Reject later occurrences
rather than booking twice or silently ignoring them; corrections need new IDs.
All submissions remain in history. This keeps references unambiguous.

### Posting amounts and dates

Zero and negative posting amounts and authorization holds are unspecified. Require
positive magnitudes; credit/debit type determines the posting sign.
Both dates must be within the configured window. Permit future value dates within
that window, affecting balances only from that date; the specification does not
require value date to precede processing day. Submission order remains authoritative.

### Excess input precision

The specification does not distinguish rounding excess digits from rejecting them.
Accept exactly representable inputs, including trailing zeros; reject any amount
that would lose value. Thus AED 10.000 is accepted and AED 10.001 is rejected.
This avoids silently changing submitted amounts.

### Authorization ID scope and reuse

The specification does not define whether authorization IDs are global or reusable.
Scope them to an account and reject reuse within that account, including IDs of
rejected or settled authorizations. This keeps settlement references unambiguous
while allowing different accounts to use the same ID.

## Authorization and settlement behavior

### Authorization timing

The specification does not define future-dated holds. Apply holds when their
submission is processed, using the known ledger balance through the processing
day and all active holds. This reserves funds immediately. Later backdating does
not revisit approval or rejection; settlement of an approved hold does not repeat
the available-balance check.

### Settlement finality and hold release

The specification does not define partial or repeated settlements. Allow one
settlement per approved authorization and release its entire hold. Reject further
settlements. Auth-A's AED 185 settlement therefore releases its AED 200 hold,
leaving no AED 15 residual hold or second settlement opportunity.

### Settlement above the hold

The specification does not define excess settlement. Accept a positive settlement
amount up to the original hold; reject amounts above it. This limits settlement
to the authorized amount rather than assuming permission for additional spending.

## Accounting policies

### Reversal scope

The prompt demonstrates reversal of a direct debit but does not define other
reversal types. Support only previously accepted direct debits on the same account.
Reject other targets; credit and settlement reversals require separate policies,
including whether a reversed settlement would restore its hold.

### Backdated fee reassessment timing

The rules do not say whether to reassess closed days immediately after a backdated
posting or wait until the current day closes. Reassess immediately, in accounting-day
order, so subsequent decisions use corrected balances including earlier fees.
Assess the current day after its inputs. Never charge an already-assessed day twice.
After E7, this makes AED -205.00 visible before E8, rather than AED -155.00 under
deferred assessment. Auth-B is rejected either way.

### Fees after reversal

The specification does not say whether reversal refunds fees triggered by the
original debit. Retain those fees: they were valid when assessed, and no refund
rule is supplied. Append-only alone does not require this choice; compensating
credits would also preserve history. E9 therefore leaves AED 75.00 in fees.

### Backdated interest recalculation

Earlier daily accruals could remain fixed or be recalculated after backdating.
Recalculate before capitalization so interest follows revised value-dated balances,
including retained fees. Calculate Day 6 interest before its capitalization credit,
then capitalize the sum of rounded daily accruals after all supplied events and
fee assessments. Under these policies, AED interest totals 0.93.

### Interest rounding

Currency precision is specified; rounding mode is not. Choose `HALF_EVEN` for
nearest rounding without consistently rounding ties upward. This is a policy,
not a supplied requirement: AED 0.125 becomes 0.12 rather than 0.13 under `HALF_UP`.

### Instalment remainder

Three exactly equal BHD amounts cannot total 10.000 at three decimal places.
The unspecified choice is where the remainder goes. Allocate extra minor units
to the earliest instalments: 3.334, 3.333, 3.333, all value-dated Day 5. Allocating
the remainder last would also conserve the total; earliest-first is a convention.

Require positive instalments: reject a count exceeding the total's minor units.
Zero-valued shares could conserve the total and count, but represent no transfer;
we choose to make every instalment an actual positive credit. This restriction
is a policy, not a prompt requirement.

### BHD overdraft fee

Only an AED fee is supplied; neither a BHD fee nor an exchange rate is defined.
If a BHD closing balance is negative, report unsupported fee assessment without
inventing a fee or silently waiving it. This does not invalidate the debit.
The supplied scenario never requires a BHD fee.
