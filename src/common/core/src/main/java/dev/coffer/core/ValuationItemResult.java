package dev.coffer.core;

import java.util.Objects;

/**
 * Result of valuating a single item (or item unit group).
 */
public final class ValuationItemResult {

    private final Object item;
    private final long quantity;
    private final long totalValue;
    private final DenialReason denialReason;

    private ValuationItemResult(
            Object item,
            long quantity,
            long totalValue,
            DenialReason denialReason
    ) {
        this.item = item;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.denialReason = denialReason;
    }

    public static ValuationItemResult accepted(Object item, long quantity, long totalValue) {
        if (totalValue <= 0) {
            throw new IllegalArgumentException("totalValue must be positive");
        }
        return new ValuationItemResult(
                Objects.requireNonNull(item),
                quantity,
                totalValue,
                null
        );
    }

    public static ValuationItemResult rejected(Object item, long quantity, DenialReason reason) {
        return new ValuationItemResult(
                Objects.requireNonNull(item),
                quantity,
                0,
                Objects.requireNonNull(reason)
        );
    }

    public Object item() {
        return item;
    }

    public long quantity() {
        return quantity;
    }

    public long totalValue() {
        return totalValue;
    }

    public boolean accepted() {
        return denialReason == null;
    }

    public DenialReason denialReason() {
        return denialReason;
    }
}
