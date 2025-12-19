package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.InventoryRemovalStep;

import java.util.Objects;
import java.util.UUID;

/**
 * MUTATION TRANSACTION — PHASE 3D.3
 *
 * Coordinates atomic execution of:
 *  1) Inventory removal
 *  2) Balance credit
 *
 * Guarantees:
 * - Either both mutations succeed, or both are rolled back
 * - No partial success is observable
 * - No guessing or recomputation
 *
 * Failure semantics (Option C):
 * - Rollback on any failure
 * - Return explicit failure result
 * - Do NOT fault the adapter here
 */
public final class MutationTransaction {

    private final UUID targetPlayerId;
    private final InventoryRemovalStep inventoryStep;
    private final BalanceCreditStep balanceStep;

    private boolean executed;

    public MutationTransaction(
            UUID targetPlayerId,
            InventoryRemovalStep inventoryStep,
            BalanceCreditStep balanceStep
    ) {
        this.targetPlayerId = Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        this.inventoryStep = Objects.requireNonNull(inventoryStep, "inventoryStep");
        this.balanceStep = Objects.requireNonNull(balanceStep, "balanceStep");
    }

    /**
     * Execute the transaction atomically.
     *
     * Returns:
     * - Result.success() if and only if both steps applied
     * - Result.failure(...) if any step failed (all changes rolled back)
     *
     * This method is single-use.
     */
    public Result execute() {
        if (executed) {
            return Result.failure("TRANSACTION_ALREADY_EXECUTED");
        }
        executed = true;

        InventoryRemovalStep.ApplyResult inventoryResult = inventoryStep.apply();
        if (!inventoryResult.applied()) {
            return Result.failure("INVENTORY_REMOVAL_FAILED: " + inventoryResult.reason());
        }

        BalanceCreditStep.ApplyResult balanceResult = balanceStep.apply(targetPlayerId);
        if (!balanceResult.applied()) {
            inventoryStep.rollback();
            return Result.failure("BALANCE_CREDIT_FAILED: " + balanceResult.reason());
        }

        return Result.success();
    }

    /**
     * Explicit rollback entry point.
     *
     * This should only be called if a higher-level failure requires
     * reversing a completed transaction in future phases.
     */
    public void rollback() {
        balanceStep.rollback(targetPlayerId);
        inventoryStep.rollback();
    }

    // -------------------------
    // Types
    // -------------------------

    public record Result(boolean success, String reason) {

        public static Result success() {
            return new Result(true, null);
        }

        public static Result failure(String reason) {
            return new Result(false, Objects.requireNonNull(reason, "reason"));
        }
    }
}
