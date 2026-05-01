# 0001: Bindings Before Adapters

## Decision

Implement the Minecraft inventory binding before scaffolding or implementing the player-trade adapter.

## Rationale

Player trade depends on reliable inventory semantics: stack descriptor mapping, container resolution, slot boundaries, item equivalence, simulation, application, and reason reporting.

Starting with the binding keeps the first implementation focused on adapting Minecraft inventory behavior to TransferableValueAuthority ports without prematurely designing a gameplay workflow.

## Consequences

The initial repository structure includes `bindings/inventory` only. Player-trade adapter directories and Fabric platform glue are intentionally deferred.
