# 0088 Minimal Lawful Transaction Receipt Topology Mirror

## Summary

Added a local mirror of the minimal lawful transaction receipt topology so
repo-local `coffer-minecraft` work can reason about the substrate receipt
model without depending on cross-repo access.

## Decision

Keep the mirror in `docs/architecture` as a reference artifact, and keep the
canonical substrate source in `dev/coffer`.

The source remains
`dev/coffer/docs/journals/0072-minimal-lawful-transaction-receipt-topology.md`.

## Rationale

This repository will not always have reliable access to the substrate checkout,
but future collaborators still need the receipt topology close to the
Minecraft/Fabric boundary work.

Duplicating the topology here is intentional because it preserves reasoning
continuity for repo-local agents while avoiding redefinition of the substrate
model.

The mirror is not independently canonical. It exists only so Minecraft-specific
work can adapt the receipt topology in place rather than reconstruct it from
memory or drift into conflicting terminology.

## Scope

Included:
- mirrored minimal lawful transaction receipt topology
- substrate provenance note at the top of the mirror
- local orientation for future `coffer-minecraft` collaborators and agents
- explicit statement that Minecraft/Fabric work must adapt the topology, not
  redefine it

Excluded:
- code changes
- schema changes
- gameplay changes
- UI changes
- receipt topology rewrites
- alternate terminology
- divergence from the substrate source

## What Must Not Diverge

The following must remain aligned with the substrate source:

- receipt identity and meaning-transition focus
- participant/admin surface distinction
- actionable vs informational classification
- uncertainty visibility
- authorization, execution attempt, unknown outcome, interruption, and
  completion distinctions
- boundary visibility and omission discipline
- anti-counterfeit framing
- the intentionally minimal and emergent character of the topology

## Minecraft-Specific Work Remaining

This mirror does not solve the Minecraft/Fabric adaptation problem.

Remaining work here is to:

- adapt the substrate topology to Minecraft-facing interaction surfaces
- preserve truthful omission in Fabric-facing presentation
- align local binding and runtime reasoning with the topology
- keep the substrate model and the Minecraft implementation boundary distinct

## Verification

Verification for this step consists of:
- `git status --short`
- `git diff --check`

## Uncertainties

This mirror should be refreshed if the substrate source changes in a way that
materially alters the receipt topology.

It should not become a parallel doctrine or a place to improvise new receipt
categories without a corresponding substrate change.
