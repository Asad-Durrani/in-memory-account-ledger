# Worklog

Timestamped engineering milestones in UTC. Earlier repository setup is recorded
as observed context rather than assigned a retrospective timestamp.

## 2026-09-06 19:28:51 UTC — Planning baseline

- Inspected the cloned repository and found an existing initial commit containing
  `.gitignore`; no implementation exists yet.
- Created the implementation plan to sequence policy review, expected results, project
  setup, small implementation milestones, verification, and required artifacts.
- Left consequential financial interpretations and language selection unresolved.
- Identified backdated overdraft assessment after E7 as the first policy to review.
- No implementation or new commit made during this planning step.

## 2026-09-06 19:41:06 UTC — Overdraft reassessment timing defined

- Recorded the assessment policy in `AMBIGUITIES.md`: reassess affected
  closed days immediately after a backdated monetary posting and assess the current
  day at its close.
- Distinguished the unspecified assessment timing from the required scope of
  reevaluation across affected accounting days.
- Documented alternatives, rationale, and the balances visible before E8; Auth-B
  is rejected under either timing interpretation.
- Updated the plan to reflect this decision and retain the unresolved replay
  day-close boundary question. No implementation or new commit made.

## 2026-09-06 19:49:51 UTC — Fee-retention policy and requirement review

- Reviewed the fee-retention rationale against the requirements and distinguished
  explicit rules from additional policy choices.
- Added fee treatment after reversal to `AMBIGUITIES.md`, with fee retention as
  the chosen policy and compensating refunds as an append-only alternative.
- Created `REJECTED.md` for reviewed claims B and F, explicitly distinguishing
  the fee-rule reasoning from the policy-dependent rejection of claim F.
- Updated the plan. No implementation or new commit made.

## 2026-09-06 20:02:05 UTC — Interest recalculation decision

- Recorded the pre-capitalization recalculation policy in `AMBIGUITIES.md`,
  distinguishing unbooked daily accruals from retained fee postings.
- Left rounding mode, capitalization timing, and the Day 6 interest basis open;
  identified post-capitalization corrections as undefined outside the scenario.
- Added rejection of claim H based on the exact sum requirement; kept
  the AED 0.93 numerical example explicitly conditional on unresolved choices.
- Started `NUMBERS.md` with specified interest-related constants and updated the
  plan. No implementation or new commit made.

## 2026-09-06 21:06:42 UTC — Half-even interest rounding selected

- Selected half-even rounding for daily interest in both currencies after
  comparing tie handling and truncation.
- Documented truncation as a genuinely considered and rejected design approach,
  without claiming it violates the requirements or was implemented.
- Updated `NUMBERS.md`, the illustrative claim H calculation, and the plan to
  reflect the resolved rounding policy. Capitalization timing and the Day 6
  interest basis remain open. No implementation or new commit made.

## 2026-09-06 21:10:13 UTC — Capitalization sequence clarified

- Clarified the end-of-Day-6 sequence: complete the event stream, calculate Day 6's
  accrual, then capitalize accumulated interest.
- Removed the overstated capitalization ambiguity and recorded the sequence as
  design reasoning. Earlier fee assessments retain their defined timing.
- Updated interest examples and references to remove obsolete provisional wording;
  recorded expected interest credits and final balances for later verification.
- No implementation or new commit made.

## 2026-09-06 21:14:09 UTC — Instalment remainder allocation selected

- Recorded the allocation rule: divide in minor units and distribute the
  remainder one unit at a time to the earliest instalments.
- Added claim G's rejection: three BHD 3.334 postings exceed E10's credit; the
  selected split is BHD 3.334, BHD 3.333, and BHD 3.333.
- Updated the numerical inventory and plan. The daily interest rounding rule
  remains separate from instalment allocation. No implementation or new commit made.

## 2026-09-06 21:16:53 UTC — Undefined BHD overdraft policy handling

- Recorded the domain exception for negative-BHD fee assessment, with no
  invented fee and no rollback of the input or valid debit posting.
- Distinguished incomplete assessment from a rejected debit; the supplied
  scenario does not encounter this unsupported case.
- Added the case as a candidate for the required intentionally failing test and
  updated the plan and numerical inventory. No implementation or new commit made.

