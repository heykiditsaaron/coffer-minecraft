# Fabric Platform Shell

## Summary

The minimal Fabric platform shell has been added.

The shell includes a Fabric entrypoint and a server-scoped service placeholder for future Coffer authority wiring.

## Added Shell

- `CofferMinecraftFabricEntrypoint`
- `CofferMinecraftFabricService`
- `fabric.mod.json`

The entrypoint initializes only the service placeholder.

## Dependencies

`platforms/fabric` continues to depend on `:bindings:inventory`.

Fabric Loader was added as a compile-only dependency to compile the `ModInitializer` entrypoint. Fabric API was not added because no lifecycle callbacks, commands, events, or platform services are implemented yet.

## Boundaries

No gameplay adapter logic was added.

No Coffer request execution was added.

No player inventory resolver, server-thread scheduler, persistence, command, UI, rollback, or retry behavior was implemented.

No inventory binding logic was duplicated.

## Verification

`gradle :platforms:fabric:compileJava` passed.

## Remaining Work

- Fabric lifecycle startup/shutdown hooks.
- Authority construction and registration.
- Real player inventory resolution.
- Server-thread execution scheduling.
- Future adapter-facing service boundary.
