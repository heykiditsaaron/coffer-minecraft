## Summary

Reconcile the Gate `#1` proof chain around the question:

- has selected-slot snapshot -> lawful authority/Core handoff been closed without widening into `HOTBAR` region semantics?

## Decision

Reclassify the later selected-offer proofs as proving a different bridge than the one deferred in `0094`.

The original `0094` gap is still open if interpreted strictly as:

- preservation of selected-slot boundary identity into authority/Core handoff

The later `0096`-`0098` proofs do not carry selected-slot boundary identity into
authority/Core. They instead prove that:

- selected-authored value identity can be assembled and arbitrated as an explicit
  declared transferable value
- fulfillment can lawfully occur from the current `HOTBAR` removability surface if
  equivalent value still exists there

## Rationale

`0093` proved an adapter-local snapshot with:

- `selectionKind`
- `slotIndex`
- exact selected value identity

`0094` correctly deferred the next bridge because the current authority surface was
region-based and did not name the selected slot.

The later proofs changed the bridge question:

- `0096` assembles a Core-facing request from the selected-authored value
- `0097` proves Core arbitration for that payload shape
- `0098` proves Runtime participation for that payload shape

Those later steps preserve selected-authored value identity such as item id,
quantity, and NBT, but they do not preserve selected boundary identity at the
authority/Core seam:

- actor refs remain `player:<uuid>:inventory:hotbar`
- actor metadata is empty
- value descriptors carry transferable-value identity only
- selected boundary fields like `selectionKind` and `selectedSlot` are not carried
  into the assembled Core-facing shape

That means the later proof chain does not close the exact `0094` concern. It closes
a narrower and different bridge: selected offer expression can lawfully become a
declared value for region-scoped equivalent fulfillment.

## Scope

Included:

- proof-chain reconciliation across journals `0093`, `0094`, `0096`, `0097`,
  `0098`, and `0099`
- one explicit assembly test proving selected boundary is omitted from the
  Core-facing actor/value shape

Excluded:

- binding or substrate redesign
- mutation changes
- Runtime behavior changes
- UI, receipts, or gameplay flow

## Verification

Evidence now explicitly shows:

- selected capture stores slot boundary locally
- current authority/container resolution remains `HOTBAR` region-scoped
- later selected-offer assembly preserves authored value identity but omits
  selected-slot identity from the Core-facing request shape

## Uncertainties

The smallest next proof, if the original `0094` question still matters, is to prove
one of two things explicitly:

- either selected-slot boundary identity is intentionally unnecessary for lawful
  authority/Core handoff after `0095`, in which case `0094` should be superseded as
  an interpretation issue
- or a slot-scoped binding identity carrier is still required, in which case the
  next proof should demonstrate a minimal lawful carrier without changing substrate
  semantics prematurely
