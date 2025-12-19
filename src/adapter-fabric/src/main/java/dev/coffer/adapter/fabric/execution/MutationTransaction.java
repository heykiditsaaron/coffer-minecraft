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

    public Result execute() {
        if (executed) {
            return Result.fail("TRANSACTION_ALREADY_EXECUTED");
        }
        executed = true;

        InventoryRemovalStep.ApplyResult inv = inventoryStep.apply();
        if (!inv.success()) {
            return Result.fail("INVENTORY_REMOVAL_FAILED: " + inv.reason());
        }

        BalanceCreditStep.ApplyResult bal = balanceStep.apply(targetPlayerId);
        if (!bal.success()) {
            inventoryStep.rollback();
            return Result.fail("BALANCE_CREDIT_FAILED: " + bal.reason());
        }

        return Result.ok();
    }

    public void rollback() {
        balanceStep.rollback(targetPlayerId);
        inventoryStep.rollback();
    }

    public record Result(boolean success, String reason) {
        public static Result ok() {
            return new Result(true, null);
        }

        public static Result fail(String reason) {
            return new Result(false, Objects.requireNonNull(reason));
        }
    }
}
