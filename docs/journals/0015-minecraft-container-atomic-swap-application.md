# Minecraft Container Atomic Swap Application

## Decision

`MinecraftPlayerInventoryContainer` now implements atomic swap application for the player inventory binding.

Application is guarded by `simulateAtomicSwap(...)`. A failed simulation returns the same failure reason without mutating real inventory. An unknown simulation returns the same unknown reason without mutating real inventory.

## Application Order

After successful simulation, real mutation follows the same logical order as simulation:

1. remove this container's outgoing values
2. remove the other container's outgoing values
3. insert this container's removed stacks into the other container
4. insert the other container's removed stacks into this container

Success is reported only if every real mutation step completes.

## Uncertain Mutation

If real state changes after simulation and a mutation step cannot complete, application returns `Unknown` with the relevant removability or receivability reason code.

Rollback and retry are not implemented. Partial real mutation may already have occurred when an uncertainty is detected.

## Boundaries

Mutation remains limited to the configured inventory region/slot list. The implementation keeps strict item id plus full NBT identity through `MinecraftItemMatcher`.

No Fabric loader glue, adapter code, or Core/Runtime API changes were added.

## Verification

`gradle :bindings:inventory:test` passed.

## Remaining Risks

The no-rollback behavior must be acceptable before exposing this through gameplay workflows. Real server-side inventory contention may need a higher-level execution guard or transaction model later.
