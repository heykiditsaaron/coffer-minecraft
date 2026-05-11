# Artifact-First Dependency Proof

## Summary

Ran the first compile proof against the artifact-first bootstrap model for
`bindings/inventory` and `platforms/fabric`.

## Decision

The current bootstrap state is not yet compile-ready because the repository
expects split substrate artifacts at `0.0.0-SNAPSHOT`, while `mavenLocal`
currently contains only `1.0.0` artifacts for the required substrate modules.

## Rationale

The purpose of this step was to prove dependency resolution explicitly before any
source migration or Fabric implementation work resumed. The attempt showed that
the first blocking failure occurs before Java compilation, so the current issue
is bootstrap coordinate availability rather than confirmed source/API drift.

## Scope

Included:

- inspecting current Gradle coordinates
- checking available `mavenLocal` substrate artifacts
- running `./gradlew tasks`
- running `./gradlew :bindings:inventory:compileJava`
- running `./gradlew :platforms:fabric:compileJava`
- categorizing the resulting failures

Excluded:

- source fixes
- dependency coordinate changes
- substrate publication changes
- Fabric implementation work

## Results

- `./gradlew tasks`: passed
- `:bindings:inventory:compileJava`: failed at dependency resolution
- `:platforms:fabric:compileJava`: failed at the same dependency boundary via
  the inventory module path

The missing artifacts were:

- `dev.coffer:coffer-core:0.0.0-SNAPSHOT`
- `dev.coffer:coffer-runtime:0.0.0-SNAPSHOT`
- `dev.coffer:coffer-transferable-value-authority:0.0.0-SNAPSHOT`

## Verification

Run:

```text
git status --short
git diff --check
./gradlew tasks
./gradlew :bindings:inventory:compileJava
./gradlew :platforms:fabric:compileJava
```

## Deferred

- publishing matching snapshot artifacts to `mavenLocal`
- deciding whether the bootstrap version should remain `0.0.0-SNAPSHOT` or be
  updated to the actual published split-repo version
- source/API drift evaluation after dependency resolution is restored
- any Fabric or gameplay implementation work

