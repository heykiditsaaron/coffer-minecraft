package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.InventoryRemovalStep;

import java.util.Objects;
import java.util.UUID;

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
        this.targetPlayerId = Objects.requireNonNull(targetPlayerId);
        this.inventoryStep = Objects.requireNonNull(inventoryStep);
        this.balanceStep = Objects.requireNonNull(balanceStep);
    }

    public ExecutionResult execute() {
        if (executed) {
            return ExecutionResult.fail("TRANSACTION_ALREADY_EXECUTED");
        }
        executed = true;

        ExecutionResult inv = inventoryStep.apply();
        if (!inv.success()) {
            return ExecutionResult.fail("INVENTORY_REMOVAL_FAILED: " + inv.reason());
        }

        ExecutionResult bal = balanceStep.apply(targetPlayerId);
        if (!bal.success()) {
            inventoryStep.rollback();
            return ExecutionResult.fail("BALANCE_CREDIT_FAILED: " + bal.reason());
        }

        return ExecutionResult.ok();
    }

    public void rollback() {
        balanceStep.rollback(targetPlayerId);
        inventoryStep.rollback();
    }

}
