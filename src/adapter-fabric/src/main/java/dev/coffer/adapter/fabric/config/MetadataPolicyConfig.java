package dev.coffer.adapter.fabric.config;

import dev.coffer.adapter.fabric.boundary.MetadataRelevance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * METADATA POLICY CONFIG
 *
 * Responsibility:
 * - Declare metadata relevance stance per itemId.
 *
 * Invariants:
 * - Default stance is explicit.
 * - Resolves to a concrete MetadataRelevance (no nulls).
 */
public final class MetadataPolicyConfig {

    private final MetadataRelevance defaultRelevance;
    private final Map<String, MetadataRelevance> perItem;

    private MetadataPolicyConfig(MetadataRelevance defaultRelevance,
                                 Map<String, MetadataRelevance> perItem) {
        this.defaultRelevance = Objects.requireNonNull(defaultRelevance, "defaultRelevance");
        this.perItem = Collections.unmodifiableMap(new HashMap<>(perItem));
    }

    public static MetadataPolicyConfig permissiveDefault() {
        return new MetadataPolicyConfig(
                MetadataRelevance.IGNORED_BY_DECLARATION,
                Map.of()
        );
    }

    public MetadataRelevance resolveForItem(String itemId) {
        Objects.requireNonNull(itemId, "itemId");
        return perItem.getOrDefault(itemId, defaultRelevance);
    }

    public static Builder builder(MetadataRelevance defaultRelevance) {
        return new Builder(defaultRelevance);
    }

    public static final class Builder {
        private final MetadataRelevance defaultRelevance;
        private final Map<String, MetadataRelevance> perItem = new HashMap<>();

        private Builder(MetadataRelevance defaultRelevance) {
            this.defaultRelevance = Objects.requireNonNull(defaultRelevance, "defaultRelevance");
        }

        public Builder setItem(String itemId, MetadataRelevance relevance) {
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(relevance, "relevance");
            perItem.put(itemId, relevance);
            return this;
        }

        public MetadataPolicyConfig build() {
            return new MetadataPolicyConfig(defaultRelevance, perItem);
        }
    }
}
