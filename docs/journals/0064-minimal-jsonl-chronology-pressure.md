# 0064 Minimal JSONL Chronology Pressure

## Summary

Applied chronological append-stream pressure to the minimal accountability
projection and JSONL emitter.

The stream remained reconstructable across mixed interaction outcomes without
adding nested timelines, replay structures, or extra narration fields.

## Decision

Keep chronology implicit in append order and keep interaction lineage
reconstructable from the existing minimal fields:

- `interactionId`
- `recordType`
- `stage`
- optional `code`

Do not add timeline containers, sequence wrappers, or repeated state summaries.

## Rationale

The current pressure question was whether multiple interactions and mixed
outcomes in one append stream would force schema growth.

The answer is no for the current proof surface:

- append order preserves chronology
- repeated `interactionId` preserves lineage
- `stage` remains enough to understand local progression
- omitted participation remains omitted

That keeps the records readable and minimally reconstructable without
duplicating truths across lines.

## Scope

Included:

- mixed append-stream proof across Core denial, Runtime success, Runtime
  unknown, and SER-only refusal
- repeated append proof for multiple two-record interaction lineages
- proof that chronological reconstruction survives without nested event
  structures

Excluded:

- explicit sequence numbering
- replay systems
- generalized event sourcing
- schema expansion for timeline narration

## Verification

Covered by tests:

- multiple interactions remain distinguishable in one append stream
- one interaction lineage stays coherent across adjacent records
- stage progression stays understandable from append order plus current fields
- no nested timeline/event/history fields emerge

## Uncertainties

This proof does not establish:

- final long-lived stream partitioning strategy
- cross-file ordering guarantees
- durable operational log rotation policy
- final schema authority for chronology beyond this minimal proof
