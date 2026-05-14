# 0080 Fabric Mutation Contact Refusal

## Summary

Pressure-test the inert mutation contact carrier against the current mutation
boundary and document the lawful refusal.

## Decision

Keep mutation seam deferred.

The inert carrier can represent contact/no-contact in tests, but it does not
yet have a safe bridge into a real mutation-boundary participant that would be
truthfully distinct from item/registry bootstrap behavior.

## Boundary Contact

Attempted contact:

- a Fabric-side inert mutation contact carrier
- the current Fabric runtime probe and accountability path

Observed result:

- contact/no-contact can be represented locally
- mutation success cannot be claimed
- no lawful mutation seam is emitted

## Rationale

The refusal boundary is the lack of a safe, truthful mutation participant that
can be reached without:

- real player inventory
- gameplay/session/command/UX behavior
- registry/bootstrap coupling that would reintroduce the original unsafe path

The carrier remains useful as a perimeter marker, but it is not yet a lawful
mutation vessel.

## Scope

Included:

- the contacted carrier as a refusal-pressure tool
- the distinction between contact representation and mutation success
- the continued absence of `fabric_mutation`
- the exact reason the contact is still refused

Excluded:

- mutation seam emission
- gameplay
- commands
- UX
- player inventory mutation
- production readiness claims
- schema expansion
- nested mutation objects

## Verification

The carrier test suite now confirms that a contacted carrier still refuses to
claim success and the existing accountability records remain unchanged.

## Uncertainties

The next lawful participant may need a dedicated inert mutation bridge or a
separate substrate-level boundary adapter before any mutation contact can be
earned truthfully.
