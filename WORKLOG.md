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
