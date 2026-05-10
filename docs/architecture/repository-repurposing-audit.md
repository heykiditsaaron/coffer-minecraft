# Repository Repurposing Audit

## Summary

`coffer-minecraft` is being refounded as the Minecraft super-platform repository.
Its purpose is Minecraft-specific binding, adapter, platform, documentation, and
test work. Platform-agnostic substrate design and implementation belong in the
separate substrate repositories.

This audit is read-only with respect to implementation code. It identifies what
should be deleted, temporarily preserved, added, and deferred before cleanup.

## Current State

The repository currently contains:

- `bindings/inventory`: Minecraft inventory semantics wired to the
  TransferableValue authority ports.
- `platforms/fabric`: Fabric lifecycle, service exposure, server-thread
  scheduling, and exchange execution.
- `docs/architecture`: boundary and adapter notes.
- `docs/decisions`: one early decision record.
- `docs/journals`: historical implementation journals from the previous
  iteration.
- Gradle wiring that still assumes a local `../coffer` composite build.

The present source tree is already closer to a Minecraft-specific platform repo
than a generic substrate repo. The stale risk is mostly in dependency wiring,
docs terminology, and historical material that predates the new multi-repo
substrate split.

## Delete Outright

The following should be deleted during cleanup after this audit is accepted:

- Generated Gradle and build output: `.gradle/`, root `build/`,
  `bindings/inventory/build/`, `platforms/fabric/build/`.
- Runtime logs and local execution artifacts: `bindings/inventory/logs/`, any
  future `run/`, `logs/`, `world/`, or generated jars.
- Obsolete documentation that describes `coffer-minecraft` as owning gameplay
  adapters or platform-neutral Coffer architecture.
- Stale Gradle composite wiring to a monolithic `../coffer` repo once
  replacement substrate coordinates or included builds are chosen.

No implementation source should be deleted until the new module boundaries are
confirmed against the substrate repositories.

## Preserve Temporarily

The following should remain available as migration/reference material until the
new foundation is compiling against the split substrate repos:

- `bindings/inventory/src/main`: reference implementation for Minecraft item
  descriptors, matching, container mutation, simulation, and payload mapping.
- `bindings/inventory/src/test`: behavioral reference tests for inventory
  semantics.
- `platforms/fabric/src/main`: reference implementation for Fabric lifecycle,
  player inventory resolution, server-thread scheduling, and service exposure.
- `docs/journals/0001-*.md` through `docs/journals/0046-*.md`: historical
  record. These should not drive new architecture, but they explain why the
  current implementation exists.
- `docs/architecture/*.md` and `docs/adapter-integration.md`: useful content
  after terminology and scope are updated.

Preserved material should be treated as candidate source, not authoritative
design. The substrate repositories are authoritative for Core, Runtime, and
TransferableValue contracts.

## Target Topology

Recommended repository shape:

```text
.
├── AGENTS.md
├── README.md
├── build.gradle
├── gradle.properties
├── settings.gradle
├── bindings/
│   └── inventory/
│       └── src/
├── platforms/
│   └── fabric/
│       └── src/
├── adapters/
│   └── README.md
├── docs/
│   ├── architecture/
│   ├── contracts/
│   ├── integration/
│   └── journals/
└── tests/
    └── fixtures/
```

`adapters/` should remain documentation-only until a concrete Minecraft-specific
adapter is requested. It should not become a generic adapter SDK.

`tests/fixtures/` should hold Minecraft-specific fixtures only when shared
fixtures are needed across modules. Module-local tests should stay beside the
module they verify.

## Fabric-First Location

Fabric-first implementation should live under:

```text
platforms/fabric/
```

The Fabric module should own:

- Fabric loader metadata and lifecycle hooks.
- Minecraft server attachment and detachment.
- Main-thread scheduling and rejection semantics.
- Player and inventory resolution through Fabric/Minecraft APIs.
- Public Minecraft platform service exposed to external consumers.

Fabric must not own inventory value semantics, Core arbitration behavior,
Runtime execution behavior, or gameplay workflow policy.

## Immediate Docs

The repo should immediately contain:

