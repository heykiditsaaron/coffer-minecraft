package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.InventoryRemovalStep;
import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.core.ExchangeEvaluationResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

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

    private final BalanceStore balanceStore;
    private final FabricAuditSink auditSink;

    public FabricMutationTransactionExecutor(BalanceStore balanceStore, FabricAuditSink auditSink) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
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

        ExecutionResult inv = inventoryStep.apply();
        if (!inv.success()) {
            return ExecutionResult.fail("INVENTORY_REMOVAL_FAILED: " + inv.reason());
        }

        List<BalanceCreditStep> applied = new ArrayList<>();
        try {
            for (var entry : creditPlan.creditsByCurrency().entrySet()) {
                BalanceCreditStep step = new BalanceCreditStep(balanceStore, entry.getKey(), entry.getValue(), auditSink);
                ExecutionResult creditRes = step.apply(playerId);
                if (!creditRes.success()) {
                    for (int i = applied.size() - 1; i >= 0; i--) {
                        applied.get(i).rollback(playerId);
                    }
                    inventoryStep.rollback();
                    return ExecutionResult.fail("BALANCE_CREDIT_FAILED: " + creditRes.reason());
                }
                applied.add(step);
            }
        } catch (Exception e) {
            for (int i = applied.size() - 1; i >= 0; i--) {
                applied.get(i).rollback(playerId);
            }
            inventoryStep.rollback();
            return ExecutionResult.fail("BALANCE_CREDIT_FAILED: " + e.getMessage());
        }

        return ExecutionResult.ok();
    }
}
