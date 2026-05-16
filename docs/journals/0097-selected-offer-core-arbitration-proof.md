## Summary

Add a narrowly scoped Gate `#1` proof for:

- assembled selected-offer request
- Core arbitration
- approval and denial outcomes

This step remains outside Runtime execution, mutation application, confirmation UX,
receipts, and gameplay polish.

## Decision

Core arbitration is safe for this proof scope.

Selected-offer request assembly can now be shown to reach Core arbitration and
produce honest approval/denial outcomes.

## Rationale

The repository already had:

- selected capture proof
- authored-offer versus fulfillment/equivalence clarification
- equivalence-preserving authority handoff proof
- minimal Core-facing request assembly proof

The missing seam was whether that assembled request could actually enter Core and
produce truthful outcomes without invoking Runtime.

This step proves two adjacent surfaces:

- Fabric-side assembled payload reaches Core and can approve or deny based on
  explicit truth attestation outcomes
- binding-side hotbar authority truth for the same payload shape approves or denies
  based on removability and receivability semantics

The proof is split this way because the Fabric test environment does not safely host
bootstrapped `ItemStack`-backed authority truth evaluation directly, while the
bindings proof surface already does.

## Scope

Included:

- approval of assembled selected-offer payload through Core without Runtime
- Core denial for unresolved required truth
- Core denial for explicit false truth attestation
- refusal before arbitration for empty selected capture
- binding-side approval for equivalent owned value elsewhere in the hotbar
- binding-side denial for missing equivalent owned value
- binding-side denial for non-receivable counterparty state

Excluded:

- Runtime execution
- mutation application
- live player interaction
- confirmation UX
- receipt implementation

## Verification

The repository now proves:

- assembled selected-offer payload can enter Core arbitration
- selected-authored descriptor identity remains preserved in the approved
  mutation-facing descriptor produced by current transferable-value authority
- approval requires explicit non-conflicting truth attestation
- denial occurs when required truth is missing or explicitly false
- denial occurs when current inventory authority surfaces report not removable or
  not receivable
- no Runtime participation is required for this proof

## Uncertainties

Still deferred:

- live Runtime execution from this exact selected-offer chain
- mutation application against real live player state
- any broader player-owned fulfillment surface beyond the current explicit actor
  region shape
