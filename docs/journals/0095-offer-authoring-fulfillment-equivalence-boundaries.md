## Summary

Clarify the lawful distinction between:

- offer-authoring boundaries
- removably-owned fulfillment boundaries
- transferable-value equivalence boundaries

This entry does not retcon prior journals. It records the missing distinction that
the discovery-vessel era had not stated explicitly enough.

## Decision

Treat these as distinct boundaries:

- offer authoring is intentionally narrower than fulfillment
- fulfillment may lawfully source from any removably-owned player location that
  still satisfies the offered transferable value equivalently
- equivalence is determined by transferable-value identity semantics, not by
  material name alone

## Rationale

### Offer-Authoring Boundary

Offer authoring is intentionally restricted to visible player-facing selection
surfaces such as:

- selected hotbar slot
- other explicitly allowed visible selection surfaces

This restriction matters because a player-facing offer should originate from a
surface the player can intentionally inspect and choose. Hidden inventory-wide
declaration is intentionally restricted so the offer does not silently become
"anything matching in my inventory." That preserves sane player-facing intent and
keeps the authored offer tied to a truthful, inspectable selection act.

### Removably-Owned Fulfillment Boundary

Fulfillment/removal is a different question from offer authoring.

Once an offer has been authored truthfully, lawful fulfillment may source the
offered value from any player-assigned removably-owned location that can still
satisfy that offered transferable value equivalently. Exact originating slot
persistence is not inherently required. Inventory drift, stack movement, or lawful
consolidation can remain acceptable if equivalent value still truthfully exists
within the player's removably-owned authority surface.

This distinction matters because otherwise a truthful offer could be invalidated by
non-counterfeit inventory drift even when the player still removably owns the same
offered value in an equivalent form.

### Transferable-Value Equivalence Boundary

Equivalent value does not mean merely similar material.

Fulfillment must preserve transferable-value identity as defined by the binding and
authority semantics. That includes distinctions such as:

- item identity
- count
- NBT
- enchantments
- durability
- metadata-relevant distinctness
- other binding-defined identity distinctions

Examples:

- an enchanted iron sword is not equivalent to a plain iron sword
- a damaged iron sword is not equivalent to a full durability iron sword

Equivalence truth remains authority-bound. This clarification does not weaken
anti-counterfeit reasoning. It only distinguishes lawful equivalent fulfillment from
unlawful substitution.

## Scope

Included:

- explicit separation of authored offer surface from fulfillment source surface
- explicit statement that fulfillment may use any removably-owned equivalent source
- explicit statement that equivalence is richer than material-only matching
- interpretation guidance for Gate `#1`

Excluded:

- final implementation shape
- substrate contract redesign
- mutation or Runtime work
- confirmation, receipts, or gameplay flow

## Impact on Gate #1

Selected-slot capture remains valuable for truthful offer expression.

Current region-based removability semantics may still be lawful for fulfillment if,
and only if, they preserve equivalent transferable-value truth rather than merely
material similarity. The remaining bridge question is therefore narrower than
"must fulfillment come from the exact authored slot?" The stronger unresolved
question is whether the current handoff can preserve equivalence-bounded fulfillment
truth without hidden assumptions or weakened authority semantics.

Journal `0094` should therefore be interpreted as a valid deferral against claiming
selected-slot persistence as the attested fulfillment boundary. It should not be
read to mean that lawful fulfillment necessarily requires exact slot persistence.

## Verification

No code changes.

This step is documentation-only clarification.

## Uncertainties

Still unresolved:

- whether the current binding/authority path proves equivalence richly enough for
  lawful fulfillment beyond material/count matching
- whether selected offer expression and later equivalent fulfillment can be linked
  without hidden assumptions
- whether a binding extension is sufficient, or whether a later substrate change is
  required, once the equivalence-preserving bridge is specified
