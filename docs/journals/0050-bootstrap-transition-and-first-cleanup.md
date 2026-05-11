# Bootstrap Transition And First Cleanup

## Summary

Removed the old monolithic composite-build assumption, prepared the repository
for `mavenLocal`-first split-repo dependency consumption, and deleted discovery-era
documentation that no longer serves the new repository direction.

## Decision

The repository now resolves substrate artifacts through centralized coordinates
and `mavenLocal`-first repository resolution instead of assuming a sibling
monolithic `../coffer` checkout.

Discovery-era documentation was removed where it was either obsolete, already
archived externally, or no longer useful for near-term migration/reference work.

## Rationale

The new Minecraft super-platform repo should consume substrate boundaries
explicitly and avoid repository-shape coupling. Retaining obsolete monolithic
assumptions or large historical doc sets inside this repo would slow future
re-foundation work and blur current ownership boundaries.

## Scope

Included:

- removing `includeBuild('../coffer')` assumptions
- adding `mavenLocal`-first dependency resolution
- centralizing substrate coordinates in `gradle.properties`
- updating bootstrap documentation
- deleting stale discovery-era docs and journals

Excluded:

- publishing substrate artifacts
- compile/test migration work
- Fabric feature work
- gameplay or adapter behavior
- cross-platform abstractions

## Removed

- monolithic composite-build substitution from `settings.gradle`
- stale adapter/integration-era docs that no longer match the repo direction
- discovery-era journals `0001` through `0046`
- obsolete early decision records tied to the old repository phase

## Preserved

- current architecture and bootstrap docs
- current re-foundation journals `0047` and later
- inventory and Fabric source that may still assist near-term migration
- Minecraft-specific architecture notes that still describe current boundaries

## Verification

Run:

```text
git status --short
git diff --check
./gradlew tasks
```

## Deferred

- local publication of actual substrate snapshot artifacts
- first compile/test proof against split substrate dependencies
- source migration or deletion beyond this initial cleanup
- new Fabric implementation work

