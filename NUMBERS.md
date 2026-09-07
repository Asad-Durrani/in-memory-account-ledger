# Numerical values

The scenario's amounts, rate, currency precision, and dates come from the prompt.
We introduced no additional financial rates, fee amounts, or tuning parameters.
Halving the supplied values would change the required scenario.

| Item | Prompt value |
| --- | --- |
| Accounting window | Days 1–6 |
| Opening balances | ACC-001: AED 0.00; ACC-002: BHD 0.000 |
| Currency precision | AED: 2 decimal places; BHD: 3 |
| Overdraft fee | AED 25.00, once per day per account with a negative closing balance |
| Daily interest | 0.04%, capitalized at the end of Day 6 |
| Authorization boundary | Available balance must remain at or above zero |

| Event | Amount | Processing day | Value date |
| --- | --- | --- | --- |
| E1 — credit | AED 1,200.00 | 1 | 1 |
| E2 — debit | AED 950.00 | 1 | 1 |
| E3 — Auth-A hold | AED 200.00 | 2 | 2 |
| E4 — credit | AED 400.00 | 3 | 3 |
| E5 — Auth-A settlement | AED 185.00 | 4 | 4 |
| E6 — Auth-Z settlement | AED 180.00 | 4 | 4 |
| E7 — debit | AED 620.00 | 5 | 2 |
| E8 — Auth-B hold | AED 90.00 | 5 | 5 |
| E9 — reversal | Reverses E7 | 6 | 2 |
| E10 — credit | BHD 10.000 across 3 instalments | 5 | 5 |
