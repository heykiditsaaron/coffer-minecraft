package dev.coffer.adapter.fabric.execution.step;

import dev.coffer.adapter.fabric.execution.BalanceCreditPlan;
import dev.coffer.adapter.fabric.execution.InMemoryBalanceStore;

import java.util.Objects;
import java.util.UUID;

/**
 * BALANCE CREDIT STEP — PHASE 3D.3
 *
 * Applies an adapter-owned BalanceCreditPlan using a provided balance store.
 *
 * Rules:
 * - No guessing, no recomputation.
 * - Applies exactly the planned credit amount.
 * - Rollback is supported via inverse delta.
 * - No persistence guarantees (explicitly out of scope).
 * - No Core interaction.
 */
public final class BalanceCreditStep {

    private final InMemoryBalanceStore balanceStore;
    private final BalanceCreditPlan creditPlan;

    private boolean applied;
    private long appliedDelta;

    public BalanceCreditStep(
            InMemoryBalanceStore balanceStore,
            BalanceCreditPlan creditPlan
    ) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore");
        this.creditPlan = Objects.requireNonNull(creditPlan, "creditPlan");
    }

    /**
     * Apply the planned balance credit.
     *
     * Returns:
     * - ApplyResult.applied() == true if credit was applied
     * - ApplyResult.applied() == false if binding failed or step was already applied
     *
     * This method is single-use; calling apply twice is refused.
     */
    public ApplyResult apply(UUID targetPlayerId) {
        if (applied) {
            return ApplyResult.failed("BALANCE_STEP_ALREADY_APPLIED");
        }

        if (!creditPlan.targetPlayerId().equals(targetPlayerId)) {
            return ApplyResult.failed("PLAYER_MISMATCH");
        }

        long delta = creditPlan.creditAmount();

        balanceStore.applyDelta(targetPlayerId, delta);
        appliedDelta = delta;
        applied = true;

        return ApplyResult.applied();
    }

    /**
     * Roll back the previously applied balance credit.
     *
     * Safe to call even if apply() was never successful; it will no-op in that case.
     */
    public void rollback(UUID targetPlayerId) {
        if (!applied) {
            return;
        }

        balanceStore.applyDelta(targetPlayerId, -appliedDelta);
        appliedDelta = 0L;
        applied = false;
    }

    // -------------------------
    // Types
    // -------------------------

    public record ApplyResult(boolean applied, String reason) {
        public static ApplyResult applied() {
            return new ApplyResult(true, null);
        }

        public static ApplyResult failed(String reason) {
            return new ApplyResult(false, Objects.requireNonNull(reason, "reason"));
        }
    }
}
