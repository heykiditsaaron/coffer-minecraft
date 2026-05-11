# Coffer Minecraft

`coffer-minecraft` is the Minecraft super-platform repository.

It is the implementation space for Minecraft-specific bindings, platform modules,
contracts, integration guidance, and tests. It is not the substrate home for
Core, Runtime, or TransferableValue authority logic.

## Current Focus

This repository is in controlled re-foundation.

Current work should stay focused on:

- stabilizing Minecraft-specific module boundaries
- documenting current binding and platform contracts
- consuming substrate artifacts through explicit dependency edges
- verifying the current inventory binding and Fabric modules against those boundaries

The current inventory binding is compile- and test-proven candidate architecture
for Minecraft-specific inventory semantics. The current Fabric source remains
available as platform implementation material while Fabric feature expansion
stays deferred.

## Intended Structure

- `bindings/inventory`: Minecraft inventory binding semantics
- `platforms/fabric`: Fabric-specific platform module space
- `docs/architecture`: repository-scoped architecture and boundaries
- `docs/contracts`: Minecraft-specific contracts only
- `docs/integration`: integration guidance with substrate repositories
- `docs/journals`: chronological repository evolution records

## Non-Goals

This repository is not:

- `coffer-core`
- `coffer-runtime`
- `coffer-transferable-value-authority`
- a generic adapter SDK
- a platform-agnostic architecture repo
- a gameplay workflow repo unless explicitly extended for a concrete
  Minecraft-specific adapter

## Dependency Direction

This repository consumes substrate repositories. It must not duplicate their
models, orchestration, or authority behavior.

See [repository-repurposing-audit.md](/home/aaron/dev/coffer-minecraft/docs/architecture/repository-repurposing-audit.md)
for the current re-foundation audit.
