# Fabric Lifecycle Init

## Summary

The Fabric entrypoint now initializes a single platform service during mod initialization.

## Service Lifetime

`CofferMinecraftFabricEntrypoint` owns a single static service reference. The service is constructed on initialization and retained for the lifetime of the loaded Fabric entrypoint.

No public gameplay-facing service API is exposed yet.

## Authority Wiring

`CofferMinecraftFabricService` now records placeholder state for future Coffer Core, Coffer Runtime, and TransferableValue authority wiring.

No authorities are constructed yet.

## Boundaries

No player inventory resolution, exchange execution, adapter logic, threading scheduler, inventory mutation, command, UI, persistence, rollback, or retry behavior was added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- Exact server startup hook remains limited to the current Fabric `ModInitializer` entrypoint.
- Future adapter discovery of the platform service remains undecided.
- Server stop/shutdown lifecycle wiring remains future work.
