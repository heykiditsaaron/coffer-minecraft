# 0068 Fabric Inert Construction Contact

## Summary

Introduced the smallest deeper real-platform contact beyond lifecycle-only
participation: a startup-bounded inert paved-construction probe in
`platforms/fabric`.

The probe reaches the lawful construction surface and intentionally refuses
before Core. No Runtime, execution, or mutation participation is introduced.

## Decision

On `SERVER_STARTED`, after the existing lifecycle `SER` record, perform one
inert paved-construction probe that uses an intentionally missing binding id.

If construction refuses, append one additional minimal `SER` record with:

- `interactionId`
- `recordType`
- `stage`
- `code`

using stage `fabric_construction_refused`.

Do not contact Core or Runtime in this step.

## Rationale

This is the smallest safe deepening of real platform participation because it:

- touches actual Fabric lifecycle callbacks
- touches the actual paved TVAL construction surface
- stops before Core arbitration
- avoids players, inventory, execution, and mutation

That preserves the accountability boundary that deeper participation must only
appear when it actually occurs.

## Scope

Included:

- startup-bounded inert paved-construction contact on the real Fabric path
- minimal JSONL accountability append under `logs/coffer/fabric-lifecycle.jsonl`
- tests proving refusal is distinguishable from lifecycle-only contact
- tests proving no Core/Runtime/execution/mutation participation is fabricated

Excluded:

- Core denial on the real platform path
- Runtime participation
- gameplay, commands, UX, admin tooling
- production adapters or trade/session flow

## Verification

Covered by tests and build validation:

- lifecycle-only records remain `SER`-only
- inert construction contact emits a distinct refusal line
- refusal lines remain minimal and readable
- no Runtime or mutation fields appear
- Fabric tests and compile still pass

## Uncertainties

The safe real-platform seam still stops before Core because a truthful Core
contact would require synthetic actor/container participation that this step
should not invent.

Still deferred:

- real-platform Core denial proof
- real-platform Runtime non-invocation proof after Core denial
- any live exchange or mutation participation
