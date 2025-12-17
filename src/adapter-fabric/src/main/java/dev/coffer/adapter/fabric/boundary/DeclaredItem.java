package dev.coffer.adapter.fabric.boundary;

import java.util.Optional;

/**
 * FABRIC ADAPTER — DECLARED ITEM SHAPE (PHASE 3.B).
 *
 * This does not assume fungibility.
 * It is a declaration envelope for an item identity as the adapter can justify.
 *
 * itemId: platform identifier string (e.g., "minecraft:dirt")
 * count: strictly positive quantity
 *
 * metadataRelevance:
 * - RELEVANT: metadata must be declared (or refuse)
 * - IGNORED_BY_DECLARATION: metadata may be omitted, but the choice must be auditable later
 * - UNDECLARED: must not proceed; adapter should refuse (this exists to prevent guessing)
 */
public record DeclaredItem(
        String itemId,
        int count,
        MetadataRelevance metadataRelevance,
        Optional<DeclaredMetadata> metadata
) {
    public DeclaredItem {
        if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId must be non-empty");
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        if (metadataRelevance == null) throw new IllegalArgumentException("metadataRelevance must be non-null");
        if (metadata == null) throw new IllegalArgumentException("metadata must be non-null (use Optional.empty())");

        itemId = itemId.trim();
    }

    public static DeclaredItem withoutMetadata(String itemId, int count, MetadataRelevance relevance) {
        return new DeclaredItem(itemId, count, relevance, Optional.empty());
    }

    public static DeclaredItem withMetadata(String itemId, int count, MetadataRelevance relevance, DeclaredMetadata metadata) {
        return new DeclaredItem(itemId, count, relevance, Optional.ofNullable(metadata));
    }
}
