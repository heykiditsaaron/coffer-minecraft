package dev.coffer.adapter.fabric.boundary;

import java.util.Optional;
import java.util.UUID;

/**
 * FABRIC ADAPTER — IDENTITY SHAPE ONLY (PHASE 3.B).
 *
 * Identity is declared as facts, not inferred meaning.
 * UUID is the stable identifier; name is optional and non-authoritative.
 */
public record DeclaredIdentity(
        UUID uuid,
        Optional<String> name
) {
    public DeclaredIdentity {
        if (uuid == null) throw new IllegalArgumentException("uuid must be non-null");
        if (name == null) throw new IllegalArgumentException("name must be non-null (use Optional.empty())");
        name = name.map(String::trim).filter(s -> !s.isBlank());
    }

    public static DeclaredIdentity of(UUID uuid) {
        return new DeclaredIdentity(uuid, Optional.empty());
    }

    public static DeclaredIdentity of(UUID uuid, String name) {
        return new DeclaredIdentity(uuid, Optional.ofNullable(name));
    }
}
