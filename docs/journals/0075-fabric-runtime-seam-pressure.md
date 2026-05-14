# 0075 Fabric Runtime Seam Pressure

## Summary

Applied omission and ordering pressure to the first earned Runtime seam,
`fabric_runtime`, on the Fabric startup accountability path.

## Decision

Keep `fabric_runtime` optional and earned only on the Runtime unknown contact
record. Do not move it into the stable envelope and do not add any deeper
Runtime content fields.

## Rationale

The runtime seam is only justified where Runtime actually participates.
That makes it a boundary-contact marker, not a universal record field.

The added pressure focuses on reconstructability:

- lifecycle records omit the runtime seam
- construction-refusal records omit the runtime seam
- Core-only records omit the runtime seam
- Runtime-contact records carry `seam:"fabric_runtime"` and the earned
  non-success code

This proves the seam remains selective without turning the record stream into
general Runtime telemetry.

## Scope

Included:

- ordering checks for the flat JSONL record shape
- omission checks for all non-Runtime startup records
- Runtime seam checks on the inert unknown Runtime contact

Excluded:

- new content fields
- nested runtime objects
- mutation success claims
- gameplay, commands, UX, or production behavior
- broader Runtime schema expansion

## Verification

Covered by targeted Fabric tests and combined build validation.

## Uncertainties

The inert Runtime contact remains intentionally minimal.

Still deferred:

- any deeper Runtime participation detail
- any live player-dependent Runtime seam
- any mutation-result expansion beyond the current unknown/no-op proof
