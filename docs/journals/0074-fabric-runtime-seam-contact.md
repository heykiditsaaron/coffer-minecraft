# 0074 Fabric Runtime Seam Contact

## Summary

Added the smallest bounded Fabric startup proof of Runtime boundary contact
after Core approval.

The existing startup path now reaches:

- lifecycle contact
- construction refusal
- Core denial
- Core approval
- Runtime unknown contact

The Runtime step is intentionally inert: it uses a runtime authority that
cannot resolve live inventory, so the probe can contact Runtime without
inventory mutation or gameplay participation.

## Decision

Emit a new optional earned seam on the Runtime unknown record only:

- `seam: "fabric_runtime"`

Keep the record flat and left-to-right readable:

- `timestamp`
- `interactionId`
- `recordType`
- `stage`
- optional `seam`
- optional `code`

## Rationale

Runtime contact is earned only after Core approval because the probe needs a
valid mutation plan before Runtime can participate at all.

The runtime authority was made inert on purpose by using container resolution
that cannot find live inventory. That preserves truthfulness while avoiding:

- inventory mutation
- player dependence
- gameplay flow
- production trade/session behavior
- fabricated success claims

The resulting Runtime result is an unknown/no-op boundary outcome, not a
mutation success.

## Scope

Included:

- Fabric startup Runtime contact probe
- optional `fabric_runtime` seam on Runtime unknown accountability records
- startup sequencing that preserves the earlier Core-only approval proof
- tests proving Runtime contact is distinguishable from Core-only approval
- tests proving no execution or mutation content appears in the record

Excluded:

- broad Runtime schemas
- nested contact objects
- player-facing UX
- commands or gameplay behavior
- inventory mutation success
- production readiness claims

## Verification

Covered by targeted tests and combined build validation:

- Core approval still exists as a distinct proof
- Runtime contact appears only after Core approval
- Runtime contact emits `fabric_runtime_unknown`
- no mutation/execution/player details appear in the JSONL record

## Uncertainties

This proves only the smallest inert Runtime seam.

Still deferred:

- any live runtime contact that depends on real player inventory state
- any mutation success or failure claim beyond the inert unknown result
- any deeper Runtime participation detail
