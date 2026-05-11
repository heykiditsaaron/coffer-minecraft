# Substrate Bootstrap

## Purpose

This document records the intended early re-foundation dependency path between
`coffer-minecraft` and the split substrate repositories.

## Default Local Workflow

Default local development should use published substrate artifacts from:

- `coffer-core`
- `coffer-runtime`
- `coffer-transferable-value-authority`

The initial local resolution mechanism should be `mavenLocal`.

This keeps `coffer-minecraft` consuming artifact boundaries rather than
subproject paths from substrate repositories.

The repository now expects the bootstrap coordinates to be centralized through
Gradle properties:

- `coffer_group`
- `coffer_version`

Subprojects should consume those properties rather than hardcoding literal
substrate versions independently.

## Why `mavenLocal` First

`mavenLocal` is the safest early bootstrap choice because it:

- preserves repository separation
- keeps dependency expectations explicit
- avoids monolithic composite-build assumptions
- reduces accidental coupling to substrate repository internals

## Secondary Workflow

If active cross-repo API work later requires tighter iteration speed, an
explicit split-repository composite-build workflow may be added later.

That workflow should remain optional. It should not become the default bootstrap
assumption for `coffer-minecraft`.

The repository no longer assumes `includeBuild('../coffer')` or substrate
subproject substitution paths from a monolithic checkout.

## Preconditions Before Implementation

Before real Fabric implementation resumes, this repository should have:

- confirmed substrate coordinates
- confirmed snapshot versioning
- one documented default artifact resolution path
- removed monolithic `../coffer` assumptions from Gradle settings
- successful dependency resolution for `bindings/inventory`
- successful dependency resolution for `platforms/fabric`

## Current Bootstrap Boundary

This repository is now prepared for artifact-first bootstrap, but it still
depends on locally published or otherwise resolvable substrate artifacts being
present for compile or test work.

That artifact publication and first compile verification remain separate follow-up
steps.

## Dependency Proof Status

The first compile proof against the artifact-first bootstrap model established
that the repository had been configured to expect:

- `dev.coffer:coffer-core:0.0.0-SNAPSHOT`
- `dev.coffer:coffer-runtime:0.0.0-SNAPSHOT`
- `dev.coffer:coffer-transferable-value-authority:0.0.0-SNAPSHOT`

At the time of that proof attempt, `mavenLocal` contained only `1.0.0` artifacts
for those substrate modules, not the expected `0.0.0-SNAPSHOT` coordinates.

That means the current blocker is coordinate/version availability, not yet
source-level API drift. Source compatibility cannot be evaluated until matching
artifacts are published or the agreed bootstrap coordinates are updated.

The bootstrap version has now been aligned to the currently available local
artifacts:

- `dev.coffer:coffer-core:1.0.0`
- `dev.coffer:coffer-runtime:1.0.0`
- `dev.coffer:coffer-transferable-value-authority:1.0.0`

This is the minimal non-behavioral alignment because those exact coordinates are
already present in `mavenLocal`, and no monolithic or local-project-path
assumptions are required to consume them.

Compile proof also established one additional bootstrap rule: `mavenLocal` must
be available to project-level dependency resolution alongside Loom-managed
repositories. Relying on settings-level repositories alone is not sufficient for
this build because Loom contributes required Minecraft and Mojang repositories at
the project level.

With those metadata alignments in place, the repository successfully reached:

- `./gradlew :bindings:inventory:compileJava`
- `./gradlew :platforms:fabric:compileJava`

That proves dependency resolution is working against the current local split
substrate artifacts and that the current inventory binding and Fabric source
compile against them at the current bootstrap boundary.
