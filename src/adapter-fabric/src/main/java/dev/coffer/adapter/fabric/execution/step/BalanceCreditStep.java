package dev.coffer.adapter.fabric.execution.step;

import dev.coffer.adapter.fabric.execution.BalanceCreditPlan;
import dev.coffer.adapter.fabric.execution.ExecutionResult;
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

    public ExecutionResult apply(UUID playerId) {
        if (applied) return ExecutionResult.fail("ALREADY_APPLIED");
        if (!plan.targetPlayerId().equals(playerId)) {
            return ExecutionResult.fail("PLAYER_MISMATCH");
        }

        delta = plan.creditAmount();
        store.applyDelta(playerId, delta);
        applied = true;

        return ExecutionResult.ok();
    }

    public void rollback(UUID playerId) {
        if (!applied) return;
        store.applyDelta(playerId, -delta);
        applied = false;
        delta = 0;
    }
}
