# Minecraft Coffer Collaborators

## Summary

The inventory binding now includes thin collaborators for TransferableValueAuthority Core and Runtime integration.

Added collaborators:

- `MinecraftDescriptorFactory`
- `MinecraftContainerResolver`
- `MinecraftRuntimeValueSetResolver`
- `MinecraftRuntimePayloadFactory`
- `MinecraftRuntimePayloadInterpreter`

## Delegation Boundary

The collaborator layer performs only shape validation, descriptor construction, actor/container resolution, runtime value-set conversion, and minimal runtime payload pass-through.

It does not duplicate inventory behavior. Matching, stacking, removability, receivability, simulation, and application remain delegated to:

- `MinecraftItemDescriptor`
- `MinecraftItemMatcher`
- `MinecraftPlayerInventoryContainer`

## Scope

Only player inventory container references using the canonical `player:<uuid>:inventory:<region>` identity are supported.

No Fabric Loader/API dependency, player-trade adapter, or gameplay workflow was added.

## Verification

`gradle :bindings:inventory:test` passed.

## Readiness

The binding is ready to be wired into TransferableValueAuthority Core/Runtime wrappers using these collaborators, without changing binding semantics.

## Uncertainties

- Runtime payload shape may need more fields once real platform wiring exists.
- Unsupported actor/container and malformed descriptor cases currently surface as unresolved optionals for the authority wrappers to classify.
- Invalid NBT is preserved at descriptor construction time and remains a binding-level match/receive failure later.
