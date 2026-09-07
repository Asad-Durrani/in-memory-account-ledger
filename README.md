# In-Memory Account Ledger

An in-memory Java ledger that replays the supplied six-day event stream for AED
and BHD accounts.

## Run the suite and report

Requires JDK 21 or newer. The included Maven wrapper downloads Maven and
dependencies on the first run, which requires internet access.

Run from the repository root:

```
./mvnw test
java -cp target/classes io.github.asaddurrani.ledger.report.ScenarioReport
```

On Windows, use `.\mvnw.cmd` instead of `./mvnw`.

## Read the output

The report replays E1–E10 in the supplied order and prints both accounts for each
of Days 1–6:

- **Closing:** revised value-dated ledger balance after the complete replay.
  Day 6 includes capitalized interest.
- **Fees:** charges for that accounting day, with the balance observed at
  assessment. Fees remain after E9, even where the revised balance is positive.
- **Interest:** rounded daily accrual before capitalization.
- **Authorizations:** decisions through that processing day. Auth-A is approved
  on Days 2–3 and settled from Day 4; Auth-B is rejected from Day 5.
- **Errors:** rejected events grouped by processing day. E6 references unknown
  Auth-Z; E8 has insufficient available balance. `none` means no item applies.
  Errors outside the displayed accounts or days appear under **Unassigned errors**.

Closing balances reflect later corrections; authorization states preserve the
original decisions. The revised Day 2 balance is not the balance used to approve
Auth-A.

Expected final balances are **AED 390.93** and **BHD 10.008**. Retained fees total
**AED 75.00** on Days 2, 4, and 5. Capitalized interest totals **AED 0.93** and
**BHD 0.008**.

## Run the intentionally failing test

```
./mvnw -Dtest=IntentionalDesignFailure test
```

This test runs separately from the normal suite and produces one assertion
failure with a nonzero exit status. It demonstrates that a recovered BHD account
cannot finalize interest without a BHD overdraft fee policy. The limitation is
annotated inline in the test.

## Supporting documents

- [AMBIGUITIES.md](AMBIGUITIES.md): policy choices and limitations.
- [REJECTED.md](REJECTED.md): rejected criteria and abandoned approaches.
- [NUMBERS.md](NUMBERS.md): numerical values from the prompt.
- [WORKLOG.md](WORKLOG.md): timestamped development record.
