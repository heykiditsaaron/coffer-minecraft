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
    private final long creditAmount;

    public BalanceCreditPlan(UUID targetPlayerId, long creditAmount) {
        this.targetPlayerId = Objects.requireNonNull(targetPlayerId, "targetPlayerId");

        if (creditAmount <= 0L) {
            throw new IllegalArgumentException("creditAmount must be > 0");
        }
        this.creditAmount = creditAmount;
    }

    public UUID targetPlayerId() {
        return targetPlayerId;
    }

    public long creditAmount() {
        return creditAmount;
    }
}
