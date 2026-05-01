# Minecraft Inventory Binding Consolidation Review

## Readiness Decision

The inventory binding is ready for thin TransferableValueAuthority collaborator integration.

It is not yet ready for gameplay or player-facing workflows.

## Gameplay Exposure Deferred

`applyAtomicSwap(...)` currently has no rollback or retry behavior.

If state changes after simulation, application returns `Unknown` and may already have partially mutated real inventory. Before real gameplay exposure, server-thread exclusivity, execution guards, or a transaction strategy must be addressed.

## Boundaries Confirmed

- No Fabric Loader/API dependency is declared.
- No player-trade adapter logic exists.
- Minecraft-specific descriptor, matcher, and container behavior remains in `bindings/inventory`.

## Coverage Confirmed

Tests cover:

- descriptor validation
- strict item id and full-NBT matching
- removability
- receivability
- non-mutating simulation
- guarded application
- drift and uncertainty behavior
- configured slot boundaries

## Non-Blocking Risks

- Access widener and test-only `-Xverify:none` remain test tooling debt.
- Java 17 with Loom 1.10.5 may need revisiting before release.
- Future Minecraft component-model changes may require descriptor changes.
- Slot-specific rules are still simplified to configured slot lists and native stack compatibility.
- Unsupported descriptor handling may need review before wrapper integration.

## Next Step

Implement thin TransferableValueAuthority collaborator integration without changing current binding semantics.
