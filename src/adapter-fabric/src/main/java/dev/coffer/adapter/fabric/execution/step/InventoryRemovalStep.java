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

/**
 * INVENTORY REMOVAL STEP — PHASE 3D.3
 *
 * Applies inventory removals exactly as declared in an adapter-owned MutationContext.
 *
 * Rules (Phase 3D.3):
 * - No guessing, no inference.
 * - Must remove only what MutationContext planned.
 * - Must be rollback-capable (slot + stack snapshots).
 * - If full removal cannot be completed, it must rollback and report failure (Option C policy).
 *
 * Ownership boundary:
 * - This step only mutates the player's own inventory surfaces available via ServerPlayerEntity.
 * - Future mod-added "owned" slots are allowed, but must be integrated explicitly (no guessing).
 */
public final class InventoryRemovalStep {

    private final ServerPlayerEntity player;
    private final MutationContext mutationContext;

    private boolean applied;
    private final List<SlotSnapshot> snapshots = new ArrayList<>();

    public InventoryRemovalStep(ServerPlayerEntity player, MutationContext mutationContext) {
        this.player = Objects.requireNonNull(player, "player");
        this.mutationContext = Objects.requireNonNull(mutationContext, "mutationContext");
    }

    /**
     * Attempts to apply all planned removals.
     *
     * Returns:
     * - ApplyResult.applied() == true if and only if all planned removals were fully satisfied
     * - ApplyResult.applied() == false if any removal could not be completed; rollback is performed
     *
     * This method is single-use; calling apply twice is refused.
     */
    public ApplyResult apply() {
        if (applied) {
            return ApplyResult.failed("INVENTORY_STEP_ALREADY_APPLIED");
        }

        // Binding: this step must only act on the intended player.
        if (!player.getUuid().equals(mutationContext.targetPlayerId())) {
            return ApplyResult.failed("PLAYER_MISMATCH");
        }

        for (MutationContext.PlannedRemoval removal : mutationContext.plannedRemovals()) {
            ApplyResult r = applySingleRemoval(removal);
            if (!r.applied()) {
                // Option C policy: rollback and refuse (no faulting here).
                rollbackInternal();
                snapshots.clear();
                applied = false;
                return r;
            }
        }

        applied = true;
        return ApplyResult.applied();
    }

    /**
     * Rolls back any changes made by this step.
     *
     * Safe to call even if apply() failed and already rolled back; it will no-op if nothing was applied.
     */
    public void rollback() {
        rollbackInternal();
        snapshots.clear();
        applied = false;
    }

    private void rollbackInternal() {
        if (snapshots.isEmpty()) {
            return;
        }

        // Restore in reverse order of first-touch snapshots (defensive; ensures last-changed slots restore last).
        for (int i = snapshots.size() - 1; i >= 0; i--) {
            SlotSnapshot snap = snapshots.get(i);
            snap.target.set(snap.original.copy());
        }
    }

    private ApplyResult applySingleRemoval(MutationContext.PlannedRemoval removal) {
        final Item targetItem = resolveItem(removal.itemId());
        if (targetItem == null) {
            return ApplyResult.failed("UNKNOWN_ITEM_ID: " + removal.itemId());
        }

        int remaining = removal.quantity();

        // Removal surfaces MUST mirror the current truthful declaration surfaces:
        // - main
        // - armor
        // - offhand
        //
        // Future mod-added owned slots are allowed, but must be integrated explicitly.
        remaining = removeFromList(new SlotListRef(SlotListKind.MAIN, player.getInventory().main), targetItem, remaining);
        if (remaining > 0) {
            remaining = removeFromList(new SlotListRef(SlotListKind.ARMOR, player.getInventory().armor), targetItem, remaining);
        }
        if (remaining > 0) {
            remaining = removeFromList(new SlotListRef(SlotListKind.OFFHAND, player.getInventory().offHand), targetItem, remaining);
        }

        if (remaining != 0) {
            return ApplyResult.failed("INSUFFICIENT_OWNED_ITEMS: " + removal.itemId());
        }

        return ApplyResult.applied();
    }

    private int removeFromList(SlotListRef listRef, Item targetItem, int remaining) {
        if (remaining <= 0) {
            return 0;
        }

        DefaultedList<ItemStack> list = listRef.list;

        for (int i = 0; i < list.size() && remaining > 0; i++) {
            ItemStack stack = list.get(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() != targetItem) {
                continue;
            }

            // Snapshot this slot the first time we touch it (for rollback correctness).
            SlotTarget slotTarget = new SlotTarget(listRef.kind, list, i);
            if (!wasSnapshotted(slotTarget)) {
                snapshots.add(new SlotSnapshot(slotTarget, stack.copy()));
            }

            int take = Math.min(stack.getCount(), remaining);
            if (take <= 0) {
                continue;
            }

            stack.decrement(take);
            remaining -= take;
        }

        return remaining;
    }

    private boolean wasSnapshotted(SlotTarget target) {
        for (SlotSnapshot s : snapshots) {
            if (s.target.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves an itemId (e.g. "minecraft:dirt") to an Item using the registry.
     *
     * Returns null if:
     * - id is invalid
     * - id is not registered
     */
    private static Item resolveItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }

        final Identifier id;
        try {
            id = Identifier.of(itemId);
        } catch (Exception e) {
            return null;
        }

        if (!Registries.ITEM.containsId(id)) {
            return null;
        }

        return Registries.ITEM.get(id);
    }

    // -------------------------
    // Types
    // -------------------------

    public record ApplyResult(boolean applied, String reason) {
        public static ApplyResult applied() {
            return new ApplyResult(true, null);
        }

        public static ApplyResult failed(String reason) {
            return new ApplyResult(false, Objects.requireNonNull(reason, "reason"));
        }
    }

    private enum SlotListKind {
        MAIN,
        ARMOR,
        OFFHAND
    }

    private static final class SlotListRef {
        private final SlotListKind kind;
        private final DefaultedList<ItemStack> list;

        private SlotListRef(SlotListKind kind, DefaultedList<ItemStack> list) {
            this.kind = Objects.requireNonNull(kind, "kind");
            this.list = Objects.requireNonNull(list, "list");
        }
    }

    /**
     * Identifies a mutable slot target in a specific inventory surface list.
     */
    private static final class SlotTarget {
        private final SlotListKind kind;
        private final DefaultedList<ItemStack> list;
        private final int index;

        private SlotTarget(SlotListKind kind, DefaultedList<ItemStack> list, int index) {
            this.kind = Objects.requireNonNull(kind, "kind");
            this.list = Objects.requireNonNull(list, "list");
            this.index = index;
        }

        public void set(ItemStack stack) {
            list.set(index, stack);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SlotTarget other)) return false;
            return index == other.index && kind == other.kind && list == other.list;
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(list);
            result = 31 * result + kind.hashCode();
            result = 31 * result + index;
            return result;
        }
    }

    private record SlotSnapshot(SlotTarget target, ItemStack original) {
        private SlotSnapshot {
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(original, "original");
        }
    }
}