## 2026-09-06 21:29:13 UTC — Documentation review and scenario policy completion

- Revised engineering documents for external review, replacing conversational
  wording with domain requirements, decisions, rationale, and limitations.
- Recorded completed-replay reporting of ledger and available balances while
  preserving historical authorization outcomes.
- Scoped post-capitalization corrections outside the six-day replay and completed
  the A–H criterion summary, including accepted criteria A, C, D, and E.
- Updated the plan to make the expected-results walkthrough the next milestone.
- Preserved existing worklog timestamps and milestone sequence. No implementation
  or new commit made.

## 2026-09-06 22:05:31 UTC — Self-contained financial explanations

- Replaced references to the implementation plan in the ambiguity, rejection, and
  numerical-constant documents with the relevant calculation sequence and values.
- Retained planning milestones in this log without requiring a separate planning
  document to interpret them. No implementation or new commit made.

## 2026-09-06 22:07:05 UTC — Documentation baseline review

- Excluded local planning and requirement-source files from version control.
- Reviewed the ambiguity decisions, A–H conclusions, numerical inventory, and
  worklog for a documentation baseline. Implementation, runnable verification,
  and build/run guidance remain future milestones.
- Exact-decimal verification identified and corrected the unrounded AED interest
  total from 0.924 to 0.918. The sum of rounded daily accruals remains AED 0.93;
  independently rounding the aggregate still produces AED 0.92.
- No commit or push performed during this review.

## 2026-09-06 22:08:43 UTC — Documentation baseline prepared for version control

- Prepared the policy decisions, acceptance-criterion analysis, numerical
  inventory, worklog, and ignore rules as a documentation milestone.
- Confirmed the repository author identity and destination branch. Local planning
  and requirement-source files remain excluded; implementation has not started.

## 2026-09-06 22:13:58 UTC — Maven and JUnit scaffold

- Confirmed JDK 21 is available and Maven is not installed globally.
- Added a Java 21 Maven project with test-scoped JUnit Jupiter and pinned compiler
  and Surefire plugins, plus the official script-only Maven Wrapper.
- Added source/test directories, ignored build output, and documented commands,
  current implementation status, report semantics, and build version choices.
- Ledger behavior and domain tests remain unimplemented; build validation follows.

## 2026-09-06 22:17:42 UTC — Build scaffold verified

- Verified Maven Wrapper launches Maven 3.9.16 on the available JDK 21.
- A temporary parameterized Jupiter smoke test compiled and ran three cases with
  no failures, confirming test discovery and execution. Removed the smoke test
  after verification so the baseline contains no library-only demonstration tests.
- Ran `./mvnw -B -ntp clean verify` successfully on the final scaffold. No domain
  tests exist yet; missing compiler output and empty-JAR warnings are expected
  until implementation adds production sources.
- Checked whitespace and ignore rules. Build output and local planning/reference
  files remain excluded. No setup commit or push performed.

## 2026-09-06 22:24:26 UTC — Numerical inventory scope refined

- Removed build versions and scenario-specific amounts, counts, and dates from
  the numerical inventory. Retained numerical business rules and a section for
  any future implementation limits or tuning constants.
- Kept instalment amounts and counts as event inputs rather than algorithm
  constants. The specified replay values remain unchanged.
- No implementation, commit, or push performed during this documentation update.

## 2026-09-06 22:26:53 UTC — README cleanup

- Reduced the README to project scope, current availability, prerequisites,
  build/test commands, source layout, and engineering-document links.
- Removed roadmap-style prose and descriptions of replay output that is not yet
  implemented. Kept the absence of domain code and tests explicit.
- No build configuration or implementation changed; no commit or push performed.

## 2026-09-06 22:28:16 UTC — Build setup milestone prepared for commit

- Prepared the verified Java 21, Maven Wrapper, and JUnit setup with source/test
  directories, README, ignore rules, and the refined numerical inventory.
- Confirmed that temporary smoke tests, build output, and local planning/reference
  files are excluded from the commit. Domain implementation remains the next stage.

## 2026-09-06 22:30:35 UTC — Exact money and currency implementation

- Added AED/BHD currency precision and an immutable money value backed by
  `BigDecimal`, with exact arithmetic, canonical scale, and currency-safe comparison.
