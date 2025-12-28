package dev.coffer.adapter.fabric.execution.step;

import dev.coffer.adapter.fabric.execution.ExecutionResult;
import dev.coffer.adapter.fabric.execution.MutationContext;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Removes only the accepted items from the preview container, with rollback support.
 */
public final class ContainerRemovalStep {

    private final SimpleInventory inventory;
    private final int limit; // slots [0, limit) are sellable
    private final List<MutationContext.PlannedRemoval> removals;
    private final List<SlotSnapshot> snapshots = new ArrayList<>();
    private boolean applied;

    public ContainerRemovalStep(SimpleInventory inventory, int limit, List<MutationContext.PlannedRemoval> removals) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.limit = limit;
        this.removals = Objects.requireNonNull(removals, "removals");
    }

    public ExecutionResult apply() {
        if (applied) return ExecutionResult.fail("ALREADY_APPLIED");
        snapshots.clear();

        for (MutationContext.PlannedRemoval removal : removals) {
            Item item = resolve(removal.itemId());
            if (item == null) {
                rollback();
                return ExecutionResult.fail("UNKNOWN_ITEM");
            }
            int remaining = removal.quantity();
            remaining = drain(item, remaining);
            if (remaining > 0) {
                rollback();
                return ExecutionResult.fail("INSUFFICIENT_ITEMS");
            }
        }

        applied = true;
        return ExecutionResult.ok();
    }

    public void rollback() {
        for (int i = snapshots.size() - 1; i >= 0; i--) {
            SlotSnapshot snap = snapshots.get(i);
            inventory.setStack(snap.index, snap.stack.copy());
        }
        snapshots.clear();
        applied = false;
    }

    private int drain(Item item, int remaining) {
        int max = Math.min(limit, inventory.size());
        for (int i = 0; i < max && remaining > 0; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack == null || stack.isEmpty() || stack.getItem() != item) continue;

            snapshots.add(new SlotSnapshot(i, stack.copy()));
            int take = Math.min(stack.getCount(), remaining);
            stack.decrement(take);
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

    private record SlotSnapshot(int index, ItemStack stack) { }
}
