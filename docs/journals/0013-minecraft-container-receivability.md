# Minecraft Container Receivability

## Summary

`MinecraftPlayerInventoryContainer` now implements `canReceive(...)`.

The container remains limited to player inventory regions and does not implement simulation or application yet.

## Behavior

Receivability is non-mutating.

The implementation evaluates all incoming values together against the configured slots. It accounts for:

- Remaining capacity in compatible partial stacks.
- Empty slots.
- Native item max stack counts.
- Strict item id and full-NBT compatibility through `MinecraftItemMatcher`.

It returns success when every incoming value can fit. It returns `VALUE_NOT_RECEIVABLE` when there is not enough compatible stack or empty-slot capacity.

If the slot source cannot be resolved, it returns `CONTAINER_UNAVAILABLE`.

## Multi-Value Evaluation

Incoming descriptors with the same item identity are aggregated.

Different incoming values share the same empty-slot pool, so each value is not evaluated independently.

## Boundaries

No inventory mutation is performed.

No stacks are reordered.

No partial insertion is simulated.

`simulateAtomicSwap` and `applyAtomicSwap` remain intentionally unsupported.

## Verification

`gradle :bindings:inventory:test` succeeded.

Tests cover empty-slot acceptance, partial-stack capacity, full-stack plus empty-slot remainder, full inventory failure, NBT mismatch, multi-value shared capacity, read-only behavior, and non-Minecraft descriptor failure.

## Uncertainties

- Slot-specific acceptance rules are not modeled beyond current configured stack behavior.
- Real player inventory bridge may expose region-specific slot constraints that require refinement.
- Empty-slot max count is derived from the descriptor item id and current Minecraft registry behavior.
