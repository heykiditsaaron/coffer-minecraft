# Authorship Surfaces And Lawful Fulfillment Surfaces Reference

## Mirror Note

This document is substrate-derived and mirrored here for repo-local reasoning
continuity.

Future `coffer-minecraft` collaborators and agents may not have reliable
cross-repo access to `~/dev/coffer`, so this copy exists to keep the
authorship-surface vs lawful-fulfillment-surface distinction available inside
this repository.

This mirror is not independently canonical.

Minecraft/Fabric work should adapt this distinction, not redefine it.

The canonical substrate source currently lives in
`dev/coffer/docs/journals/0076-authorship-surfaces-and-lawful-fulfillment-surfaces-clarification.md`.

## Summary

Canonize a substrate-level distinction that recent convergence has already made
visible:
- authorship surfaces describe how participants express transferable-value
  intent
- lawful fulfillment surfaces describe where authorities evaluate whether that
  declared value can actually be removed, received, and owned lawfully

This is a clarification and stabilization step. It does not redesign Core
Law, add new runtime semantics, or force slot-scoped fulfillment behavior.

## Decision

Treat authorship surfaces and lawful fulfillment surfaces as distinct but
related layers of the same interaction.

Authorship may preserve contextual specificity, including selected-slot or
surface identity, when participants declare transferable-value intent.

Lawful fulfillment remains authority-facing and pool/container scoped unless a
future domain explicitly proves narrower semantics are lawfully required.

Core/arbitration continues to evaluate declared transferable-value identity
plus explicit authority truth. It does not directly arbitrate authorship
surface mechanics.

## Rationale

Recent Gate #1 proof pressure clarified an important convergence rather than
producing a new philosophy.

Selected-slot identity is useful and sometimes important at authorship time.
It helps explain what the participant meant to offer, what interaction context
they acted from, and what later stale/accountability interpretation may need
to remember.

That usefulness does not mean lawful fulfillment must remain bound to the exact
originating slot. The current proven interaction surface instead points toward
authority-facing fulfillment pools where removability, receivability, and
ownership are evaluated across the declared lawful pool rather than by exact
slot persistence.

Stabilizing that distinction now prevents two kinds of drift:
- over-narrowing fulfillment into premature slot-preserving semantics
- over-flattening authorship into a context-free pool that loses meaningful
  participant intent

## Scope

Included:
- authorship-surface clarification
- lawful-fulfillment-surface clarification
- Core/arbitration clarification
- non-goals and anti-overreach constraints
- future compatibility note for broader interaction surfaces

Excluded:
- Core Law redesign
- new runtime semantics
- UI or gameplay requirements
- permissions doctrine
- storage schema
- implementation detail
- forced slot-scoped fulfillment semantics

## Authorship Surfaces

Authorship surfaces are participant-facing intent-expression surfaces.

They are the selected or contextual interaction boundaries through which a
participant says, in effect:

"This is the transferable value I am declaring into lawful exchange."

At this layer:
- contextual specificity matters
- selected-slot identity may matter
- selected region or interaction surface identity may matter
- the participant-facing origin of the declaration may matter

Selected-slot or surface identity should therefore be understood as authorship
metadata: context that helps construct and preserve the participant's declared
intent without automatically becoming the fulfillment rule itself.

This preservation is useful because authorship context may later matter for:
- explanation
- stale or invalidation feedback
- accountability interpretation
- modded interaction surfaces
- future UX experimentation

Authorship surfaces are not empty decoration. They can carry meaningful
declarative specificity. But that specificity should not be silently upgraded
into a stronger fulfillment promise than the lawful domain has actually
attested.

## Lawful Fulfillment Surfaces

Lawful fulfillment surfaces are authority-facing fulfillment pools.

They are the surfaces within which an authority evaluates:
- removability
- receivability
- ownership
- equivalent lawful fulfillment of the declared transferable value

Within this layer, the key question is not:

"Did the value remain in the exact originating slot?"

The key question is:

"Can the declared transferable value be lawfully fulfilled within the declared
fulfillment surface the authority is attesting?"

This is why exact originating slot persistence is not inherently required by
the currently proven substrate.

If the authority can explicitly attest that the declared transferable value is
lawfully removable from, receivable into, and ownable within the declared
pool/container surface, fulfillment may remain lawful even when exact slot
continuity is not preserved.

This does not collapse all inventory meaning into one undifferentiated space.
It means that the lawful fulfillment surface is pool/container scoped where the
domain has actually proven that scope, and not narrower by default merely
because authorship began at a selected slot.

This framing also preserves future extension to:
- modded inventory surfaces
- container-backed ownership pools
- alternate inventory regions
- other lawful storage-like surfaces that are not best modeled as one exact
  originating slot

## Core And Arbitration Clarification

Core/arbitration does not directly arbitrate authorship surfaces.

Core does not decide whether a selected slot, selected hand, selected region,
or other participant-facing authorship surface should remain the exact
fulfillment site.

Core arbitrates:
- declared transferable-value identity
- explicit authority truth
- lawful authorization consequences derived from that truth

For this clarification, the relevant Core question is:

"Has the authority explicitly attested that lawful fulfillment is possible
within the declared fulfillment surface for the declared transferable value?"

If yes, Core may arbitrate on that explicit truth.

If no, Core must not infer fulfillment from authorship context alone.

This preserves the existing separation:
- adapters and interaction surfaces capture intent
- authorities attest domain truth
- Core arbitrates declared value plus explicit truth
- Runtime executes authorized mutations without redefining either layer

## Non-Goals And Anti-Overreach

This clarification does not erase slot or surface identity conceptually.

It does not require slot-scoped fulfillment semantics.

It does not authorize hidden widening of undeclared value meaning.

It does not redefine existing Core Law.

It does not imply arbitrary equivalence or fuzzy matching.

More specifically:
- authorship metadata is not permission to reinterpret the declared value into
  something broader than what was actually declared
- pool/container-scoped fulfillment is not permission to substitute merely
  similar value without explicit binding/authority equivalence rules
- preserving selected-slot context does not mean the system must promise exact
  slot persistence at execution time
- declining to require exact slot persistence does not mean every nearby or
  convenient unit is automatically lawful fulfillment

The separation is therefore narrow and conservative:
- preserve authorship specificity where it is real
- require explicit authority truth for lawful fulfillment
- avoid inventing stronger slot guarantees or looser equivalence claims than
  the substrate has earned

## Future Compatibility Note

This separation is intentionally future-compatible.

It preserves room for lawful interaction surfaces that may later include:
- modded inventories
- container-backed ownership pools
- alternate inventory regions
- future UX experimentation around authorship context and explanation

The point is not to pre-design those systems now.

The point is to avoid prematurely constraining fulfillment semantics to exact
slot persistence merely because selected-slot identity was useful at authorship
time, while also avoiding a flattening move that would discard valuable
authorship context before future surfaces have a chance to use it lawfully.

## Verification

Verification for this step consists of:
- `git status --short`
- `git diff --check`

## Uncertainties

This clarification stabilizes current convergence, but it does not answer
every future authorship or fulfillment question.

Still deferred:
- when a domain might need stricter fulfillment-site guarantees
- which future authorship metadata deserves durable accountability exposure
- how modded or alternate container surfaces should declare their fulfillment
  pools in detail

Those questions should be answered only from future pressure, not by widening
this clarification into a new general semantics layer.
