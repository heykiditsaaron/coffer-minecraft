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
    private final java.util.Map<String, Long> totalsByCurrency;

    public ValuationSnapshot(List<ValuationItemResult> itemResults) {
        this.itemResults = List.copyOf(
                Objects.requireNonNull(itemResults, "itemResults")
        );

        java.util.Map<String, Long> totals = new java.util.HashMap<>();
        for (ValuationItemResult result : this.itemResults) {
            if (result.accepted()) {
                String currency = result.currencyId();
                if (currency == null) continue;
                totals.merge(currency, result.totalValue(), Long::sum);
            }
        }
        this.totalsByCurrency = java.util.Collections.unmodifiableMap(totals);
    }

    public List<ValuationItemResult> itemResults() {
        return itemResults;
    }

    public long totalAcceptedValue() {
        return totalsByCurrency.values().stream().mapToLong(Long::longValue).sum();
    }

    public java.util.Map<String, Long> totalsByCurrency() {
        return totalsByCurrency;
    }

    public boolean hasAnyAccepted() {
        return totalsByCurrency.values().stream().anyMatch(v -> v > 0);
    }
}
