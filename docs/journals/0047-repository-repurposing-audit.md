# Repository Repurposing Audit

## Summary

Added a read-only architecture audit for repurposing `coffer-minecraft` into the
Minecraft super-platform repository.

## Decision

The repository should own Minecraft-specific bindings, platform implementations,
documentation, contracts, and tests. Core, Runtime, and TransferableValue
substrate behavior must remain in separate substrate repositories and be consumed
as dependencies.

## Rationale

The current tree contains useful Minecraft inventory and Fabric implementation
material, but its docs and build wiring still reflect an earlier monolithic
repository assumption. A scoped audit gives the cleanup a stable sequence before
files are deleted or implementation boundaries are rewritten.

## Scope

Included:

- Cleanup recommendations.
- Temporary preservation guidance.
- Target repository topology.
- Fabric-first module location.
- Immediate documentation recommendations.
- Substrate dependency guidance.
- Non-duplication boundaries.
- AGENTS.md guidance.
- Deferred work.

Excluded:

- Source code changes.
- File deletion.
- Dependency rewiring.
- README or AGENTS.md replacement.
- New platform or adapter implementation.

## Verification

Run:

```text
git diff --check
```

## Uncertainties

- Exact published coordinates or included-build paths for the split substrate
  repositories are not confirmed in this repo.
- Existing implementation source may need API updates after dependency wiring is
  moved away from the old monolithic `../coffer` assumption.

