# Inventory Binding Dev Namespace

## Summary

The inventory binding now exposes named dev classes to local Gradle consumers used by Fabric dev runtimes.

## Decision

Use the binding `jar` task's named dev output for the binding's API, named, and runtime outgoing variants, and have the Fabric platform depend on the binding's `namedElements` variant directly.

## Rationale

Fabric Loom exposes the inventory binding's remapped jar at `build/libs/inventory.jar` and its named development jar at `build/devlibs/inventory-dev.jar`. The adapter `runServer` environment runs in the Yarn named namespace, but the transitive project dependency from the Fabric platform selected the binding runtime variant and leaked intermediary Minecraft references such as `net/minecraft/class_1799`.

Selecting named output for local consumable variants keeps the binding classes in the same namespace as the dev server runtime without changing exchange schema, adapter behavior, bundling, or the substrate model.

## Scope

Included:
- Inventory binding API, named, and runtime outgoing variant artifact wiring.
- Fabric platform Gradle dependency wiring for `bindings/inventory`.

Excluded:
- Exchange schema changes.
- Adapter runtime behavior changes.
- Bundling or shading coffer-minecraft.
- Inventory authority semantics.

## Verification

- `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle clean runServer` from `coffer-adapter-player-trade`
  - Reached Minecraft server startup `Done`.
  - Loaded `coffer-minecraft 0.1.0`.
  - Logged Coffer Fabric platform initialization, authority wiring, and server attachment.
  - Did not fail with `NoClassDefFoundError: net/minecraft/class_1799`.
  - The server was stopped with `stop` and Gradle completed successfully.
- Adapter `build/loom-cache/argFiles/runServer` contains `/home/aaron/dev/coffer-minecraft/bindings/inventory/build/devlibs/inventory-dev.jar` for the inventory binding.
- Bytecode check on `bindings/inventory/build/devlibs/inventory-dev.jar` found no `net/minecraft/class_1799` reference in `MinecraftPlayerInventoryContainer`.
- `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle test`
  - Passed.

## Uncertainties

Production publication metadata may need separate review before a release flow depends on Gradle module variants.
