package io.github.asaddurrani.ledger.model;

import java.util.List;

/** Ordered history with no update or delete operations. Records must be immutable. */
public interface AppendOnlyJournal<T> {
    /** Append a non-null record, preserving repeated submissions. */
    void append(T record);

    /** Immutable snapshot in insertion order, unaffected by later appends. */
    List<T> records();
}
