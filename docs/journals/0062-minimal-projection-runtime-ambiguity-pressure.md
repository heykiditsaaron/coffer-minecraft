# 0062 Minimal Projection Runtime Ambiguity Pressure

## Summary

Pressure-tested the current minimal ghost-adapter SER/CER-shaped projection
against richer Runtime ambiguity and non-participation cases without expanding
the shape.

The projection survived both:

- Core denial without Runtime participation
- distinct Runtime `UNKNOWN` causes with the same minimal two-record envelope

## Decision

Keep the current tiny projection shape unchanged:

- `interactionId`
- `recordType`
- `stage`
- optional `code`

Do not add placeholder Runtime sections, nested participation blocks, or
extra cause-expansion fields just to represent different `UNKNOWN` paths.

## Rationale

The current accountability pressure goal is survivability, not schema
finalization.

The projection only needs to remain minimally reconstructable for the current
proof surface:

- a single canonical interaction identity
- whether lawful contact occurred
- whether Runtime did not participate
- whether Runtime participated and ended in success, failure, or unknown
- which minimal code distinguishes ambiguous `UNKNOWN` causes

The proof shows that richer ambiguity pressure does not yet justify structural
growth.

## Scope

Included:

- proof that malformed-runtime `UNKNOWN` projects through the same minimal shape
- proof that disappearance `UNKNOWN` and malformed-runtime `UNKNOWN` remain
  distinguishable by `code` without new layers
- proof that non-participating surfaces remain omitted
- proof that no contradictory or duplicate event identity emerges

Excluded:

- file emission
- production JSONL infrastructure
- final SER/CER schema work
- richer cause taxonomy
- feedback/explanation projection

## Verification

Covered by projection tests:

- Core denial still emits no fabricated Runtime layer
- malformed-runtime `UNKNOWN` emits `SER` plus `CER`
- disappearance `UNKNOWN` emits the same shape with a different `code`
- records stay within the same tiny field envelope

## Uncertainties

This proof does not settle:

- whether future durable accountability needs more than one `code`
- whether some later ambiguity classes require another stage value
- how append-only JSONL emission will be attached at the platform layer
- whether final SER/CER authority keeps this exact record cardinality
