# In-Memory Account Ledger

A Java project for an in-memory AED/BHD account ledger with value-dated postings,
authorization holds, overdraft fees, and daily interest.

The currency and immutable money types are implemented with exact `BigDecimal`
arithmetic and behavioral tests. Event processing and scenario replay are not yet
available.

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
`target/surefire-reports/` when tests are present.

The current tests cover currency precision, exact arithmetic, numerical equality,
negative balances, and rejection of mixed-currency operations.

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
