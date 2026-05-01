# Minecraft Version and Dependency Plan

This is a first-pass dependency plan for `bindings/inventory`. It records direction before adding Minecraft dependencies or Java implementation.

## 1. Target Minecraft Version

Use Minecraft `1.20.1` as the initial implementation target.

Rationale: it is a stable modding baseline and keeps the first inventory binding on the NBT-era item model already assumed by the descriptor journals.

## 2. Mapping Strategy

Use Yarn named mappings for readable Minecraft inventory and item APIs during development.

The exact Yarn build should be pinned in `gradle.properties` when Gradle is updated. Do not use floating mapping versions.

## 3. Direct Minecraft Types

`bindings/inventory` may depend directly on Minecraft types when needed for item and inventory semantics, especially `ItemStack`, inventory/slot abstractions, item identifiers, stack limits, and NBT access.

The module should still keep Minecraft-specific interpretation local to the binding and avoid leaking Minecraft types into platform-neutral Coffer APIs.

## 4. Fabric Loader/API Placement

Do not add Fabric Loader or Fabric API to `bindings/inventory` yet.

Fabric loader glue belongs in a future `platforms/fabric` module. The inventory binding should stay focused on Minecraft item/container semantics and TransferableValueAuthority ports.

## 5. Likely Gradle Setup

Likely later setup:

- Apply Java support already present in `bindings/inventory`.
- Add Minecraft dependency and Yarn mappings through a Minecraft-aware Gradle plugin.
- Prefer Fabric Loom as the initial tooling path if it can provide Minecraft dependencies and mappings without adding Fabric Loader/API runtime coupling to the binding.
- Keep Coffer dependencies as already wired through the composite build.

No publishing setup is needed for this step.

## 6. Deferred

- Fabric Loader and Fabric API dependencies.
- `platforms/fabric` module.
- Player-trade adapter.
- Block inventories and screen-specific containers.
- Multi-version Minecraft support.
- Release artifact publication workflow.

## 7. Risks

- Minecraft `1.20.5+` changes item data toward components, while the current descriptor plan assumes full NBT payload preservation.
- Strict full-NBT equality may need a different representation for component-based versions.
- Mapping names can drift across Yarn builds.
- Direct Minecraft types in `bindings/inventory` may make later multi-loader support harder if the boundary is not kept tight.
- Test fixtures may need lightweight fake inventory abstractions to avoid requiring full game runtime startup.

## 8. Decision

Start with `bindings/inventory` targeting Minecraft `1.20.1`, Yarn mappings, and direct Minecraft types only where required for inventory semantics.

Defer Fabric Loader/API and runtime glue until a separate platform module is introduced.
