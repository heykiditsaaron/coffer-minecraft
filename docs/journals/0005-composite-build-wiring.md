# Composite Build Wiring

## Summary

Local development now uses a Gradle composite build to consume the sibling `../coffer` repository.

The inventory binding module declares dependencies on:

- `dev.coffer:coffer-core:1.0.0`
- `dev.coffer:coffer-runtime:1.0.0`
- `dev.coffer:coffer-transferable-value-authority:1.0.0`

`settings.gradle` substitutes those module dependencies with projects from `../coffer`.

## Scope

No Minecraft or Fabric dependencies were added.

No Java implementation classes were added.

No adapters were scaffolded.

## Verification

`gradle projects` succeeded and showed `:coffer` as an included build.

`gradle :bindings:inventory:compileJava` succeeded. The task compiled the substituted sibling `coffer` projects and reported `:bindings:inventory:compileJava NO-SOURCE`, as expected because no Java implementation classes exist yet.

## Uncertainty

Release-time artifact publication and versioning remain deferred.
