# In-Memory Account Ledger

An in-memory Java account ledger for AED and BHD, with value-dated postings,
authorization holds, overdraft fees, and daily interest. It has no web layer,
persistence, UI, or database.

The core replays credits, debits, authorizations, settlements, debit reversals,
and instalment credits in submission order. Exact `BigDecimal` money preserves
currency precision; replay assesses overdraft fees and capitalizes daily interest.

## Requirements

- JDK 21 or newer, with `java` on `PATH` or `JAVA_HOME` set to the JDK.
- Internet access for the first build to download Maven and dependencies.
- On macOS/Linux, a POSIX shell, `curl` or `wget`, and `unzip`.
- On Windows, PowerShell for the wrapper's initial download.

Maven is downloaded through the included wrapper. Build and dependency versions
are pinned in the wrapper configuration and `pom.xml`.

## Build and test

Run from the repository root:

```sh
./mvnw test
./mvnw clean verify
```

On Windows, replace `./mvnw` with `.\mvnw.cmd`.

`test` runs JUnit Jupiter tests. `clean verify` rebuilds, tests, and packages the
project. Build output goes to `target/`; test reports go to
`target/surefire-reports/`.

Tests cover money precision, immutable histories, all event types, validation,
backdated fees, interest rounding and conservation, repeatable replay, and daily
reporting of the complete scenario.

## Run the six-day report

After building, run:

```sh
java -cp target/classes io.github.asaddurrani.ledger.report.ScenarioReport
```

The runner submits E1 through E10 in their original order, including Day 5's E10
following Day 6's E9. It prints both accounts for each of Days 1 through 6:

- **Closing:** revised value-dated balance after the entire replay. Earlier days
  include E7/E9 and retained fees; Day 6 also includes capitalized interest.
- **Fees:** assessments attributed to their accounting day, with the negative
  balance observed when assessed. Those assessments remain even when the revised
  closing balance is positive after E9.
- **Interest:** rounded daily accrual, before the single Day 6 capitalization.
- **Authorizations:** latest recorded decision through that processing day.
  Auth-A is approved on Days 2–3 and settled from Day 4; Auth-B is rejected from
  Day 5. Backdating does not rewrite these decisions.
- **Errors:** that processing day's rejected events. E6 reports unknown Auth-Z;
  E8 reports insufficient available balance. `none` means no item applies.
  Rejections for unknown accounts or processing days outside the displayed range
  appear once in an **Unassigned errors** section, with their account and processing
  day. The section is omitted when there are no such errors.

Balances and authorization states deliberately use different time perspectives:
revised accounting balances versus original operational decisions. In particular,
do not interpret the revised Day 2 balance as the balance used to approve Auth-A.

Expected revised closing balances:

| Day | ACC-001 (AED) | ACC-002 (BHD) |
| --- | ---: | ---: |
| 1 | 250.00 | 0.000 |
| 2 | 225.00 | 0.000 |
| 3 | 625.00 | 0.000 |
| 4 | 415.00 | 0.000 |
| 5 | 390.00 | 10.000 |
| 6 | 390.93 | 10.008 |

Retained fees total AED 75.00 across Days 2, 4, and 5. Interest totals AED 0.93
and BHD 0.008. The ordinary suite verifies these results.

## Acceptance criteria

Numbered in the order supplied in [ORIGINAL_PROMPT](ORIGINAL_PROMPT).

| # | Criterion | Outcome and evidence |
| --- | --- | --- |
| 1 | Day 2 balance evaluated at end of Day 5, before fees, is AED -370.00 | Accepted; [fee tests][fee-tests] verify the Day 2 pre-fee assessment basis. |
| 2 | E7 causes exactly one fee, on Day 2 | Rejected; Days 2, 4, and 5 incur AED 75.00 total. See [REJECTED.md, criterion 2](REJECTED.md) and [fee tests][fee-tests]. |
| 3 | Accept Auth-A's settlement | Accepted; [authorization tests][auth-tests] verify E5 settles AED 185.00 and releases the AED 200.00 hold. |
| 4 | Reject settlement of an unknown authorization without moving funds | Accepted; [authorization tests][auth-tests] verify E6 is rejected without a debit. |
| 5 | If approved, Auth-B reduces available balance only | Accepted; [authorization tests][auth-tests] verify approved holds reserve funds without ledger entries. Auth-B itself is rejected in the supplied stream. |
| 6 | E9 restores all balances and fees to pre-E7 values | Rejected under the fee-retention policy; AED 75.00 remains charged. See [REJECTED.md, criterion 6](REJECTED.md) and [fee tests][fee-tests]. |
| 7 | Each BHD instalment is 3.334 | Rejected; [instalment tests][instalment-tests] verify 3.334 + 3.333 + 3.333 = 10.000. See [REJECTED.md, criterion 7](REJECTED.md). |
| 8 | Discard any interest remainder | Rejected; [interest tests][interest-tests] verify capitalization equals the sum of rounded daily accruals. See [REJECTED.md, criterion 8](REJECTED.md). |

[fee-tests]: src/test/java/io/github/asaddurrani/ledger/replay/OverdraftAssessmentTest.java
[auth-tests]: src/test/java/io/github/asaddurrani/ledger/replay/AuthorizationReplayTest.java
[instalment-tests]: src/test/java/io/github/asaddurrani/ledger/replay/InstalmentReplayTest.java
[interest-tests]: src/test/java/io/github/asaddurrani/ledger/replay/InterestCapitalizationTest.java

## Intentionally failing design test

Run the design counterexample separately:

```sh
./mvnw -Dtest=IntentionalDesignFailure test
```

Expect one assertion failure and a nonzero exit status. A BHD account goes from
0.000 to -1.000 on Day 1, then receives 100.000 on Day 2. Although its booked
balance recovers to 99.000, interest remains unfinalized through Day 6 because
Day 1's overdraft requires an undefined BHD fee policy. The test asserts that
interest can finalize after recovery and explains the missing currency-specific
fee policy inline.

`IntentionalDesignFailure` deliberately falls outside Surefire's default test
class naming patterns, so `test` and `clean verify` run the normal regression
suite. The explicit command runs the failing assertion.

## Source layout

```text
src/main/java/    Ledger implementation
src/test/java/    Behavioral tests
pom.xml          Java, dependency, and build plugin configuration
.mvn/wrapper/    Pinned Maven distribution configuration
```

## Engineering notes

- [Ambiguities](AMBIGUITIES.md): policy decisions, alternatives, and limitations.
- [Rejected claims](REJECTED.md): acceptance-criterion analysis and rejected approaches.
- [Numerical constants](NUMBERS.md): specified values and design choices.
- [Worklog](WORKLOG.md): timestamped engineering milestones.
