# Minecraft Gradle Tooling

## Tooling Selected

`bindings/inventory` now uses Fabric Loom as the Minecraft-aware Gradle tooling.

Pinned versions:

- Loom: `1.10.5`
- Minecraft: `1.20.1`
- Yarn mappings: `1.20.1+build.10`

Loom was selected because it provides Minecraft dependency and Yarn mapping configurations without requiring Fabric Loader/API as an implementation dependency.

## Fabric Loader/API

Fabric Loader and Fabric API were not added.

Loom itself is Fabric ecosystem tooling and adds its own internal repositories/configurations, but this module does not currently declare Fabric runtime/API coupling.

## Verification

`gradle :bindings:inventory:compileJava` succeeded.

The task configured Fabric Loom `1.10.5`, used the existing composite build for Coffer dependencies, and reported `:bindings:inventory:compileJava NO-SOURCE`, as expected because no Java implementation classes exist yet.

## Notes

An initial attempt with Loom `1.16.1` failed because that plugin version requires a Java 21 Gradle runtime while the current environment runs Gradle on Java 17.

The repository policy that rejected project repositories was removed because Loom adds its own `LoomLocalRemappedMods` repository during plugin application.

## Uncertainty

- Whether this repo should eventually move to Java 21 and a newer Loom line.
- Whether strict NBT descriptors remain sufficient if future Minecraft targets use component-based item data.
- Whether a future non-Fabric tooling option is preferable if multi-loader support becomes a priority.
