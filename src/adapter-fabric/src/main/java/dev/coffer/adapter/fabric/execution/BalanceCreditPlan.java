package dev.coffer.adapter.fabric.execution;

import java.util.Objects;
import java.util.UUID;

/**
 * BALANCE CREDIT PLAN — PHASE 3D.3
 *
 * Adapter-owned, immutable credit intent.
 *
 * This plan represents the exact balance credit the adapter intends to apply
 * if (and only if) atomic mutation proceeds after Core PASS and binding checks.
 *
 * Rules:
 * - Built ONLY from adapter-verified truth (and already-established valuation outputs)
 * - Immutable once created
 * - Never inferred or recomputed during execution
 * - Opaque to Core
 *
 * Contains NO behavior.
 * Persistence is explicitly out of scope for Phase 3D.3.
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