- Defined excess-precision handling: accept exact normalization, reject amounts
  requiring rounding, and keep interest rounding separate from money construction.
- Added behavioral tests for precision, arithmetic, equality, negative balances,
  required fields, and mixed-currency rejection. Replaced empty-directory markers
  with source files and updated the README. Validation follows.

## 2026-09-06 22:33:13 UTC — Money behavior verified

- Ran `./mvnw -B -ntp -Dtest=MoneyTest test`: 20 cases passed with no failures,
  errors, or skipped cases. Both production types and the test suite compiled
  for Java 21.
- Verified exact AED/BHD arithmetic, normalization without rounding, numerical
  equality, negative results, and currency mismatch handling. Whitespace checks
  passed. Event processing remains the next increment; no commit or push made.

## 2026-09-06 23:29:19 UTC — Input and journal increment

- Inspected the existing money implementation, repository changes, and recorded
  financial decisions. Preserved all existing local changes; no applicable
  `AGENTS.md` files were found.
- Added immutable inputs with event/account IDs, processing day, value date, and
  sealed details for credit, debit, authorization, settlement, referenced reversal,
  and instalment credit. Reversals carry only their reference, not another amount.
- Added append-only account journals and a shared submission sequence to recover
  cross-account input order independently of either date. Returned histories are
  immutable snapshots. Business validation remains a replay responsibility;
  unknown references and duplicate submissions are retained for evaluation.
- Added behavioral coverage for global ordering, account routing, retained inputs,
  and history immutability. Replay, financial postings, and balance projections
  remain for subsequent increments; validation follows.

## 2026-09-06 23:29:59 UTC — Input and journal behavior verified

- Ran `./mvnw -B -ntp test`: all 24 cases passed (20 money cases and 4 journal
  cases), with no failures, errors, or skips.
- Verified cross-account submission order despite nonmonotonic dates, retained
  unknown references and repeated submissions, immutable history snapshots, and
  rejection of incorrectly routed inputs without altering the journal.
- Whitespace checks passed. Documented the submission sequence representation in
  the numerical inventory. No commit or push performed.

## 2026-09-06 23:47:51 UTC — Ledger-owned journal model

- Confirmed that the earlier input/journal files had been removed and that the
  existing money and currency implementation remained. Preserved local changes.
- Added account definitions, typed `EventRecord` inputs, a single ledger-owned
  `Journal`, and immutable `LedgerEntry` values with typed event, fee, or interest
  sources. Accounts derive currency from their opening money amount rather than
  storing a second potentially inconsistent currency field.
- Added `InMemoryLedger.appendToLedger(EventRecord)` and private ledger-entry
  append, with immutable history access. Appending records does not create
  monetary entries. The accounting window is supplied at construction.
- Reserved `replay()` with an explicit unsupported-operation exception pending the
  financial-processing increment. No replay results, fees, holds, capitalization,
  or financial idempotency are implemented or claimed in this model increment.
- Replaced obsolete sequence-counter documentation and recorded the replaced
  account-local journal approach. Added behavioral tests; validation follows.

## 2026-09-06 23:50:29 UTC — Core model verified

- Ran `./mvnw -B -ntp test`: 25 tests passed (20 money cases and 5 ledger model
  cases), with no failures, errors, or skips.
- Verified submission order across accounts and nonmonotonic dates, retention of
  unknown references and repeated inputs, immutable snapshots, protected account
  definitions, and absence of monetary entries when merely recording activity.
- `git diff --check` passed. Financial replay remains unimplemented; the model
  tests do not claim replay correctness. No commit or push performed.

## 2026-09-06 23:51:27 UTC — Domain package organization

- Moved `Account`, `Currency`, `Money`, `EventRecord`, `LedgerEntry`, and `Journal`
  into `io.github.asaddurrani.ledger.model`. Kept `InMemoryLedger` in the main
  ledger package as the coordinating API.
- Moved `MoneyTest` into the matching model test package and updated ledger imports.
  No domain behavior changed. Started a clean test build to verify the new package
  layout without relying on previously compiled classes.

## 2026-09-06 23:52:17 UTC — Package move verified

- Ran `./mvnw -B -ntp clean test`: all 25 tests passed with no failures, errors,
  or skips. Whitespace checks passed. No commit or push performed.

