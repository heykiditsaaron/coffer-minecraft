package dev.coffer.adapter.fabric.execution;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * MUTATION CONTEXT
 *
 * Responsibility:
 * - Immutable adapter-owned execution plan for inventory removals.
 *
 * Invariants:
 * - Built only from adapter-verified truth.
 * - targetPlayerId non-null; plannedRemovals non-empty and defensive copy.
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
