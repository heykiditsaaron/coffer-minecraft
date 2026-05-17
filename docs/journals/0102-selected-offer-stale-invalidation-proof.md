## Summary

Add a narrow stale/invalidation proof for Gate `#1` across the transition from:

- selected-authored value expression
- to lawful pool-scoped fulfillment truth at Core authorization

## Decision

Stale behavior is proven at the authority/Core seam as follows:

- empty selected capture remains non-submittable before Core contact
- exact originating slot persistence is not required
- equivalent value elsewhere in the declared lawful fulfillment pool may still
  authorize successfully
- missing or materially changed fulfillment value denies authorization explicitly
- receiver incapacity before authorization denies explicitly
- no mutation occurs before Core approval

## Rationale

The selected snapshot is an authorship surface, not the fulfillment surface.

That means stale handling should not ask whether the original selected slot still
contains the same stack. It should ask whether the declared transferable value is
still lawfully fulfillable from the current attested fulfillment pool.

The proof therefore belongs at the current Core authority truth boundary:

- if equivalent value still exists anywhere in the lawful `HOTBAR` fulfillment
  pool, authorization may still succeed
- if the value is gone, materially changed, or the receiver cannot receive,
  authorization must deny

This preserves the authorship vs fulfillment distinction without forcing slot-scoped
fulfillment semantics.

## Scope

Included:

- Core-side stale/invalidation denial proof for authored value removed before
  submission
- Core-side stale/invalidation approval proof for equivalent value elsewhere in
  the pool
- Core-side stale/invalidation denial proof for materially changed value before
  submission
- Core-side stale/invalidation denial proof for receiver incapacity before
  authorization
- confirmation that empty selected capture remains non-submittable before Core

Excluded:

- full exchange implementation
- UI or confirmation flow
- receipt implementation
- slot-scoped fulfillment redesign
- live gameplay polish

## Verification

The proof chain now explicitly shows:

- empty selected capture is refused before request construction
- removal of the authored value from the fulfillment pool denies Core
  authorization with `minecraft.value.not_removable`
- equivalent authored value elsewhere in the same lawful pool can still satisfy
  authorization
- materially changed authored value does not silently satisfy authorization
- receiver incapacity denies with `minecraft.value.not_receivable`
- denial leaves inventory unchanged before any mutation execution

## Uncertainties

Still unresolved for live Gate `#1`:

- player-facing stale messaging or confirmation policy
- whether any pre-submission adapter-side freshness check is desirable, as
  distinct from the already-proven authority truth at authorization time
- live runtime behavior after approval when post-approval drift occurs, beyond the
  existing paved Runtime proofs
