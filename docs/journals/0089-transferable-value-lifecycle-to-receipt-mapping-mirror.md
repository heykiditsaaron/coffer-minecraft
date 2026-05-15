# 0089 Transferable-Value Lifecycle to Receipt Mapping Mirror

## Summary

Added a local mirror of the transferable-value lifecycle to receipt mapping
pressure test so repo-local `coffer-minecraft` work can reason about the
substrate mapping without depending on cross-repo access.

## Decision

Keep the mirror in `docs/architecture` as a reference artifact, and keep the
canonical substrate source in `dev/coffer`.

The source remains
`dev/coffer/docs/journals/0073-transferable-value-lifecycle-to-receipt-mapping-pressure-test.md`.

## Rationale

This repository will not always have reliable access to the substrate checkout,
but future collaborators still need the lifecycle-to-receipt mapping close to
the Minecraft/Fabric boundary work.

Duplicating the mapping here is intentional because it preserves reasoning
continuity for repo-local agents while avoiding redefinition of the substrate
model.

The mirror is not independently canonical. It exists only so Minecraft-specific
work can adapt the mapping in place rather than reconstruct it from memory or
drift into conflicting terminology.

## Scope

Included:
- mirrored lifecycle-to-receipt mapping pressure test
- substrate provenance note at the top of the mirror
- local orientation for future `coffer-minecraft` collaborators and agents
- explicit statement that Minecraft/Fabric work must adapt the mapping, not
  redefine it

Excluded:
- code changes
- schema changes
- gameplay changes
- UI changes
- lifecycle mapping rewrites
- alternate terminology
- divergence from the substrate source

## What Must Not Diverge

The following must remain aligned with the substrate source:

- which lifecycle states are receipt-worthy by default
- which states remain temporary visibility or deferred pressure
- participant/admin distinction
- omission and anti-counterfeit rules
- uncertainty visibility
- the provisional character of the mapping

## Minecraft-Specific Work Remaining

This mirror does not solve the Minecraft/Fabric adaptation problem.

Remaining work here is to:

- adapt the substrate mapping to Minecraft-facing interaction surfaces
- preserve truthful omission in Fabric-facing presentation
- align local binding and runtime reasoning with the mapping
- keep the substrate model and the Minecraft implementation boundary distinct

## Verification

Verification for this step consists of:
- `git status --short`
- `git diff --check`

## Uncertainties

This mirror should be refreshed if the substrate source changes in a way that
materially alters the lifecycle-to-receipt mapping.

It should not become a parallel doctrine or a place to improvise new receipt
or lifecycle categories without a corresponding substrate change.
