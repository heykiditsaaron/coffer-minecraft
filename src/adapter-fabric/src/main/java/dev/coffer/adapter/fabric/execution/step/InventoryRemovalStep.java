package dev.coffer.adapter.fabric.execution.step;

import dev.coffer.adapter.fabric.execution.MutationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InventoryRemovalStep {

    private final ServerPlayerEntity player;
    private final MutationContext mutationContext;

    private boolean applied;
    private final List<SlotSnapshot> snapshots = new ArrayList<>();

    public InventoryRemovalStep(ServerPlayerEntity player, MutationContext mutationContext) {
        this.player = Objects.requireNonNull(player);
        this.mutationContext = Objects.requireNonNull(mutationContext);
    }

    public ApplyResult apply() {
        if (applied) return ApplyResult.fail("ALREADY_APPLIED");
        if (!player.getUuid().equals(mutationContext.targetPlayerId())) {
            return ApplyResult.fail("PLAYER_MISMATCH");
        }

        for (MutationContext.PlannedRemoval r : mutationContext.plannedRemovals()) {
            ApplyResult res = applySingle(r);
            if (!res.success()) {
                rollbackInternal();
                return res;
            }
        }

        applied = true;
        return ApplyResult.ok();
    }

    public void rollback() {
        rollbackInternal();
        snapshots.clear();
        applied = false;
    }

    private void rollbackInternal() {
        for (int i = snapshots.size() - 1; i >= 0; i--) {
            snapshots.get(i).restore();
        }
    }

    private ApplyResult applySingle(MutationContext.PlannedRemoval removal) {
        Item item = resolve(removal.itemId());
        if (item == null) return ApplyResult.fail("UNKNOWN_ITEM");

        int remaining = removal.quantity();
        remaining = drain(player.getInventory().main, item, remaining);
        remaining = drain(player.getInventory().armor, item, remaining);
        remaining = drain(player.getInventory().offHand, item, remaining);

        if (remaining > 0) {
            return ApplyResult.fail("INSUFFICIENT_ITEMS");
        }
        return ApplyResult.ok();
    }

    private int drain(DefaultedList<ItemStack> list, Item item, int remaining) {
        for (int i = 0; i < list.size() && remaining > 0; i++) {
            ItemStack s = list.get(i);
            if (s == null || s.isEmpty() || s.getItem() != item) continue;

            snapshots.add(new SlotSnapshot(list, i, s.copy()));

            int take = Math.min(s.getCount(), remaining);
            s.decrement(take);
            remaining -= take;
        }
        return remaining;
    }

    private static Item resolve(String id) {
        try {
            return Registries.ITEM.get(Identifier.of(id));
        } catch (Exception e) {
            return null;
        }
    }

    public record ApplyResult(boolean success, String reason) {
        public static ApplyResult ok() {
            return new ApplyResult(true, null);
        }

        public static ApplyResult fail(String reason) {
            return new ApplyResult(false, Objects.requireNonNull(reason));
        }
    }

    private record SlotSnapshot(DefaultedList<ItemStack> list, int index, ItemStack original) {
        void restore() {
            list.set(index, original.copy());
        }
    }
}
