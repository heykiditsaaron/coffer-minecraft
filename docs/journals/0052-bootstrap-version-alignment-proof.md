# Bootstrap Version Alignment Proof

## Summary

Aligned the centralized substrate version from `0.0.0-SNAPSHOT` to `1.0.0` and
reran the artifact-first compile proof.

## Decision

`1.0.0` was chosen as the bootstrap version because it is the currently
available substrate version in `mavenLocal` for:

- `dev.coffer:coffer-core`
- `dev.coffer:coffer-runtime`
- `dev.coffer:coffer-transferable-value-authority`

This is the smallest correct alignment step that preserves the artifact-first
bootstrap model and avoids reintroducing repository-shape coupling.

One additional metadata-only correction was required during proof:

- keep `mavenLocal` available to project-level resolution
- allow Loom-managed project repositories to remain active for Minecraft/Fabric
  dependencies

## Rationale

The prior failure was caused by a coordinate/version mismatch, not by proven
source incompatibility. Aligning the centralized version to the available local
artifacts is a non-behavioral bootstrap correction that allows the proof to
advance to the next real blocker.

## Scope

Included:

- updating the centralized substrate version
- correcting repository metadata so `mavenLocal` and Loom repositories can
  coexist
- updating bootstrap documentation
- rerunning `tasks`
- rerunning `:bindings:inventory:compileJava`
- rerunning `:platforms:fabric:compileJava`

Excluded:

- source migration
- behavior changes
- substrate publication changes
- broad API drift fixes

## Verification

Run:

```text
git status --short
git diff --check
./gradlew tasks
./gradlew :bindings:inventory:compileJava
./gradlew :platforms:fabric:compileJava
```

## Results

- `./gradlew tasks`: passed
- `./gradlew :bindings:inventory:compileJava`: passed
- `./gradlew :platforms:fabric:compileJava`: passed

The proof first exposed a repository-metadata issue after version alignment:
settings-only repository preference prevented Loom-managed Minecraft/Mojang
repositories from participating. That was corrected without behavior changes by
restoring Loom-managed repositories and adding `mavenLocal` at the project
level.

After that correction:

- dependency resolution was proven
- no substrate coordinate mismatch remained
- no source/API drift was exposed by `compileJava`

## Deferred

- broader test execution beyond `compileJava`
- source migration decisions, which remain unnecessary at this stage
- any Fabric feature work
- any gameplay or adapter work
