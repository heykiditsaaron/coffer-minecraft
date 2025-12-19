package dev.coffer.adapter.fabric.execution;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * MUTATION CONTEXT — PHASE 3D.2
 *
 * Adapter-owned, immutable execution plan.
 *
 * This context represents the exact mutation the adapter intends
 * to perform if (and only if) Core evaluation allows it.
 *
 * Rules:
 * - Built ONLY from adapter-verified truth
 * - Immutable once created
 * - Never inferred or reconstructed
 * - Opaque to Core
 *
 * This class contains NO behavior.
 */
public final class MutationContext {

    private final UUID targetPlayerId;
    private final List<PlannedRemoval> plannedRemovals;

    public MutationContext(
            UUID targetPlayerId,
            List<PlannedRemoval> plannedRemovals
    ) {
        this.targetPlayerId = Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        this.plannedRemovals = List.copyOf(
                Objects.requireNonNull(plannedRemovals, "plannedRemovals")
        );

        if (this.plannedRemovals.isEmpty()) {
            throw new IllegalArgumentException("plannedRemovals must not be empty");
        }
    }

    public UUID targetPlayerId() {
        return targetPlayerId;
    }

    public List<PlannedRemoval> plannedRemovals() {
        return plannedRemovals;
    }

    /**
     * Represents a single, explicit inventory removal plan.
     *
     * This does NOT imply success, only intent.
     */
    public static final class PlannedRemoval {

        private final String itemId;
        private final int quantity;

        public PlannedRemoval(String itemId, int quantity) {
            if (itemId == null || itemId.isBlank()) {
                throw new IllegalArgumentException("itemId must be non-empty");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }

            this.itemId = itemId;
            this.quantity = quantity;
        }

        public String itemId() {
            return itemId;
        }

        public int quantity() {
            return quantity;
        }
    }
}
