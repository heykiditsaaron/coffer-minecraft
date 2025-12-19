package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.InventoryRemovalStep;
import dev.coffer.core.ExchangeEvaluationResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.UUID;

/**
 * FABRIC MUTATION EXECUTOR — PHASE 3D.3
 *
 * Defines the execution boundary between truthful Core evaluation
 * and real-world adapter mutation.
 *
 * This executor:
 * - performs binding validation
 * - coordinates atomic mutation when all execution inputs are explicitly provided
 * - never reconstructs or guesses mutation intent
 *
 * Phase compatibility:
 * - Phase 3D.2 path is preserved (no mutation)
 * - Phase 3D.3 path performs atomic inventory + balance mutation
 */
public final class FabricMutationExecutor {

    private final InMemoryBalanceStore balanceStore;

    /**
     * Phase 3D.3 constructor.
     *
     * Balance store ownership is adapter-side and long-lived.
     * The store is injected explicitly to avoid hidden state.
     */
    public FabricMutationExecutor(InMemoryBalanceStore balanceStore) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore");
    }

    /**
     * PHASE 3D.2 EXECUTION PATH (PRESERVED)
     *
     * Binding checks only.
     * No mutation occurs.
     */
    public void execute(
            ServerPlayerEntity targetPlayer,
            ExchangeEvaluationResult evaluationResult,
            MutationContext mutationContext
    ) {
        Objects.requireNonNull(targetPlayer, "targetPlayer");
        Objects.requireNonNull(evaluationResult, "evaluationResult");
        Objects.requireNonNull(mutationContext, "mutationContext");

        if (!evaluationResult.allowed()) {
            return;
        }

        if (!targetPlayer.getUuid().equals(mutationContext.targetPlayerId())) {
            return;
        }

        // Intentionally no mutation (Phase 3D.2)
    }

    /**
     * PHASE 3D.3 EXECUTION PATH
     *
     * Performs atomic mutation using:
     * - adapter-owned MutationContext
     * - adapter-owned BalanceCreditPlan
     *
     * Failure semantics (Option C):
     * - rollback on any failure
     * - explicit refusal via result
     * - no adapter faulting here
     */
    public ExecutionResult executeAtomic(
            ServerPlayerEntity targetPlayer,
            ExchangeEvaluationResult evaluationResult,
            MutationContext mutationContext,
            BalanceCreditPlan creditPlan
    ) {
        Objects.requireNonNull(targetPlayer, "targetPlayer");
        Objects.requireNonNull(evaluationResult, "evaluationResult");
        Objects.requireNonNull(mutationContext, "mutationContext");
        Objects.requireNonNull(creditPlan, "creditPlan");

        if (!evaluationResult.allowed()) {
            return ExecutionResult.refused("CORE_DENIED");
        }

        UUID playerId = targetPlayer.getUuid();

        if (!playerId.equals(mutationContext.targetPlayerId())) {
            return ExecutionResult.refused("MUTATION_CONTEXT_PLAYER_MISMATCH");
        }

        if (!playerId.equals(creditPlan.targetPlayerId())) {
            return ExecutionResult.refused("CREDIT_PLAN_PLAYER_MISMATCH");
        }

        InventoryRemovalStep inventoryStep =
                new InventoryRemovalStep(targetPlayer, mutationContext);

        BalanceCreditStep balanceStep =
                new BalanceCreditStep(balanceStore, creditPlan);

        MutationTransaction transaction =
                new MutationTransaction(playerId, inventoryStep, balanceStep);

        MutationTransaction.Result result = transaction.execute();

        if (!result.success()) {
            return ExecutionResult.refused(result.reason());
        }

        return ExecutionResult.applied();
    }

    // -------------------------
    // Types
    // -------------------------

    public record ExecutionResult(boolean applied, String reason) {

        public static ExecutionResult applied() {
            return new ExecutionResult(true, null);
        }

        public static ExecutionResult refused(String reason) {
            return new ExecutionResult(false, Objects.requireNonNull(reason, "reason"));
        }
    }
}
