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
    private final String currencyId;

    private ValuationItemResult(
            Object item,
            long quantity,
            long totalValue,
            DenialReason denialReason,
            String currencyId
    ) {
        this.item = Objects.requireNonNull(item);
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.denialReason = denialReason;
        this.currencyId = currencyId;
    }

    public static ValuationItemResult accepted(Object item, long quantity, long totalValue, String currencyId) {
        if (totalValue <= 0) {
            throw new IllegalArgumentException("totalValue must be positive");
        }
        if (currencyId == null || currencyId.isBlank()) {
            throw new IllegalArgumentException("currencyId must be non-empty");
        }
        return new ValuationItemResult(
                item,
                quantity,
                totalValue,
                null,
                currencyId
        );
    }

    public static ValuationItemResult rejected(Object item, long quantity, DenialReason reason) {
        return new ValuationItemResult(
                item,
                quantity,
                0,
                Objects.requireNonNull(reason),
                null
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

    public String currencyId() {
        return currencyId;
    }
}
