# 0061 Minimal Ghost SER/CER Projection Proof

## Summary

Added the first extremely small, test-only SER/CER-shaped accountability
projection proof in the ghost adapter living space.

The new proof does not finalize schemas or introduce operational logging
infrastructure. It only pressure-tests whether current ghost-adapter
participation depth can be projected into minimal JSONL-friendly records without
fabricating contact, success, or absence.

## Decision

Use a tiny ghost-adapter-local projection shape with only:

- `interactionId`
- `recordType`
- `stage`
- optional `code`

Project:

- construction refusal as a single `SER` record
- Core denial as `SER` plus `CER`
- Runtime success as `SER` plus `CER`
- Runtime failure as `SER` plus `CER`
- Runtime unknown as `SER` plus `CER`

Do not add timestamps, player feedback, inventory snapshots, alternate sinks, or
durable logging mechanics in this step.

## Rationale

This repository has enough proof to distinguish construction refusal, Core
denial, and Runtime success/failure/unknown through the ghost adapter harness.

That makes it possible to pressure-test a minimal accountability projection
without claiming final SER/CER design and without turning the work into
production infrastructure.

Minimality matters here because:

- participation depth should determine record shape
- unknown must remain unknown
- non-participation should be omitted rather than fabricated
- redundant fields would harden premature schema assumptions

## Scope

Included:

- test-only accountability projection utility under `test-support/ghost-adapter`
- proof that one interaction keeps one canonical identity across emitted records
- proof that `SER` exists before lawful substrate contact
- proof that lawful contact produces `CER`-shaped accountability
- proof that Runtime non-invocation remains distinguishable
- proof that Runtime unknown remains distinguishable
- proof that approval is not equivalent to execution success

Excluded:

- production logging infrastructure
- log folder creation or deployment behavior
- final JSONL schema design
- SER/CER implementation claims
- gameplay, UX, commands, admin tooling
- retries, rollback, recovery, remediation, timeout policy

## Verification

Covered by inventory binding tests:

- construction refusal emits only a `SER` record
- Core denial emits `SER` then `CER` without fabricated Runtime participation
- Runtime success emits a distinct `CER` success stage
- Runtime failure emits a distinct `CER` failure stage
- Runtime unknown emits a distinct `CER` unknown stage
- emitted records stay intentionally tiny

## Uncertainties

This proof must not be read as final schema authority.

The following remain intentionally unresolved:

- whether future canonical records remain exactly two-layered
- which additional fields are truly required for durable reconstruction
- how append-only JSONL emission should be owned at the platform layer
- where final SER/CER interpretation and feedback projection should attach
