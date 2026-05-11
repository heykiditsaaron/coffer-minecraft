# 0065 Minimal JSONL Mixed Stream Pressure

## Summary

Applied denser mixed-stream pressure to the minimal accountability projection and
JSONL emitter, including interleaved interaction lineages and repeated mixed
unknown/non-participation cases.

The current shape remained reconstructable without sequence inflation, replay
structures, or duplicated narration.

## Decision

Keep mixed-stream reconstruction grounded in the existing minimal surface:

- append order
- `interactionId`
- `recordType`
- `stage`
- optional `code`

Do not add explicit sequence numbers or structural lineage wrappers at this
stage.

## Rationale

The remaining pressure question was whether noisier append streams would make
the current shape unreadable or ambiguous.

The tests show the shape still survives when:

- multiple lineages are interleaved
- refusal, denial, success, and repeated unknown outcomes share one stream
- omitted participation fields remain omitted under dense append conditions

That is enough for current survivability pressure and does not yet justify more
structure.

## Scope

Included:

- proof that interleaved lineages remain reconstructable from append order plus
  identity
- proof that repeated unknown outcomes do not inflate the schema
- proof that denser mixed streams remain readable left-to-right in raw JSONL
- proof that omission still suppresses structural noise

Excluded:

- sequence numbering
- replay/event-sourcing structures
- generalized chronology infrastructure
- any schema growth beyond the current tiny envelope

## Verification

Covered by tests:

- interleaved `SER` then `CER` lines for multiple interactions remain coherent
- mixed refusal/denial/unknown/success streams remain distinguishable
- no sequence, history, timeline, participation, or runtime wrapper fields
  emerge

## Uncertainties

This proof does not establish:

- final policy for very large or rotated streams
- cross-process ordering guarantees
- whether future accountability layers will ever require explicit sequence
  fields
