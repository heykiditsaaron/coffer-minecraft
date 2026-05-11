# Fabric Bootstrap Path Planning

## Summary

Documented the minimal dependency/bootstrap path required before future
Fabric-first implementation resumes.

## Decision

The repository should bootstrap against split substrate artifacts using a
published-artifact flow first, with `mavenLocal` snapshots as the default local
development path. The old monolithic composite-build assumption should be
removed before real Fabric implementation restarts.

## Rationale

The current build still assumes a sibling monolithic `../coffer` checkout and
hardcoded substrate coordinates. A published-artifact-first path preserves
substrate separation, avoids accidental duplication, and keeps future rewiring
cost low.

## Scope

Included:

- inspection of current Gradle structure
- identification of remaining monolithic-era dependency assumptions
- definition of the minimal bootstrap sequence
- recommendation of the safest initial dependency topology

Excluded:

- build-file rewiring
- dependency version changes
- source migration
- Fabric implementation work
- gameplay or adapter behavior

## Verification

Run:

```text
git status --short
git diff --check
```

## Uncertainties

- Canonical snapshot version naming for the split substrate repositories is not
  yet documented in this repository.
- Optional split-repo composite builds may still be useful later for focused
  API work, but they should remain secondary to artifact-based bootstrap.

