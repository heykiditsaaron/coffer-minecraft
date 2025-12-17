package dev.coffer.core;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of a valuation attempt.
 *
 * Contains accepted and rejected item results.
 */
public final class ValuationSnapshot {

    private final List<ValuationItemResult> itemResults;
    private final long totalAcceptedValue;

    public ValuationSnapshot(List<ValuationItemResult> itemResults) {
        this.itemResults = List.copyOf(
                Objects.requireNonNull(itemResults, "itemResults")
        );

        long sum = 0;
        for (ValuationItemResult result : this.itemResults) {
            if (result.accepted()) {
                sum += result.totalValue();
            }
        }
        this.totalAcceptedValue = sum;
    }

    public List<ValuationItemResult> itemResults() {
        return itemResults;
    }

    public long totalAcceptedValue() {
        return totalAcceptedValue;
    }

    public boolean hasAnyAccepted() {
        return totalAcceptedValue > 0;
    }
}
