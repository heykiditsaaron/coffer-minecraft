## Summary

Add a narrowly scoped Gate `#1` proof for:

- Core-approved selected exchange
- Runtime participation through the paved-road path
- honest success, failure, and unknown Runtime outcomes

This step remains outside confirmation UX, receipts, and live gameplay polish.

## Decision

Runtime participation is safe for this proof scope.

The repository now proves that a Core-approved selected inventory exchange can
enter Runtime participation through the paved road without adapter-side mutation of
the approved `MutationPlan`.

## Rationale

The repository already had:

- selected capture proof
- equivalence-preserving handoff proof
- Core-facing request assembly proof
- Core arbitration approval/denial proof

The missing seam was whether the approved plan from that chain could enter Runtime
honestly and remain gated by Core outcome.

This step proves two adjacent surfaces:

- Fabric-side adapter participation logic only enters Runtime after Core approval
  and passes the approved `MutationPlan` through unchanged
- binding-side paved `CofferRuntime` execution on the same selected-offer payload
  shape reports success, failure, and unknown honestly

## Scope

Included:

- denial gating before Runtime
- pass-through of approved `MutationPlan` without adapter modification
- Runtime participation result preservation for success, failure, and unknown
- malformed Runtime payload proof remains unknown rather than counterfeit success

Excluded:

- participant confirmation UX
- receipt implementation
- live gameplay interaction
- claims of completed live selected exchange UX

## Verification

The repository now proves:

- denied Core outcomes do not enter Runtime
- approved Core outcomes do enter Runtime
- approved `MutationPlan` reference is passed through unchanged by the adapter-side
  participation helper
- the paved Runtime path reports:
  - success when execution is feasible
  - failure when post-approval drift breaks execution
  - unknown when Runtime contact becomes unavailable or malformed

## Uncertainties

Still deferred:

- participant-facing confirmation flow
- receipt attachment
- live gameplay orchestration around this chain
- broader full live exchange UX beyond the proved paved-road participation
