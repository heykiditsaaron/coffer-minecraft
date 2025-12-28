package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.BalanceDebitStep;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Executes a BUY mutation atomically: debit currency then grant items, with rollback on failure.
 */
public final class ShopPurchaseTransactionExecutor {

    private final BalanceStore balanceStore;
    private final FabricAuditSink auditSink;

    public ShopPurchaseTransactionExecutor(BalanceStore balanceStore, FabricAuditSink auditSink) {
        this.balanceStore = Objects.requireNonNull(balanceStore, "balanceStore");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
    }

    public ExecutionResult execute(ServerPlayerEntity player, ShopPurchasePlan plan) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(plan, "plan");

        if (!player.getUuid().equals(plan.playerId())) {
            return ExecutionResult.fail("PLAYER_MISMATCH");
        }

        // Debit step
        BalanceDebitStep debit = new BalanceDebitStep(balanceStore, plan.cost(), plan.currencyId(), auditSink);
        ExecutionResult debitResult = debit.apply(plan.playerId());
        if (!debitResult.success()) {
            return ExecutionResult.fail("BALANCE_DEBIT_FAILED: " + debitResult.reason());
        }

        // Grant items
        Item item;
        try {
            item = Registries.ITEM.get(Identifier.of(plan.itemId()));
        } catch (Exception e) {
            debit.rollback(plan.playerId());
            return ExecutionResult.fail("UNKNOWN_ITEM");
        }
        if (item == null) {
            debit.rollback(plan.playerId());
            return ExecutionResult.fail("UNKNOWN_ITEM");
        }
        ItemStack stack = new ItemStack(item, plan.quantity());
        boolean inserted = player.getInventory().insertStack(stack);
        if (!inserted) {
            debit.rollback(plan.playerId());
            return ExecutionResult.fail("INVENTORY_FULL");
        }

        auditSink.emitAdmin("SHOP_BUY", player.getUuid() + " bought " + plan.quantity() + "x " + plan.itemId() + " for " + plan.cost() + " (" + plan.currencyId() + ") in shop " + plan.shopId());
        return ExecutionResult.ok();
    }
}
