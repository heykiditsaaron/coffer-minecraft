package dev.coffer.adapter.fabric.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * VALUATION CONFIG — PHASE 3C.3
 *
 * Adapter-local valuation configuration.
 *
 * Rules:
 * - Items have no value unless explicitly listed.
 * - Any value <= 0 means the item is NOT a participant in the economy.
 * - No defaults are inferred.
 * - Zero-config boots and denies all valuation honestly.
 *
 * NOTE:
 * - File-backed loading is deferred by design.
 * - This is a pure data structure.
 */
public final class ValuationConfig {

    private final Map<String, Long> valueByItemId;

    private ValuationConfig(Map<String, Long> valueByItemId) {
        this.valueByItemId = Collections.unmodifiableMap(new HashMap<>(valueByItemId));
    }

    /**
     * Zero-config configuration.
     * All items are unvalued and will be denied.
     */
    public static ValuationConfig empty() {
        return new ValuationConfig(Map.of());
    }

    /**
     * Returns the configured value for an itemId, or null if unlisted.
     */
    public Long getValueForItem(String itemId) {
        Objects.requireNonNull(itemId, "itemId");
        return valueByItemId.get(itemId);
    }

    /**
     * Builder for future extension (e.g., file-backed config).
     */
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
