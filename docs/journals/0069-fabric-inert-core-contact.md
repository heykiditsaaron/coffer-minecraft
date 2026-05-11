# 0069 Fabric Inert Core Contact

## Summary

Introduced the smallest safe real-platform Core contact seam on the Fabric
startup path.

After lifecycle contact and the existing construction-refusal probe, Fabric now
performs one paved-construction success followed by an inert Core arbitration
that is denied before Runtime.

## Decision

Use a startup-bounded inert payload with:

- valid paved construction
- unresolved Core authority resolution

to produce a truthful Core denial without invoking binding authority, Runtime,
execution, or mutation.

Emit one minimal `CER` record:

- `interactionId`
- `recordType`
- `stage`
- `code`

with stage `fabric_core_denied`.

## Rationale

This is the smallest safe real-platform Core seam because it:

- reaches actual Core arbitration
- remains bounded to startup contact
- avoids players, gameplay flow, and inventory mutation
- preserves the distinction between construction refusal and Core denial

The denial is truthful because the payload is valid enough to reach Core, but
authority resolution remains intentionally unresolved at the Core boundary.

## Scope

Included:

- startup-bounded Core denial probe on the real Fabric path
- minimal `CER` JSONL append under `logs/coffer/fabric-lifecycle.jsonl`
- tests proving Core denial is distinct from construction refusal
- tests proving binding/Runtime/execution/mutation participation remains omitted

Excluded:

- Runtime invocation
- mutation execution
- gameplay or player interaction
- production adapter behavior

## Verification

Covered by tests and build validation:

- lifecycle records remain unchanged
- construction refusal remains `SER`
- Core denial emits `CER`
- no Runtime/execution/mutation fields appear
- Fabric tests and compile still pass

## Uncertainties

This seam still stops at unresolved authority resolution rather than live
binding participation.

Still deferred:

- truthful binding-participating Core denial on the real platform path
- Runtime non-invocation proof after live-platform binding-aware Core denial
- any deeper execution participation
