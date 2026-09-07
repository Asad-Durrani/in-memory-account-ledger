# In-Memory Account Ledger

A Java project for an in-memory AED/BHD account ledger with value-dated postings,
authorization holds, overdraft fees, and daily interest.

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

Balances and authorization states deliberately use different time perspectives:
revised accounting balances versus original operational decisions. In particular,
do not interpret the revised Day 2 balance as the balance used to approve Auth-A.
No finalization of shortened replays is used to construct the report.

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
and BHD 0.008. The ordinary suite verifies these results; the separately required
intentionally failing design test has not yet been added.

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
