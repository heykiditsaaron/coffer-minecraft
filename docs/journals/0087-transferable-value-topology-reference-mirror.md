# 0087 Transferable-Value Topology Reference Mirror

## Summary

Added a local mirror of the transferable-value interaction lifecycle topology
so repo-local `coffer-minecraft` work can reason about the substrate model
without depending on cross-repo access.

## Decision

Keep the mirror in `docs/architecture` as a reference artifact, and keep the
canonical substrate source in `dev/coffer`.

The source remains
`dev/coffer/docs/journals/0071-transferable-value-interaction-lifecycle-topology.md`.

## Rationale

This repository will not always have reliable access to the substrate checkout,
but future collaborators still need the participant interaction topology close
to the Minecraft/Fabric boundary work.

Duplicating the topology here is intentional because it preserves reasoning
continuity for repo-local agents while avoiding redefinition of the substrate
model.

The mirror is not independently canonical. It exists only so Minecraft-specific
work can adapt the substrate topology in place rather than reconstruct it from
memory or drift into conflicting terminology.

## Scope

Included:
- mirrored transferable-value interaction lifecycle topology
- substrate provenance note at the top of the mirror
- local orientation for future `coffer-minecraft` collaborators and agents
- explicit statement that Minecraft/Fabric work must adapt the topology, not
  redefine it

Excluded:
- code changes
- schema changes
- gameplay changes
- UI changes
- lifecycle state rewrites
- alternate terminology
- divergence from the substrate source

## What Must Not Diverge

The following must remain aligned with the substrate source:

- lifecycle state names
- lifecycle ordering
- the participant-centered scope
- refusal separation into actionable and non-actionable cases
- authorization, execution contact, execution unknown, and completion as
  distinct states
- anti-counterfeit framing
- the emergent, non-final character of the topology

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
materially alters the lifecycle topology.

It should not become a parallel doctrine or a place to improvise new states
without a corresponding substrate change.