## 2026-09-06 23:55:18 UTC — Shared append-only history abstraction

- Replaced the event-specific `Journal` with `AppendOnlyJournal<T>` and a final
  `InMemoryJournal<T>` implementation. The interface exposes only append and
  immutable insertion-ordered snapshots; stored domain records are immutable.
- Changed both event and monetary histories in `InMemoryLedger` to the interface,
  removing its direct access to the mutable ledger-entry list. Public event append
  and private monetary-entry append retain their existing visibility.
- Added behavioral checks for repeated event records, rejection of snapshot update,
  deletion, insertion, and iterator removal, stable snapshots after monetary append,
  and null rejection without changing history. Validation follows.

## 2026-09-06 23:56:39 UTC — Append-only histories verified

- Ran `./mvnw -B -ntp clean test`: all 28 tests passed, with no failures, errors,
  or skips. The new journal tests exercise both event and monetary record types.
- Whitespace checks passed. Replay remains unimplemented. No commit or push made.

## 2026-09-06 23:59:31 UTC — Immutable ledger settings

- Moved the closing day into immutable `LedgerSettings`, alongside the AED
  overdraft fee, daily interest rate, and interest rounding mode. Added a window
  factory using the specified fee/rate and selected half-even rounding.
- The ledger retains a final settings reference supplied at construction; no
  settings replacement API exists. Account currency precision remains in `Currency`.
- Documented fixed lifetime settings and the need for policy versioning and
  effective-date rules before supporting changes within an existing ledger.
  Negative-BHD fee assessment remains unsupported. Validation follows.

## 2026-09-07 00:29:47 UTC — API and replay structure changes recorded

This entry records the changes completed in this session. Build completion times
below come from the Maven output and are converted to UTC.

- Renamed `InMemoryLedger.appendToLedger(EventRecord)` to `appendEvent` to
  distinguish submitted event history from generated monetary entries. Renamed
  `LedgerEntry.Source.Event` to `Source.InputEvent` and updated test references.
- Initially clarified the unimplemented replay contract to prohibit duplicate
  financial entries. Documented `HALF_EVEN` in `LedgerSettings.forWindow` as a
  selected interpretation because the specification does not define tie-breaking.
  The rounding mode itself was unchanged.
- Verified that patch with `./mvnw clean verify`, completed at
  2026-09-07 00:14:50 UTC: 31 tests passed, with no failures, errors, or skips;
  the JAR was built successfully.
- Subsequently removed the long-lived ledger-entry journal and its accessor from
  `InMemoryLedger`. The ledger now owns only account definitions, fixed settings,
  and submitted append-only event history.
- Changed `replay()` to return an immutable `ReplayResult` through package-private
  `LedgerReplay`. Each invocation creates a fresh package-private `ReplayState`
  with its own append-only ledger-entry journal. `ReplayResult` defensively copies
  the resulting entries.
- Documented insertion-order replay and reconstruction of the entries that would
  have been booked, with later entries never mutating or removing earlier ones.
  Empty history returns an empty result. Nonempty history explicitly throws
  `UnsupportedOperationException` because financial event interpretation remains
  unimplemented.
- Updated existing tests for the removed accessor and added coverage for repeated
  empty replay, rejection of nonempty replay without changing submitted history,
  isolation between replay states, stable earlier results, and defensive copying.
  These structural checks do not establish deterministic financial replay yet.
- Verified the structural patch with `./mvnw clean verify`, completed at
  2026-09-07 00:24:47 UTC: 35 tests passed, with no failures, errors, or skips;
  the JAR was built successfully. `git diff --check` passed for both patches.
- Preserved existing model semantics, including account currency derived from
  opening balance, exact `BigDecimal` money, and construction using
  `RoundingMode.UNNECESSARY`. Added no business rules, persistence, caching,
  snapshots, or concurrency infrastructure.
- Reviewed `ORIGINAL_PROMPT` and the documented policies. Recommended basic
  credit/debit booking and value-dated balance calculation as the next increment;
  that work has not started. No commit or push performed.

## 2026-09-07 00:33:07 UTC — Pre-commit cleanup verified

