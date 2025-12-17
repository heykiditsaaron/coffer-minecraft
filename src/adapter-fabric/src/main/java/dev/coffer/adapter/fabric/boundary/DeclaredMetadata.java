package dev.coffer.adapter.fabric.boundary;

import java.util.Optional;

/**
 * FABRIC ADAPTER — OPAQUE METADATA SHAPE (PHASE 3.B).
 *
 * This is intentionally opaque and platform-agnostic.
 * It allows the adapter to declare metadata facts (or refuse) without
 * encoding NBT semantics here.
 *
 * Examples of scheme values (non-binding): "NBT", "DATA_COMPONENTS", "CUSTOM"
 *
 * IMPORTANT:
 * - This record is only "declared facts".
 * - Meaning and interpretation are adapter/platform-specific and deferred.
 */
public record DeclaredMetadata(
        String scheme,
        String fingerprint,
        Optional<String> note
) {
    public DeclaredMetadata {
        if (scheme == null || scheme.isBlank()) throw new IllegalArgumentException("scheme must be non-empty");
        if (fingerprint == null || fingerprint.isBlank()) throw new IllegalArgumentException("fingerprint must be non-empty");
        if (note == null) throw new IllegalArgumentException("note must be non-null (use Optional.empty())");
        scheme = scheme.trim();
        fingerprint = fingerprint.trim();
        note = note.map(String::trim).filter(s -> !s.isBlank());
    }

    public static DeclaredMetadata of(String scheme, String fingerprint) {
        return new DeclaredMetadata(scheme, fingerprint, Optional.empty());
    }

    public static DeclaredMetadata of(String scheme, String fingerprint, String note) {
        return new DeclaredMetadata(scheme, fingerprint, Optional.ofNullable(note));
    }
}
