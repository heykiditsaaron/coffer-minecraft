package dev.coffer.adapter.fabric.boundary;

/**
 * FABRIC ADAPTER — METADATA RELEVANCE DECLARATION (PHASE 3.B).
 *
 * This does NOT parse platform metadata (NBT, tags, etc.).
 * It only captures the declared stance required by adapter law:
 *
 * - Metadata is never silently ignored.
 * - Metadata may be ignored only by explicit, auditable declaration.
 * - If metadata relevance is undeclared / unknown, the adapter must refuse.
 */
public enum MetadataRelevance {
    /**
     * Metadata is considered relevant to valuation/policy and must be declared
     * (or the adapter must refuse if it cannot be declared truthfully).
     */
    RELEVANT,

    /**
     * Metadata is explicitly declared irrelevant for valuation/policy relevance.
     * This choice MUST be auditable when used.
     */
    IGNORED_BY_DECLARATION,

    /**
     * Metadata relevance is not declared.
     * This state exists so the adapter can refuse explicitly without guessing.
     */
    UNDECLARED
}
