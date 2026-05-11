# Fabric Bootstrap Path

## Summary

This document defines the safest minimal bootstrap path before any new
Fabric-first implementation resumes in `coffer-minecraft`.

The immediate goal is dependency and build discipline, not renewed platform
development. The repository must remain platform-specific and must consume the
split substrate repositories without recreating their logic locally.

## Current Build State

The current Gradle structure is small:

- root `settings.gradle`
- root `build.gradle`
- `bindings/inventory/build.gradle`
- `platforms/fabric/build.gradle`

The build still reflects monolithic-era assumptions:

- `settings.gradle` uses `includeBuild('../coffer')`
- dependency substitution points into monolithic-era project paths
- both subprojects declare hardcoded substrate coordinates at `1.0.0`
- local development assumes a sibling checkout with the old repository shape

The source tree also imports substrate packages such as `org.coffer.core`,
`org.coffer.runtime`, and
`org.coffer.firstparty.authority.transferablevalue.*`. Those imports are not a
monolithic assumption by themselves. They are expected dependency consumers and
should remain externalized.

## Remaining Old Assumptions

The remaining old dependency/bootstrap assumptions are:

1. `settings.gradle` assumes a sibling monorepo at `../coffer`.
2. `settings.gradle` assumes the authority project path
   `:first-party:authorities:transferable-value`.
3. `bindings/inventory/build.gradle` assumes the substrate artifacts are
   available as `dev.coffer:*:1.0.0`.
4. `platforms/fabric/build.gradle` assumes the same hardcoded substrate
   coordinates.
5. Historical journals `0004` and `0005` document the old composite-build
   strategy and should be treated as historical reference only.

These assumptions are the minimum set that must be replaced before real Fabric
implementation restarts.

## Minimal Bootstrap Changes Required

Before implementation resumes, the repo should make only these dependency
bootstrap changes:

1. Remove the monolithic `includeBuild('../coffer')` assumption from
   `settings.gradle`.
2. Replace hardcoded substrate coordinates with one explicit repository-level
   version declaration strategy.
3. Choose one default local development resolution path for substrate artifacts.
4. Document that path in `docs/integration`.
5. Prove that `bindings/inventory` and `platforms/fabric` can resolve substrate
   dependencies through the chosen path before any Fabric behavior work begins.

No new gameplay behavior, platform behavior, or local substrate shim should be
added as part of this bootstrap step.

## Recommended Dependency Topology

The safest initial topology is:

- `coffer-core`: consumed as a published artifact from the split substrate repo
- `coffer-runtime`: consumed as a published artifact from the split substrate
  repo
- `coffer-transferable-value-authority`: consumed as a published artifact from
  the split substrate repo

The Minecraft repo should depend only on artifact coordinates and versions, not
on internal project paths inside substrate repositories.

Recommended ownership shape:

- `bindings/inventory` depends on `coffer-core`,
  `coffer-transferable-value-authority`, and any `coffer-runtime` types it
  actually needs.
- `platforms/fabric` depends on `bindings/inventory`, `coffer-core`,
  `coffer-runtime`, and `coffer-transferable-value-authority`.
- root Gradle configuration owns only repository resolution and shared version
  declarations.

This keeps substrate separation explicit and avoids baking substrate repository
structure into the Minecraft repo.

## Recommended Early Development Approach

Use `mavenLocal` snapshot publishing from the split substrate repositories as
the default early re-foundation bootstrap path.

Why this is safest:

- It preserves repository separation cleanly.
- It avoids depending on substrate internal project paths.
- It reduces accidental circular development assumptions.
- It keeps `coffer-minecraft` honest about consuming public substrate artifacts.
- It minimizes future rewiring pain when published repository coordinates are
  used later.

Composite builds should be a secondary, opt-in workflow for tightly scoped
cross-repo API development only after the baseline artifact flow is proven.

If composite builds are later needed, they should target the split substrate
repositories individually and remain explicit. The repo should not return to a
single monolithic `../coffer` assumption.

## Safest Bootstrap Sequence

Before any new Fabric implementation begins:

1. Confirm canonical artifact coordinates and snapshot versioning for
   `coffer-core`, `coffer-runtime`, and
   `coffer-transferable-value-authority`.
2. Decide the single default local resolution path:
   `mavenLocal` snapshots first.
3. Centralize substrate versions in a small set of Gradle properties instead of
   repeating literal coordinates in multiple module build files.
4. Replace the monolithic composite-build assumption in `settings.gradle`.
5. Add integration documentation that explains how developers publish substrate
   snapshots locally and how `coffer-minecraft` resolves them.
6. Verify dependency resolution and compile readiness for `bindings/inventory`
   before touching new Fabric behavior.
7. Verify dependency resolution and compile readiness for `platforms/fabric`
   after the inventory module baseline is proven.
8. Only then begin intentional Fabric-first implementation work.

## What To Avoid

Do not do the following during bootstrap:

- copy substrate classes into this repository
- create local shim implementations to bypass missing artifacts
- depend on substrate internal project paths
- add cross-platform abstractions
- introduce additional platform modules
- restart gameplay or adapter work
- mix bootstrap cleanup with behavior changes

## Deferred

Deferred until after the bootstrap baseline is proven:

- optional split-repo composite build workflow
- dependency catalogs or richer Gradle convention cleanup
- source migration or API adaptation work
- Fabric implementation changes
- adapter-facing gameplay behavior

