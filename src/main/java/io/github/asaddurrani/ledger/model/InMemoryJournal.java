package io.github.asaddurrani.ledger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** In-memory append-only history. Not thread-safe. */
public final class InMemoryJournal<T> implements AppendOnlyJournal<T> {
    private final List<T> records = new ArrayList<>();

    @Override
    public void append(T record) {
        records.add(Objects.requireNonNull(record, "record"));
    }

    @Override
    public List<T> records() {
        return List.copyOf(records);
    }
}
