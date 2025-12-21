package dev.coffer.adapter.fabric.boundary;

import java.util.Objects;
import java.util.UUID;

/**
 * FABRIC ADAPTER — INVOCATION CONTEXT
 *
 * Responsibility:
 * - Capture who invoked the exchange and how (player vs system).
 */
public record InvocationContext(
        InvokerKind invokerKind,
        UUID playerId
) {
    public InvocationContext {
        Objects.requireNonNull(invokerKind, "invokerKind");
        if (invokerKind == InvokerKind.PLAYER && playerId == null) {
            throw new IllegalArgumentException("playerId required for player invocation");
        }
    }

    public static InvocationContext player(UUID playerId) {
        return new InvocationContext(InvokerKind.PLAYER, Objects.requireNonNull(playerId, "playerId"));
    }

    public static InvocationContext system() {
        return new InvocationContext(InvokerKind.SYSTEM, null);
    }
}
