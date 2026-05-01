# Player Inventory Resolution

## Input

The Fabric platform resolver receives a Coffer actor identifier, expected to map to the existing container identity shape:

```text
player:<uuid>:inventory:<region>
```

The UUID identifies the Minecraft player. The region identifies the logical inventory slice: `main`, `hotbar`, `armor`, or `offhand`.

## Resolution Process

The Fabric platform resolves the UUID against the live server player list.

Conceptually:

1. parse the actor/container identifier
2. extract player UUID and inventory region
3. look up the live server player by UUID
4. map the requested region to live inventory slots

If the player is online, resolution returns a live inventory-backed container.

If the player is offline or not found, resolution should not fabricate an inventory. The resolver should report unresolved so the authority path can classify the container as unavailable/unknown.

## Container Creation

Resolved slot lists are supplied to `MinecraftPlayerInventoryContainer`.

Supported regions remain:

- `main`
- `hotbar`
- `armor`
- `offhand`

The platform layer owns the mapping from Minecraft's real player inventory layout to these regions. The binding owns all inventory behavior once slots are supplied.

## Failure vs Unknown

Malformed actor/container identifiers are resolution failures before a Minecraft container exists.

Unavailable live state, such as an offline player or unreadable inventory, should map to unknown/unavailable semantics consistent with `MinecraftPlayerInventoryContainer.CONTAINER_UNAVAILABLE`.

Known inventory constraints after resolution, such as missing items or insufficient space, remain binding failures and must not be handled by the resolver.

## Threading Requirement

Player lookup and inventory slot access must occur on the Minecraft server thread.

If invoked off-thread, the platform layer should schedule resolution/execution onto the server thread or return an unresolved/unknown result. It must not read or mutate live inventory off-thread.

## Explicit Non-Goals

- No inventory matching logic.
- No stack or slot mutation.
- No descriptor interpretation beyond delegation.
- No adapter or gameplay workflow behavior.
- No offline inventory loading.

## Uncertainties

- Exact Fabric API and Minecraft server classes to use.
- Whether offline players should ever be queued, rejected, or loaded later.
- Whether resolver results should be cached or always live-looked-up.
- How cross-thread calls are represented and reported.
