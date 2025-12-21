package dev.coffer.adapter.fabric.boundary;

import java.util.Objects;

/**
 * FABRIC ADAPTER — DECLARED ITEM
 *
 * Responsibility:
 * - Immutable declaration of an item id, quantity, and metadata relevance stance.
 *
 * Invariants:
 * - itemId non-blank, count > 0, relevance non-null.
 * - Carries no valuation or mutation meaning.
 */
public record DeclaredItem(
        String itemId,
        long count,
        MetadataRelevance metadataRelevance,
        DeclaredMetadata metadata
) {
    public DeclaredItem {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must be non-empty");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        Objects.requireNonNull(metadataRelevance, "metadataRelevance");
    }

    public static DeclaredItem withoutMetadata(
            String itemId,
            long count,
            MetadataRelevance metadataRelevance
    ) {
        return new DeclaredItem(itemId, count, metadataRelevance, null);
    }
}
