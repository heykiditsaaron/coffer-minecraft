# 0073 Fabric Core Seam Identity

## Summary

Added the first narrowly bounded optional content beyond the stable
SER/CER envelope: a flat `seam` field on Fabric Core-contact `CER`
records.

The Fabric startup probes already reach truthful Core denial and truthful
Core approval without entering Runtime. This step marks that contact as
occurring across the `fabric_core` seam while leaving the envelope fields
unchanged.

## Decision

Emit optional `seam: "fabric_core"` only on `CER` records whose stage is:

- `fabric_core_denied`
- `fabric_core_approved`

Do not emit `seam` on lifecycle `SER` records or construction-refusal `SER`
records.

Keep the JSONL line flat and left-to-right readable:

- `timestamp`
- `interactionId`
- `recordType`
- `stage`
- optional `seam`
- optional `code`

## Rationale

The Fabric-to-Core boundary is the first lawful selective expansion point
because it is a real inter-boundary contact, not just platform lifecycle or
pre-contact refusal.

`seam` is earned here because:

- the payload has already crossed from Fabric into Core participation
- the accountable question deepens from "what stage occurred" to "which
  bounded surface carried that contact"
- the answer can be given without claiming Runtime, mutation, authority
  detail, or narrative telemetry

This keeps stage as the accountable outcome while making the contact surface
explicit.

## Scope

Included:

- optional seam identity on Fabric Core `CER` lines
- tests proving seam appears only when Core boundary participation occurs
- tests proving field order remains raw-JSONL readable

Excluded:

- envelope changes
- nested contact objects
- Runtime or execution detail
- mutation-plan identifiers
- authority identifiers
- gameplay or session narration

## Verification

Covered by targeted tests and combined build validation:

- lifecycle `SER` lines remain unchanged
- construction refusal remains minimal and seam-less
- Core denial and approval now carry `seam: "fabric_core"`
- seam ordering stays after `stage` and before optional `code`

## Uncertainties

This step proves only that one seam identity can be added selectively
without collapsing omission discipline.

Still deferred:

- whether future lawful seams need identities beyond `fabric_core`
- whether deeper earned detail should attach at authority or Runtime contact
- whether any later seam needs more than one flat optional field
