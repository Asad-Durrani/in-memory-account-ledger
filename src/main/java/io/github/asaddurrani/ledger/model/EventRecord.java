package io.github.asaddurrani.ledger.model;

import java.util.Objects;

/** Submitted activity. Processing day and value date are numbered accounting days. */
public record EventRecord(
        String eventId, String accountId, int processingDay, int valueDate, Details details) {
    public EventRecord {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(details, "details");
    }

    /** Financial validation belongs to replay so rejected activity remains recorded. */
    public sealed interface Details {
        record Credit(Money amount) implements Details {
            public Credit {
                Objects.requireNonNull(amount, "amount");
            }
        }

        record Debit(Money amount) implements Details {
            public Debit {
                Objects.requireNonNull(amount, "amount");
            }
        }

        record Authorization(String authorizationId, Money holdAmount) implements Details {
            public Authorization {
                Objects.requireNonNull(authorizationId, "authorizationId");
                Objects.requireNonNull(holdAmount, "holdAmount");
            }
        }

        record Settlement(String authorizationId, Money amount) implements Details {
            public Settlement {
                Objects.requireNonNull(authorizationId, "authorizationId");
                Objects.requireNonNull(amount, "amount");
            }
        }

        record Reversal(String referencedEventId) implements Details {
            public Reversal {
                Objects.requireNonNull(referencedEventId, "referencedEventId");
            }
        }

        record InstalmentCredit(Money totalAmount, int instalmentCount) implements Details {
            public InstalmentCredit {
                Objects.requireNonNull(totalAmount, "totalAmount");
            }
        }
    }
}
