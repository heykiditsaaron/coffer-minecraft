# Minecraft Player Resolution Runtime Fix

## Summary

Fabric player inventory resolution no longer calls `MinecraftServer#getPlayerManager()` directly during exchange arbitration.

## Decision

Resolve online players through Fabric API `PlayerLookup.all(server)` and match the live `ServerPlayerEntity#getUuid()` against the actor UUID before exposing inventory slots.

## Rationale

The previous direct server player-manager lookup compiled to `MinecraftServer.method_3760()`, which was missing in the observed Minecraft 1.20.1 runtime and caused `NoSuchMethodError` before the container could report unavailability. Scanning live server players avoids that direct call from coffer-minecraft and compares the UUID assigned to the loaded player entity, which covers normal and offline-mode UUIDs.

## Scope

Included:

- Fabric platform player lookup implementation.
- Removal of the temporary raw `System.out` submit probe.

Excluded:

- Adapter code.
- Exchange schema.
- Inventory binding semantics.

## Verification

- `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava`
- `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:build`
- Bytecode check confirmed `CofferMinecraftFabricService` calls `PlayerLookup.all(server)` and no longer calls `MinecraftServer#getPlayerManager()` directly.

## Uncertainties

Live verification is still required in the target server runtime to confirm the previous `NoSuchMethodError` no longer occurs during arbitration.
