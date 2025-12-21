package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.InventoryRemovalStep;
import dev.coffer.core.ExchangeEvaluationResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.UUID;

/**
 * FABRIC MUTATION TRANSACTION EXECUTOR
 *
 * Responsibility:
 * - Bind Core PASS, MutationContext, and BalanceCreditPlan to the same player.
 * - Run atomic mutation transaction (inventory removal + balance credit).
 *
 * Invariants:
 * - No mutation if Core denied.
 * - Player identity must match across evaluation, mutation context, and credit plan.
 * - All-or-nothing execution via MutationTransaction.
 */
public final class FabricMutationTransactionExecutor {

    private final InMemoryBalanceStore balanceStore;

    public FabricMutationTransactionExecutor() {
        this(new InMemoryBalanceStore());
    }

    public FabricMutationTransactionExecutor(InMemoryBalanceStore balanceStore) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore");
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

        ExecutionResult txResult = tx.execute();

        if (!txResult.success()) {
            return ExecutionResult.fail(txResult.reason());
        }

        return ExecutionResult.ok();
    }
}
