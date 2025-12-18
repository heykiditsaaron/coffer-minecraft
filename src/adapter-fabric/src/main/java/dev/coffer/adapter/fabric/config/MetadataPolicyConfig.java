package dev.coffer.adapter.fabric.config;

import dev.coffer.adapter.fabric.boundary.MetadataRelevance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * METADATA POLICY CONFIG — PHASE 3C.2.A
 *
 * Adapter-local configuration that determines how metadata is declared
 * for items during declaration construction.
 *
 * This config:
 * - makes metadata stance explicit and auditable
 * - introduces no valuation semantics
 * - introduces no defaults beyond an explicit global stance
 *
 * Default stance (Option 2): IGNORED_BY_DECLARATION
 */
public final class MetadataPolicyConfig {

    private final MetadataRelevance defaultRelevance;
    private final Map<String, MetadataRelevance> perItem;

    private MetadataPolicyConfig(MetadataRelevance defaultRelevance,
                                 Map<String, MetadataRelevance> perItem) {
        this.defaultRelevance = Objects.requireNonNull(defaultRelevance, "defaultRelevance");
        this.perItem = Collections.unmodifiableMap(new HashMap<>(perItem));
    }

    /**
     * Creates a permissive-by-default config:
     * - default: IGNORED_BY_DECLARATION
     * - no per-item overrides
     */
    public static MetadataPolicyConfig permissiveDefault() {
        return new MetadataPolicyConfig(
                MetadataRelevance.IGNORED_BY_DECLARATION,
                Map.of()
        );
    }

    /**
     * Resolves the metadata relevance for a given itemId.
     * If no override exists, returns the explicit default.
     */
    public MetadataRelevance resolveForItem(String itemId) {
        Objects.requireNonNull(itemId, "itemId");
        return perItem.getOrDefault(itemId, defaultRelevance);
    }

    /**
     * Builder-style factory for future extension (e.g., file-backed config).
     */
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