- Reviewed pending source and documentation changes, including untracked Java
  files. Found no stale API references outside historical worklog entries and no
  accidental files among pending additions. Removed `.gitkeep` placeholders are
  superseded by the source and test packages.
- Tidied import order and wrapped the duplicate-account assertion in
  `InMemoryLedgerTest`. Retained the intentional replay parameters and
  `ReplayState.appendLedgerEntry`; no business behavior changed.
- Clarified in `AMBIGUITIES.md` and `REJECTED.md` that selected policies and
  scenario calculations do not claim completed financial event processing.
- Left README unchanged during this cleanup, as requested, and preserved prior
  worklog entries.
- Ran `./mvnw clean verify`: 35 tests passed with no failures, errors, or skips,
  and the JAR built successfully. Checked all 17 Java files for whitespace and
  conflict markers; `git diff --check` passed. No staging, commit, or push performed.

## 2026-09-07 00:39:37 UTC — Basic credit/debit replay verified

- Implemented insertion-order credit/debit processing using fresh replay state.
  Accepted credits append positive entries and debits append negative entries;
  direct debits may overdraw. Entry IDs derive from event IDs and posting type,
  and each entry retains its input-event source and supplied value date.
- Added immutable `ReplayError` records with typed reasons. Invalid credit/debit
  submissions remain in input history, produce one rejection each, and do not
  prevent later valid submissions from booking. Unsupported event types still
  throw explicitly without returning a partial result.
- Validated event identity, account existence, processing/value dates within the
  configured window, currency agreement, and strictly positive input amounts.
  Documented first-occurrence duplicate-ID handling, validation precedence,
  future in-window value dates, and other selected policies in `AMBIGUITIES.md`.
- Extended `ReplayResult` with immutable account definitions and errors, plus
  `balanceOn(accountId, day)`: opening balance plus applicable value-dated entries.
  Queries use the history known after this replay; later submissions do not change
  earlier results. Queries beyond the window only carry forward existing entries.
- Replaced the obsolete test rejecting all nonempty replay with focused credit/debit
  tests. Verified the E7 backdated debit yields Day 2 AED -370.00 before fees,
  preserves the earlier entry prefix, and produces equal results across repeated
  replay. Also covered nonmonotonic dates, account isolation, opening balances,
  invalid inputs, duplicate IDs, immutable collections, and all unsupported types.
- Ran `./mvnw clean verify`, completed at 2026-09-07 00:39:30 UTC:
  43 tests passed with no failures, errors, or skips; JAR build succeeded.
  `git diff --check` passed.
- Account and Money semantics remain unchanged. Authorization, settlement,
  reversal, instalment processing, fees, interest, and the final daily report are
  not implemented in this increment. Updated policy-document status accordingly;
  README was left unchanged. No commit or push performed.

## 2026-09-07 00:43:51 UTC — Policy documents tightened

- Reduced `AMBIGUITIES.md` to material specification gaps, selected policies, and
  their consequences. Removed API mechanics, validation sequencing, progress
  reporting, and speculative extensions; implementation history remains here.
- Reduced `REJECTED.md` to criteria B/F/G/H with supporting reasoning and the
  actually replaced account-local journal approach. Removed the accepted-criteria
  table and the unimplemented truncation alternative, already covered by the
  rounding policy. Preserved the conditional basis of criterion F's rejection.
- Documentation only; no policy or code changes. `git diff --check` passed.
  Tests were not rerun. No commit or push performed.

## 2026-09-07 00:52:12 UTC — Dedicated money package

- Moved `Money` and `Currency` from `ledger.model` to `ledger.money`, and moved
  `MoneyTest` into the matching test package. Updated imports in ledger models,
  replay classes, and tests.
- Verified the moved files differ only in package declarations. Exact arithmetic,
  currency precision, and rounding behavior are unchanged.
- Ran `./mvnw clean verify`: all 43 tests passed with no failures, errors, or skips;
  the JAR built successfully. `git diff --check` passed. No commit or push performed.

## 2026-09-07 00:53:51 UTC — Dedicated replay package

- Moved `LedgerReplay`, `ReplayState`, `ReplayResult`, and `ReplayError` into
  `ledger.replay`, along with `LedgerReplayTest` and `ReplayStateTest`.
  `InMemoryLedger` remains the root API and imports the replay entry point/result.
