package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.BalanceMutation;
import dev.coffer.core.ExchangeEvaluationResult;

import java.util.Objects;

/**
 * FABRIC MUTATION EXECUTOR (PHASE 3B.3).
 *
 * Applies Core-issued mutations using volatile adapter storage.
 *
 * This executor:
 * - trusts Core-issued mutations
 * - applies them exactly
 * - performs no validation or reinterpretation
 *
 * Persistence is intentionally absent.
 */
public final class FabricMutationExecutor {

    private final InMemoryBalanceStore balanceStore;

    public FabricMutationExecutor(InMemoryBalanceStore balanceStore) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore must be non-null");
    }

    /**
     * Apply all balance mutations from a Core evaluation result.
     */
    public void apply(ExchangeEvaluationResult result) {
        Objects.requireNonNull(result, "result must be non-null");

        for (BalanceMutation mutation : result.balanceMutations()) {
            balanceStore.applyDelta(
                    mutation.account(),
                    mutation.delta()
            );
        }
    }
}
