# Coffer Minecraft — Agent Instructions

This file defines repository-specific constraints for the coffer-minecraft platform.

Global rules from ~/.codex/AGENTS.md apply.

---

## Purpose

This repository provides a Fabric platform implementation of the Coffer TransferableValue system.

It owns:
- Minecraft inventory-backed value semantics (bindings/inventory)
- Fabric lifecycle, wiring, and execution surface (platforms/fabric)

It exposes a safe execution boundary for external adapters.

---

## Core Architecture

This repository is divided into:

- bindings/inventory
  Owns inventory descriptors, matching, containers, and mutation behavior.

- platforms/fabric
  Owns lifecycle, authority wiring, player resolution, threading, and execution surface.

- adapters (external, NOT in this repo)
  Consume the platform via the public service interface.

---

## Authority Boundaries

- TransferableValueAuthority implementations exist ONLY in bindings/inventory.
- platforms/fabric MUST NOT reimplement inventory or value semantics.
- External adapters MUST NOT implement authorities.

---

## Execution Boundary

All exchange execution flows through:

CofferMinecraftExchangeService.submitExchange(...)

This is the ONLY supported entry point for external consumers.

---

## Threading Rules

- All execution must occur on the Minecraft server thread.
- platforms/fabric owns scheduling.
- Off-thread execution must be scheduled or rejected.
- No direct inventory mutation may occur off-thread.

---

## Result Semantics

FabricCofferExecutionResult defines the execution boundary:

- Denied → Core refusal (no runtime execution)
- Executed → Runtime attempted execution
- Unavailable → Platform could not safely attempt execution

Critical rule:

Non-success MUST NOT be interpreted as “no mutation occurred”.

Inventory may have partially changed due to post-simulation drift.

---

## Platform Responsibilities

platforms/fabric is responsible for:

- Fabric lifecycle integration
- Authority construction and wiring
- Player inventory resolution
- Server-thread scheduling
- Exposing the adapter-facing execution service

platforms/fabric MUST NOT:

- implement gameplay logic
- define player interaction behavior
- introduce adapter-specific assumptions

---

## Binding Responsibilities

bindings/inventory is responsible for:

- Descriptor parsing and validation
- Item and NBT matching
- Container mutation behavior
- Simulation and guarded application

bindings/inventory MUST NOT:

- depend on Fabric lifecycle
- perform scheduling
- expose platform-specific behavior

---

## Adapter Relationship

Adapters are external consumers.

This repository MUST:

- expose a minimal, stable execution surface
- avoid leaking internal implementation details
- avoid expanding API surface prematurely

This repository MUST NOT:

- implement player-trade logic
- define UI or commands
- define messaging behavior

---

## Known Constraints

- No rollback or retry exists.
- Partial mutation is possible under drift conditions.
- Result diagnostics are intentionally minimal.
- Service discovery is Fabric-entrypoint-based.
- Test tooling in bindings includes access widener and -Xverify:none.

---

## Development Discipline

- Maintain strict separation between binding, platform, and adapter concerns.
- Do not introduce cross-layer shortcuts.
- Do not duplicate logic across modules.
- Do not expand public API without clear adapter need.

---

## Outcome

This repository must remain:

- platform-agnostic at the Core level
- platform-specific only at the Fabric layer
- authoritative for inventory-backed value exchange
- safe and predictable for external adapter consumption
