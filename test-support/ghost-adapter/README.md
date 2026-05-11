# Ghost Adapter Test Support

## What This Is

`test-support/ghost-adapter` is a test-only living space for an
adapter-shaped proving vessel.

It exists to pressure-test survivability and accountability behavior around the
real Minecraft inventory binding, paved TVAL construction surface, Core, and
Runtime without turning that work into gameplay or production adapter logic.

## What This Is Not

This is not:

- a production adapter
- a Fabric readiness claim
- a live mutation readiness claim
- a generic adapter SDK or framework
- a player/admin UX surface
- SER/CER implementation

## Why It Exists

`bindings/inventory` now has strong local proof coverage, but some remaining
adapter-shaped questions sit above the binding and below any future
SER/CER-facing accountability layer.

This ghost space gives those proofs their own living area so they do not blur
into:

- binding production source
- platform production source
- substrate-facing code
- speculative generalized adapter infrastructure

## What It May Pressure-Test

The ghost adapter may pressure-test:

- interaction-capture-shaped request assembly
- paved TVAL construction refusal vs success
- Core denial vs approval boundaries
- Runtime success, failure, and unknown projection categories
- where future accountability projection could attach

## What Remains Deferred

Still deferred:

- gameplay behavior
- commands
- player-facing UX
- admin tooling
- production adapter APIs
- orchestration policy such as retry, rollback, recovery, or timeout handling
- SER/CER implementation