- Made `LedgerReplay` and its static `replay` method public for cross-package
  delegation. Kept `ReplayState` package-private. Processing behavior is unchanged.
- Ran `./mvnw clean verify`: all 43 tests passed with no failures, errors, or skips;
  the JAR built successfully. No stale root-package replay imports remain and
  `git diff --check` passed. No commit or push performed.

## 2026-09-07 00:55:14 UTC — Package reorganization prepared for commit

- Reviewed the combined money/replay package moves for the user-authorized commit.
  The root package now contains only `InMemoryLedger`; monetary values, ledger
  models, and replay processing have separate packages with matching test locations.
- Both moves and their successful 43-test clean builds are recorded above.
  This commit groups the structural changes without changing financial behavior.

## 2026-09-07 01:04:02 UTC — Authorization assumptions recorded

- Reorganized `AMBIGUITIES.md` into input validity/identity, authorization and
  settlement behavior, and accounting policies. Preserved existing decisions
  and the fee-policy heading used by `REJECTED.md`.
- Recorded the agreed assumptions: authorization IDs are account-scoped and
  cannot be reused, including after rejection or settlement; one settlement
  releases the entire hold; positive settlement amounts cannot exceed that hold.
  These choices are documented policies, not newly implemented behavior.
- Documentation only. `git diff --check` passed; no tests rerun, commit, or push.

## 2026-09-07 01:12:08 UTC — Authorization and settlement replay verified

- Added immutable authorization projections with APPROVED, REJECTED, and SETTLED
  states. Replay owns an account-scoped authorization map; results copy the final
  projections without exposing mutable state. Approved holds affect availability
  but create no ledger entries.
- Approval uses known entries through the event's processing day minus all active
  holds and permits exactly zero remaining availability. Later backdating does not
  rewrite prior decisions. Added a concise timing policy and extended positive
  amount validation to holds in `AMBIGUITIES.md`.
- Settlement of an active authorization books the actual debit at its supplied
  value date and releases the entire hold. Reject unknown/inactive references,
  reused authorization IDs, blank IDs, and settlement above the original hold.
  Invalid settlements preserve the existing hold for a later valid submission.
- Common event validation applies to both new event types. Identifiable rejected
  authorizations reserve their IDs; duplicate/invalid event identities and unknown
  accounts do not create authorization state. Existing authorizations are never
  overwritten by rejected duplicate submissions.
- Added `ReplayResult.authorizations` and `availableOn`. Availability queries use
  final active holds from that result, not historical daily authorization states.
  Shared balance calculations preserve the existing value-date query behavior.
- Added 12 authorization/settlement test cases and narrowed unsupported-event
  tests to reversal and instalment credit. Verified E1–E6 gives AED 465.00 after
  Auth-A settles for AED 185, with Auth-Z rejected; covered exact-zero approval,
  insufficient funds, reuse, repeated/excess settlement, account/currency isolation,
  malformed inputs, backdating, immutable prior results, and repeatable replay.
- Ran `./mvnw clean verify`, completed at 2026-09-07 01:11:54 UTC: all 53 tests
  passed with no failures, errors, or skips; JAR build succeeded.
  `git diff --check` passed. Fees, interest, reversals, instalments, and historical
  daily reporting remain pending. README unchanged. No commit or push performed.

## 2026-09-07 01:19:38 UTC — Debit-only reversal scope recorded

- Recorded the selected reversal scope in `AMBIGUITIES.md`: only a previously
  accepted direct debit on the same account may be reversed. Other event types
  require separate policies.
- Proposed expressing this scope as `DebitReversal(String debitEventId)` in the
  event model. The rename and history-based target validation remain unimplemented.
- Documentation only; `git diff --check` passed. No tests rerun, commit, or push.

## 2026-09-07 01:25:49 UTC — Debit reversal replay verified

- Renamed the input detail to `DebitReversal(String debitEventId)` and updated
  existing fixtures. Replay resolves only previously accepted direct debits on
  the same account; rejected inputs, credits, holds, settlements, and reversal
  entries cannot serve as targets.
- Added per-replay accepted-debit and reversed-debit tracking. A successful
  reversal appends one full credit using the debit's exact amount/currency and
  the reversal's supplied value date, with provenance pointing to the reversal
  event. Original entries and earlier replay results remain unchanged.
