# Ghost Adapter Living Space And First Harness

## Summary

Created the first test-only ghost adapter living space in
`test-support/ghost-adapter` and added an adapter-shaped harness that pressures
construction, Core, and Runtime projection boundaries without becoming
production adapter work.

## Decision

The ghost adapter receives its own living space at:

- `test-support/ghost-adapter`

That location is intentional. It keeps adapter-shaped proof work out of:

- `bindings/inventory` production source
- `platforms/fabric` production source
- substrate-facing code
- premature generic adapter infrastructure

## Rationale

The latest proof-gate map concluded that the next vessel should sit above the
binding and below any future SER/CER accountability layer.

The ghost adapter is needed because it can:

- capture adapter-shaped request intent
- use the paved TVAL construction surface
- contact Core
- contact Runtime only when lawfully approved
- preserve distinct projection categories for refusal, denial, success,
  failure, and unknown

It is not production adapter implementation because it does not:

- provide gameplay behavior
- provide commands or UX
- define production adapter APIs
- define final feedback/accountability projection
- claim Fabric or live mutation readiness

## Scope

Included:

- isolated ghost adapter living space
- boundary README for that living space
- first test-only harness using real Minecraft inventory binding collaborators
- narrow tests for projection categories around the current lawful middle

Excluded:

- gameplay behavior
- admin tooling
- SER/CER implementation
- recovery orchestration
- production adapter APIs or generic frameworks

## Verification

First harness now proves:

- construction refusal is preserved before Core
- Core denial prevents Runtime invocation
- Core approval invokes Runtime
- Runtime success is projected distinctly
- Runtime failure is projected distinctly
- Runtime unknown is projected distinctly
- the ghost harness routes through the paved construction gateway rather than
  hand-building TVAL law locally

## Uncertainties

Still deferred:

- richer adapter-side capture semantics
- final feedback/accountability projection
- timeout policy
- disconnect sequencing beyond the current runtime ambiguity proofs
- production adapter implementation

These must not be assumed from the current ghost adapter harness.
