package dev.coffer.adapter.fabric.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * VALUATION CONFIG
 *
 * Responsibility:
 * - Explicit per-item valuation data for the adapter.
 *
 * Invariants:
 * - Items have no value unless listed.
 * - value <= 0 means the item is not a participant.
 */
public final class ValuationConfig {

    private final Map<String, Long> valueByItemId;

    private ValuationConfig(Map<String, Long> valueByItemId) {
        this.valueByItemId = Collections.unmodifiableMap(new HashMap<>(valueByItemId));
    }

    public static ValuationConfig empty() {
        return new ValuationConfig(Map.of());
    }

    public Long getValueForItem(String itemId) {
        Objects.requireNonNull(itemId, "itemId");
        return valueByItemId.get(itemId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Long> valueByItemId = new HashMap<>();

        public Builder setValue(String itemId, long value) {
            Objects.requireNonNull(itemId, "itemId");
            valueByItemId.put(itemId, value);
            return this;
        }

        public ValuationConfig build() {
            return new ValuationConfig(valueByItemId);
        }
    }
}