- Added structured invalid-target and already-reversed errors. Invalid attempts
  do not consume the debit's reversal eligibility. Common event identity,
  account, and date validation applies without requiring an input amount.
- Treated prevention of double refunds as a correctness invariant and use of the
  supplied value date as specified behavior; added no ambiguity entries for them.
- Added six reversal tests covering E7/E9, differing value dates, repeated refunds,
  ineligible/cross-account/forward references, invalid envelopes, exact BHD amounts,
  and replay repeatability. Instalment credit remains explicitly unsupported.
- Ran `./mvnw clean verify`: all 58 tests passed with no failures, errors, or skips;
  JAR build succeeded. `git diff --check` passed and no old reversal type references
  remain in source. Fees, interest, and instalments remain pending. No commit or push.

## 2026-09-07 01:33:07 UTC — Positive instalment credit allocation verified

- Implemented instalment credits using exact integer minor-unit division and
  earliest-first remainder allocation. E10 produces BHD 3.334, 3.333, and 3.333,
  each using the supplied value date and input-event source. Entry IDs include
  deterministic one-based instalment positions.
- Reject nonpositive counts and counts exceeding the total's minor units before
  appending entries. Recorded the chosen rationale in `AMBIGUITIES.md`: each
  instalment represents a positive transfer; zero shares could conserve the total
  but are intentionally disallowed, not prohibited by the original prompt.
- Common account, currency, amount, date, and event-ID validation applies. Monetary
  amounts remain exact BigDecimal values; allocation uses BigInteger to avoid
  narrowing large totals. Money construction and rounding behavior are unchanged.
- Added 15 instalment test cases covering E10, multiple remainder units, exact
  division, one-minor-unit shares, single instalments, large totals, zero-share
  rejection, invalid counts/amounts, duplicate events, validation, and replay
  repeatability. Removed the obsolete unsupported-instalment test.
- Ran `./mvnw clean verify`: all 72 tests passed with no failures, errors, or skips;
  JAR build succeeded. `git diff --check` passed. All modeled input event types
  now have processing paths; fee assessment, interest, and daily reporting remain
  pending. README unchanged. No commit or push performed.

## 2026-09-07 01:41:22 UTC — Overdraft fee assessment verified

- Added `OverdraftAssessment` to close days as processing advances and reassess
  affected closed days immediately after monetary postings. Processing dates never
  move the closed-day boundary backward; the replay closes remaining days through
  the configured window after all submissions, including late E10.
- Assess accounts in chronological day order using ledger balances only. Append
  one negative fee entry per account/day, with that day's value date and typed
  overdraft source. Earlier fees affect later balances; reversals retain fees.
- Added immutable `FeeAssessment` results containing account, accounting day,
  pre-assessment balance, and optional fee amount. An absent amount reports
  unsupported currency policy, not a waived fee or rejected debit. Unsupported BHD
  assessments remain visible even after a later correction; no guessed fee is booked.
- Honor the configured AED fee, including zero: record a supported zero assessment
  without appending a zero-valued financial entry. Fee state is fresh for each replay.
- Initial test run found seven old assertions expecting pre-fee balances or entry
  counts. Kept those posting-focused cases explicit about using a zero fee; new
  integration tests exercise the specified AED 25 policy and fee interactions.
- Added 11 tests covering the full E1–E10 stream, immediate reassessment before
  authorization, cascading fees, same-day recovery, holds, empty/negative opening
  balances, account isolation, late inputs, retained fees, duplicate prevention,
  unsupported BHD assessment, configured fees, and immutable results.
- Full scenario verification: fees on Days 2, 4, and 5 total AED 75; E9 retains
  them, leaving AED 390 before interest. ACC-002 receives BHD 10.000 after late
  E10. Replay is repeatable and input history remains unchanged.
- Ran `./mvnw clean verify`: all 83 tests passed with no failures, errors, or skips;
  JAR build succeeded. `git diff --check` passed. Existing ambiguity policies were
  sufficient; README unchanged. Interest and daily reporting remain pending.
  No commit or push performed.

## 2026-09-07 01:49:37 UTC — Daily interest and capitalization verified

