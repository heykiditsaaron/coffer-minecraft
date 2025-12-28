package dev.coffer.adapter.fabric.execution;

import java.util.Objects;
import java.util.UUID;

/**
 * BALANCE CREDIT PLAN
 *
 * Responsibility:
 * - Immutable credit intent derived from planned, accepted value.
 *
 * Invariants:
 * - Player id non-null; creditAmount > 0.
 * - No behavior; opaque to Core.
 */
public final class BalanceCreditPlan {

    private final UUID targetPlayerId;
    private final java.util.Map<String, Long> creditsByCurrency;

    public BalanceCreditPlan(UUID targetPlayerId, java.util.Map<String, Long> creditsByCurrency) {
        this.targetPlayerId = Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        if (creditsByCurrency == null || creditsByCurrency.isEmpty()) {
            throw new IllegalArgumentException("creditsByCurrency must be non-empty");
        }
        for (var e : creditsByCurrency.entrySet()) {
            if (e.getKey() == null || e.getKey().isBlank()) {
                throw new IllegalArgumentException("currencyId must be non-empty");
            }
            if (e.getValue() == null || e.getValue() <= 0) {
                throw new IllegalArgumentException("credit amount must be > 0");
            }
        }
        this.creditsByCurrency = java.util.Collections.unmodifiableMap(new java.util.HashMap<>(creditsByCurrency));
    }

    public UUID targetPlayerId() {
        return targetPlayerId;
    }

    public java.util.Map<String, Long> creditsByCurrency() {
        return creditsByCurrency;
    }
}
