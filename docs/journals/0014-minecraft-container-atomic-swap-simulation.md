# Minecraft Container Atomic Swap Simulation

## Summary

`MinecraftPlayerInventoryContainer` now implements `simulateAtomicSwap(...)`.

Application remains intentionally unsupported.

## Behavior

Simulation is non-mutating. Both containers are copied into working slot state before any simulated removal or insertion occurs.

The simulated swap order is:

1. Remove this container's outgoing values from this working state.
2. Remove the other container's outgoing values from the other working state.
3. Insert this container's outgoing values into the other working state.
4. Insert the other container's outgoing values into this working state.

If any step fails, simulation returns the corresponding failure reason.

## Identity and Capacity

Simulation reuses the existing matcher and capacity helpers.

Item identity remains strict item id plus full NBT equality.

Incoming values are aggregated and evaluated against shared stack and empty-slot capacity.

## Boundaries

No real inventory mutation is performed.

No rollback is implemented because no real mutation occurs.

`applyAtomicSwap(...)` still throws `UnsupportedOperationException`.

## Verification

`gradle :bindings:inventory:test` succeeded.

Tests cover successful simulation, non-mutation, failed removal from either side, failed receive into either side, NBT mismatch, multi-value all-or-nothing fit, and unsupported application.

## Uncertainties

- Real player inventory slot bridging may require region-specific insertion constraints.
- Simulation currently supports only `MinecraftPlayerInventoryContainer` peers.
- Application must still define concrete mutation order and failure handling.
