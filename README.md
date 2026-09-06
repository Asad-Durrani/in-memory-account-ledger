# In-Memory Account Ledger

A Java project for an in-memory AED/BHD account ledger with value-dated postings,
authorization holds, overdraft fees, and daily interest.

The repository currently contains the build setup and domain documentation.
Ledger implementation, domain tests, and scenario replay are not yet available.

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

At this setup stage, Maven reports no tests and may emit missing-output-directory
and empty-JAR warnings. A successful build validates the configuration only.

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
