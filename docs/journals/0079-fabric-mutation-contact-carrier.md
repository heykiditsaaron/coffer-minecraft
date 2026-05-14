# 0079 Fabric Mutation Contact Carrier Readiness

## Summary

Define a test-first inert mutation contact carrier that can represent
mutation-boundary contact/no-contact without claiming success or requiring
gameplay, commands, UX, or real player inventory.

This is a readiness note, not a mutation seam implementation.

## Decision

Use a minimal Fabric-side test carrier with only mutation contact state.

Do not add a mutation seam, do not add schema fields, and do not wire this into
production startup contact paths yet.

## Rationale

The carrier is intentionally small enough to stay inert:

- it only represents contact/no-contact
- it cannot claim mutation success
- it does not model players, commands, UX, or inventory
- it does not expand the SER/CER envelope

The current topology still does not prove a lawful mutation-boundary seam. The
safe path remains test-first only.

## Scope

Included:

- the proposed carrier shape
- the carrier's proof obligations
- the carrier's explicit omissions
- the continued mutation-seam deferral

Excluded:

- production wiring
- gameplay
- commands
- UX
- real player inventory mutation
- exchange success claims
- schema expansion
- nested mutation objects

## Verification

The carrier is constrained to tests and the existing envelope/accountability
path remains unchanged.

## Uncertainties

This carrier may still be insufficient if a future mutation proof requires a
separate inert vessel or an additional boundary adapter before any mutation
contact can be earned truthfully.
