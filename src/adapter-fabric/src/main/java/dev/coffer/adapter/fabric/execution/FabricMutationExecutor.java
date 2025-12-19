package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.InventoryRemovalStep;
import dev.coffer.core.ExchangeEvaluationResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.UUID;

public final class FabricMutationExecutor {

    private final InMemoryBalanceStore balanceStore;

    public FabricMutationExecutor() {
        this(new InMemoryBalanceStore());
    }

    public FabricMutationExecutor(InMemoryBalanceStore balanceStore) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore");
    }

    public void execute(
            ServerPlayerEntity targetPlayer,
            ExchangeEvaluationResult evaluationResult,
            MutationContext mutationContext
    ) {
        Objects.requireNonNull(targetPlayer);
        Objects.requireNonNull(evaluationResult);
        Objects.requireNonNull(mutationContext);

        if (!evaluationResult.allowed()) return;
        if (!targetPlayer.getUuid().equals(mutationContext.targetPlayerId())) return;
    }

    public ExecutionResult executeAtomic(
            ServerPlayerEntity targetPlayer,
            ExchangeEvaluationResult evaluationResult,
            MutationContext mutationContext,
            BalanceCreditPlan creditPlan
    ) {
        Objects.requireNonNull(targetPlayer);
        Objects.requireNonNull(evaluationResult);
        Objects.requireNonNull(mutationContext);
        Objects.requireNonNull(creditPlan);

        if (!evaluationResult.allowed()) {
            return ExecutionResult.fail("CORE_DENIED");
        }

        UUID playerId = targetPlayer.getUuid();

        if (!playerId.equals(mutationContext.targetPlayerId())) {
            return ExecutionResult.fail("MUTATION_CONTEXT_PLAYER_MISMATCH");
        }

        if (!playerId.equals(creditPlan.targetPlayerId())) {
            return ExecutionResult.fail("CREDIT_PLAN_PLAYER_MISMATCH");
        }

        InventoryRemovalStep inventoryStep =
                new InventoryRemovalStep(targetPlayer, mutationContext);

        BalanceCreditStep balanceStep =
                new BalanceCreditStep(balanceStore, creditPlan);

        MutationTransaction tx =
                new MutationTransaction(playerId, inventoryStep, balanceStep);

        MutationTransaction.Result txResult = tx.execute();

        if (!txResult.success()) {
            return ExecutionResult.fail(txResult.reason());
        }

        return ExecutionResult.ok();
    }

    public record ExecutionResult(boolean success, String reason) {
        public static ExecutionResult ok() {
            return new ExecutionResult(true, null);
        }

        public static ExecutionResult fail(String reason) {
            return new ExecutionResult(false, Objects.requireNonNull(reason));
        }
    }
}