- `README.md`: current purpose, module map, build/test basics, and non-goals.
- `AGENTS.md`: repo-specific boundaries for the Minecraft super-platform.
- `docs/architecture/repository-boundaries.md`: source of truth for what this
  repo owns versus substrate repos.
- `docs/architecture/repository-repurposing-audit.md`: this audit.
- `docs/contracts/fabric-execution-service.md`: adapter-facing Fabric service
  contract, threading, result semantics, and non-success caveats.
- `docs/contracts/inventory-binding.md`: Minecraft inventory descriptor,
  matching, container, simulation, and mutation semantics.
- `docs/integration/substrate-dependencies.md`: consumed substrate artifacts and
  local development wiring.
- `docs/journals/`: concise chronological records for behavior or boundary
  changes.

The existing `docs/adapter-integration.md` can either move to
`docs/contracts/fabric-execution-service.md` or be rewritten as an integration
guide after the Fabric service contract stabilizes.

## Dependency Guidance

This repo should consume substrate repos as external dependencies:

- `coffer-core`: exchange request model, arbitration, outcomes, authority
  resolution contracts, mutation plan model.
- `coffer-runtime`: runtime execution engine, execution result model, runtime
  authority contracts.
- `coffer-transferable-value-authority`: TransferableValue core/runtime
  authorities and port interfaces used by Minecraft inventory bindings.

Local development may use included builds or Maven-local coordinates, but the
repo should not assume a single monolithic `../coffer` checkout. The replacement
should point at the split repositories or published coordinates explicitly.

Minecraft dependencies should stay loader/platform-specific where possible:

- Fabric loader/API in `platforms/fabric`.
- Minecraft/Yarn/Loom only where Minecraft classes are compiled.
- Shared Minecraft inventory binding dependencies in `bindings/inventory` only
  when the binding directly uses Minecraft classes.

## Do Not Duplicate

This repository must not duplicate:

- Core arbitration rules.
- Exchange request, outcome, mutation plan, or authority identity models.
- Runtime execution orchestration.
- TransferableValue authority behavior that belongs in the authority repo.
- Generic adapter frameworks or SDK abstractions.
- Platform-agnostic documentation from substrate repos.
- Gameplay policy such as player trade workflows, UI, commands, messaging, or
  permissions unless explicitly requested as Minecraft-specific adapters.

## AGENTS.md Guidance

`AGENTS.md` should be replaced or tightened to reflect the new purpose:

- State that this is the Minecraft super-platform repo, not a substrate repo.
- Preserve binding/platform/adapters separation.
- Require Fabric-first work to stay under `platforms/fabric`.
- Require Minecraft inventory semantics to stay under `bindings/inventory`.
- Require all substrate behavior to be consumed, not reimplemented.
- Keep adapters external or deferred unless explicitly requested.
- Require docs and journals for behavior-affecting changes.
- Warn that old discovery-era journals are historical reference, not current
  authority.

## Cleanup Sequence

Safest sequence:

1. Add this audit and a journal entry.
2. Update `AGENTS.md`, `README.md`, and architecture docs to state the new
   Minecraft super-platform purpose.
3. Add immediate contract docs for Fabric execution and inventory binding.
4. Replace `settings.gradle` substrate wiring with split-repo or published
   dependency wiring.
5. Run focused compile/tests to establish a baseline.
6. Delete generated outputs and local runtime artifacts.
7. Move or rewrite stale docs into `docs/contracts/` and `docs/integration/`.
8. Reassess implementation source after dependency wiring is stable.
9. Remove or rewrite obsolete source only with a narrow journaled decision.
10. Introduce future platform modules only when there is concrete implementation
    work for them.

## Deferred

The following should remain deferred:

- NeoForge, Bukkit, Paper, or other platform modules.
- Gameplay adapters, player-trade flows, UI, commands, messaging, and
  permissions.
- Generic adapter SDKs.
- Rollback or retry semantics for inventory mutation.
- Expanded public API surface beyond the Fabric execution service.
- Cross-platform abstractions until at least two platform implementations need
  the same contract.
- Deleting implementation source before the split substrate dependency baseline
  is proven.

