# Fabric Platform Design

## Module Structure

Future module:

```text
platforms/fabric
```

`platforms/fabric` depends on `bindings/inventory` and wires it into a Fabric runtime environment.

Inventory logic remains in `bindings/inventory`. The Fabric module must not duplicate descriptor, matcher, removability, receivability, simulation, or application behavior.

## Responsibilities

The Fabric platform module owns:

- server lifecycle integration for startup and shutdown
- registering TransferableValueAuthority with the active Coffer runtime
- wiring Minecraft collaborators:
  - `MinecraftDescriptorFactory`
  - `MinecraftContainerResolver`
  - `MinecraftRuntimeValueSetResolver`
  - `MinecraftRuntimePayloadInterpreter`
- resolving real player inventories by UUID
- ensuring authority execution happens on the correct server thread

## Player Inventory Resolution

Actor identity remains:

```text
player:<uuid>:inventory:<region>
```

The Fabric resolver maps the UUID to a live server player, then maps the requested region to the corresponding inventory slot list for `MinecraftPlayerInventoryContainer`.

If the player is offline, unavailable, or the inventory cannot be read reliably, resolution should fail so the authority path reports unknown/unavailable rather than pretending the container exists.

## Threading Model

All inventory mutation must occur on the Minecraft server thread.

The Fabric module must guarantee that runtime execution enters TransferableValueAuthority on the server thread. If called off-thread, it should schedule onto the server thread or fail with an unknown/unavailable result rather than mutating directly.

No binding class should be responsible for thread scheduling.

## Runtime Authority Wiring

The Fabric module constructs `TransferableValueRuntimeAuthority` with:

- `MinecraftContainerResolver`
- `MinecraftRuntimeValueSetResolver`
- `MinecraftRuntimePayloadInterpreter`
- a runtime detail mapper preserving reason codes

Gameplay systems or future adapters invoke Coffer runtime execution at a high level. The Fabric module supplies the Minecraft-aware authority wiring, but does not implement adapter-specific workflows.

## Explicit Non-Goals

- No inventory logic in `platforms/fabric`.
- No matching logic in `platforms/fabric`.
- No descriptor interpretation beyond collaborator delegation.
- No player-trade adapter.
- No block inventory support.

## Uncertainties

- Exact Fabric lifecycle hooks.
- Exact server-thread execution guarantee mechanism.
- Offline player behavior and whether queued execution is ever allowed.
- How future adapters will discover or receive platform runtime services.
