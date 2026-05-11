# Coffer Minecraft — Agent Instructions

This file defines repository-specific constraints for `coffer-minecraft`.

Global rules from `~/.codex/AGENTS.md` apply.

## Purpose

`coffer-minecraft` is the Minecraft super-platform repository.

It exists for:

- Minecraft-specific bindings
- Minecraft-specific platform implementations
- Minecraft-specific contracts and integration docs
- Minecraft-specific tests and fixtures

It does not exist for:

- Core substrate ownership
- Runtime substrate ownership
- TransferableValue authority ownership
- platform-agnostic architecture work
- generic adapter SDK design

Authoritative substrate logic lives in separate repositories, including:

- `coffer-core`
- `coffer-runtime`
- `coffer-transferable-value-authority`

## Repository Shape

The intended top-level structure is:

- `bindings/inventory`
- `platforms/fabric`
- `docs/architecture`
- `docs/contracts`
- `docs/integration`
- `docs/journals`

Optional future directories must be justified by concrete Minecraft-specific
need, not speculative platform planning.

## Ownership Boundaries

`bindings/inventory` owns Minecraft inventory binding semantics only:

- descriptor mapping
- item matching
- slot and container interpretation
- simulation and mutation behavior at the Minecraft boundary

`platforms/fabric` owns Fabric-specific platform work only:

- Fabric lifecycle integration
- server attachment and scheduling
- Fabric-facing execution surface
- player and inventory resolution through Fabric/Minecraft APIs

`docs/contracts` is for Minecraft-specific contracts only.

`docs/integration` is for integration guidance between this repository and the
substrate repositories.

`adapters/` is deferred. If present, it remains documentation-only until a
concrete Minecraft-specific adapter is explicitly requested.

## Hard Prohibitions

Do not place the following in this repository:

- Core arbitration logic
- Runtime orchestration logic
- TransferableValue authority behavior that belongs in the substrate authority repo
- generic adapter SDK or framework layers
- platform-agnostic architecture or contracts
- gameplay policy, commands, UI, messaging, or permissions unless explicitly
  requested as Minecraft-specific adapter work

Do not duplicate substrate models, identifiers, planning logic, or authority
behavior locally for convenience.

## Re-Foundation Rules

During the repository re-foundation period:

- Keep scaffolding lightweight and non-speculative.
- Preserve existing inventory and Fabric source as migration/reference material
  until dependency wiring and boundaries are re-established.
- Prefer documenting boundaries over adding new abstractions.
- Do not begin new Fabric implementation unless explicitly requested.
- Do not port historical adapter logic unless explicitly requested.
- Do not delete implementation source solely because it is old; delete only what
  is clearly generated, stale, or superseded by an approved boundary decision.

Historical discovery-era journals and source are reference material, not current
architectural authority.

## Dependency Discipline

This repository consumes substrate repositories. It does not redefine them.

Use substrate dependencies for:

- exchange request and outcome models
- mutation and execution models
- authority contracts and implementations
- runtime orchestration

Do not invent local replacements for missing dependency wiring. If a dependency
is unsettled, document the boundary and defer implementation.

## Documentation Discipline

Use documentation intentionally:

- `README.md`: repository purpose, structure, and current non-goals
- `docs/architecture`: repository-scoped architecture and boundary decisions
- `docs/contracts`: Minecraft-specific service and binding contracts
- `docs/integration`: substrate integration and migration guidance
- `docs/journals`: chronological records of behavior or boundary changes

Do not mix platform-agnostic substrate design into repository-local docs.

## Journal Requirement

Create a journal entry before any meaningful commit that changes behavior,
repository structure, ownership boundaries, or integration expectations.

Journal entries must stay narrow, factual, and auditable.