- Added `InterestCapitalization` after all submitted events and day-close fee
  assessment. Calculate each day's positive ledger balance from the completed
  pre-capitalization entries, including retained fees and late/backdated activity.
  Holds do not reduce the interest basis; zero and negative balances accrue zero.
- Round each daily calculation explicitly to the account currency precision using
  the configured rate and rounding mode. Sum the resulting Money values exactly
  and append one positive capitalization credit per account at the closing day.
  Zero totals create no financial entry. The credit never earns interest itself.
- Added immutable `InterestAccrual` results with the daily pre-capitalization basis.
  An absent amount denotes unfinalized interest for an account with unresolved
  fee assessments. Such accounts receive no capitalization; valid event postings
  remain present and other accounts finalize normally. Added this consequence to
  the existing BHD ambiguity entry; no supplied-scenario policy remained unresolved.
- Added 12 tests for the full scenario, daily sum conservation, AED/BHD half-even
  ties, large exact amounts, configurable rate/rounding, no daily compounding,
  holds, backdating/reversal, zero/negative balances, future value dates, zero rate,
  unsupported BHD fee consequences, immutable results, and repeatable replay.
- Earlier posting and fee tests now explicitly use zero interest where needed to
  retain their focused assertions; the new integration test uses specified settings.
- Full E1–E10 verification: daily AED accruals 0.10, 0.09, 0.25, 0.17, 0.16, 0.16
  total AED 0.93. BHD accrues 0.004 on each of Days 5 and 6, totaling 0.008.
  Final balances are AED 390.93 and BHD 10.008; retained fees remain AED 75.
- Ran `./mvnw clean verify`: all 95 tests passed with no failures, errors, or skips;
  JAR build succeeded. `git diff --check` passed. Runnable daily reporting and
  remaining deliverable checks are still pending. README unchanged. No commit or push.

## 2026-09-07 01:55:40 UTC — Runnable daily scenario report verified

- Added a Java entry point that submits the exact E1–E10 stream using ACC-001
  and ACC-002, retaining E10 after E9 despite its earlier processing day.
- Report both accounts for Days 1–6 with revised closing balances, retained fee
  assessments and their original negative bases, rounded interest, authorization
  states through each processing day, and that day's rejected events.
- Retained immutable authorization transitions during replay, including the
  originating decision event and processing day. Final authorization projections
  remain available; reporting does not finalize shortened replays or recompute
  historical approval decisions from revised balances.
- Documented the report's accounting/operational time perspectives in AMBIGUITIES
  and refreshed README with current capabilities, the run command, field meanings,
  and all expected daily closing balances.
- Added scenario reporting tests for daily balances in both currencies, approval
  and settlement timing, rejected events, fees, accruals, immutable transitions,
  original input order, and repeatable results.
- Ran `./mvnw -B -ntp clean verify`: 97 tests passed with no failures, errors, or
  skips, and the JAR built successfully. Ran the documented Java command and
  inspected its complete output; final balances are AED 390.93 and BHD 10.008.
  `git diff --check` passed. The required intentionally failing design test remains
  pending. No commit or push performed.

## 2026-09-07 01:59:13 UTC — Intentionally failing design test verified

- Committed the daily reporting milestone as `fc4e196` with the message
  `Report daily balances and historical authorization decisions`.
- Added `IntentionalDesignFailure`: debit BHD 1.000 on Day 1, then credit
  BHD 100.000 on Day 2. Accepted postings recover the balance to BHD 99.000,
  but the unresolved Day 1 BHD fee prevents interest finalization through Day 6.
- The inline-annotated assertion requests the broader capability to finalize
  interest after recovery. It exposes the AED-only fee configuration limitation
  without claiming a new prompt requirement or guessing a BHD fee/interest total.
- Kept the class outside default Surefire naming patterns and documented its
  explicit command in README and REJECTED. It is executable, not disabled.
- Ran `./mvnw -B -ntp -Dtest=IntentionalDesignFailure test`: exactly one test
  failed at the intended interest-finalization assertion, with no errors or skips.
- Ran `./mvnw -B -ntp clean verify`: all 97 normal tests passed with no failures,
  errors, or skips; the JAR built successfully. The new test and documentation
  changes remain uncommitted. No push performed.
