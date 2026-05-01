# Minecraft Container Removability

## Summary

`MinecraftPlayerInventoryContainer` now implements the first container slice: `canRemove(...)`.

The container is limited to player inventory regions:

- `main`
- `hotbar`
- `armor`
- `offhand`

## Behavior

Removability is read-only.

The implementation iterates configured inventory slots, uses `MinecraftItemMatcher` for strict item id and full-NBT identity matching, and sums matching stack counts.

It returns success when available quantity is at least the requested quantity. It returns `VALUE_NOT_REMOVABLE` when matching quantity is missing or insufficient.

If the slot source cannot be resolved, it returns `CONTAINER_UNAVAILABLE`.

## Boundaries

No inventory mutation is performed.

Stacks are not reordered.

Removal is not partially simulated.

`canReceive`, `simulateAtomicSwap`, and `applyAtomicSwap` remain intentionally unsupported.

## Verification

`gradle :bindings:inventory:test` succeeded.

Tests cover single-stack success, multi-stack aggregation, insufficient quantity, no matching items, NBT mismatch, empty inventory, and read-only behavior.

## Uncertainties

- The concrete bridge to real player inventory slots is still future work.
- The unavailable-container path is present but not yet connected to runtime resolution.
- Duplicate descriptors with the same item identity are aggregated, but broader multi-value behavior may need refinement with future authority workflows.
