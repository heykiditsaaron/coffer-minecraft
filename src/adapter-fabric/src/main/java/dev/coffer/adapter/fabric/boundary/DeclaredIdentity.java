package dev.coffer.adapter.fabric.boundary;

import java.util.Objects;
import java.util.UUID;

/**
 * FABRIC ADAPTER — DECLARED IDENTITY
 *
 * Responsibility:
 * - Immutable representation of the target identity for an exchange.
 *
 * Not responsible for:
 * - Permissions, policy, or platform resolution beyond holding an id.
 *
 * Invariants:
 * - UUID must be non-null.
 */
public record DeclaredIdentity(UUID id) {
    public DeclaredIdentity {
        Objects.requireNonNull(id, "id");
    }

    public static DeclaredIdentity of(UUID id) {
        return new DeclaredIdentity(id);
    }
}
