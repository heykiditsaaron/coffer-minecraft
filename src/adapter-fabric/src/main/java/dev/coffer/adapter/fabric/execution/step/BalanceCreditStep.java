package dev.coffer.adapter.fabric.execution.step;

import dev.coffer.adapter.fabric.execution.BalanceCreditPlan;
import dev.coffer.adapter.fabric.execution.InMemoryBalanceStore;

import java.util.Objects;
import java.util.UUID;

public final class BalanceCreditStep {

    private final InMemoryBalanceStore store;
    private final BalanceCreditPlan plan;

    private boolean applied;
    private long delta;

    public BalanceCreditStep(InMemoryBalanceStore store, BalanceCreditPlan plan) {
        this.store = Objects.requireNonNull(store);
        this.plan = Objects.requireNonNull(plan);
    }

    public ApplyResult apply(UUID playerId) {
        if (applied) return ApplyResult.fail("ALREADY_APPLIED");
        if (!plan.targetPlayerId().equals(playerId)) {
            return ApplyResult.fail("PLAYER_MISMATCH");
        }

        delta = plan.creditAmount();
        store.applyDelta(playerId, delta);
        applied = true;

        return ApplyResult.ok();
    }

    public void rollback(UUID playerId) {
        if (!applied) return;
        store.applyDelta(playerId, -delta);
        applied = false;
        delta = 0;
    }

    public record ApplyResult(boolean success, String reason) {
        public static ApplyResult ok() {
            return new ApplyResult(true, null);
        }

        public static ApplyResult fail(String reason) {
            return new ApplyResult(false, Objects.requireNonNull(reason));
        }
    }
}
