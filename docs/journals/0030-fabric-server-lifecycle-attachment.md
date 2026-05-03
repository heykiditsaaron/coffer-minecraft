# Fabric Server Lifecycle Attachment

## Summary

The Fabric entrypoint now attaches the active Minecraft server to the platform service during server lifecycle startup and detaches it during server stop.

## Server Reference Lifecycle

`CofferMinecraftFabricService` owns the current server reference.

The lifecycle is:

- mod initialization constructs and initializes the platform service
- `SERVER_STARTED` attaches the live `MinecraftServer`
- `SERVER_STOPPED` detaches the server reference
- service shutdown also clears the server reference

## Resolution Dependency

Player inventory resolution depends on the attached server reference.

If no server is attached, player inventory resolution returns unresolved inventory state. The existing binding container path then preserves unavailable/unknown semantics rather than reporting an inventory failure.

When a server is attached, resolution still requires server-thread execution before reading live player inventory.

## Boundaries

No execution surface, exchange execution, scheduler, adapter logic, inventory mutation, command, UI, persistence, rollback, retry behavior, or binding logic change was added.

## Fabric API Scope

The platform module now uses Fabric API lifecycle events for server attach/detach callbacks.

No commands, events beyond server lifecycle, gameplay systems, or adapter-facing APIs were added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- Server-thread scheduling remains future work.
- Shutdown behavior for unusual hot-reload or failed-start paths may need review before gameplay exposure.
- Future adapter discovery of the platform service remains undecided.
