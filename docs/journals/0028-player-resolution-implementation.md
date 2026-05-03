# Player Resolution Implementation

## Summary

The Fabric platform service now contains the live player inventory slot resolver used by `MinecraftContainerResolver`.

## Online Handling

When a server is attached to the service and resolution is invoked on the server thread, the resolver looks up the player UUID through the live server player manager.

If the player is online, the resolver returns live inventory-backed slot views for the requested region.

## Offline Handling

If the server is not attached, the call is off-thread, the player manager is unavailable, or the player is offline/not found, the resolver returns unresolved inventory state.

This preserves unavailable/unknown semantics through the existing binding container path rather than reporting an inventory failure.

## Container Regions

The resolver maps regions as follows:

- `main`: non-hotbar player main inventory slots
- `hotbar`: hotbar slots
- `armor`: armor slots
- `offhand`: offhand slots

The returned lists are live views over Minecraft inventory storage. The binding remains responsible for inventory semantics once slots are supplied.

## Boundaries

No exchange execution surface, scheduler, adapter logic, cache, inventory mutation logic, command, UI, persistence, rollback, retry behavior, or binding logic change was added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- The service now has package-private server attach/detach hooks, but no Fabric server lifecycle event wiring is added in this step.
- Server-thread scheduling remains future work.
- The exact long-term meaning of `main` versus `hotbar` may need confirmation if future gameplay expects `main` to include hotbar slots.
