## Summary

Add a construction-only proof for Gate `#1` showing that selected authored
inventory value can be assembled into a minimal Core-facing exchange request shape
without entering Runtime, mutation execution, confirmation UX, receipts, or
gameplay interaction.

## Decision

The minimal request assembly step is safe.

Selected authored value can be prepared into a lawful Core-facing request shape as
explicit declared value material, provided that:

- each selected capture is materialized
- actor surfaces are stated explicitly
- transferable-value identity is preserved in the declared value descriptor

## Rationale

The repository already had:

- adapter-local selected capture proof
- clarified distinction between offer authoring and fulfillment
- equivalence-preserving removability proof within current authority surfaces
- paved TransferableValue payload construction

The missing proof was whether selected authored value could actually be assembled
into the minimal request shape that Core expects.

This step proves that:

- two actors can be represented distinctly
- each participant's selected authored value becomes one explicit declared value
- item identity, quantity, and NBT survive assembly unchanged
- non-equivalent metadata is not silently collapsed during request preparation
- empty selected capture is refused before Core-facing payload construction
- assembly stops at construction and does not invoke Runtime or mutation execution

## Scope

Included:

- a narrow Fabric-side request assembly helper
- construction-only tests for actor/value preservation
- refusal proof for empty selected capture
- proof that the assembly surface does not expose Runtime, mutation, confirmation,
  or receipt behavior

Excluded:

- Core arbitration proof beyond construction success
- Runtime execution
- mutation application
- live gameplay interaction
- confirmation UX
- receipt implementation

## Verification

Fabric tests now prove:

- selected authored values can be assembled into a construction-successful
  `ExchangePayload`
- actor declarations remain distinct
- declared value descriptors preserve exact authored identity information
- non-materialized selected capture remains non-submittable

## Uncertainties

This does not yet prove the broader fulfillment surface question.

The remaining bridge is whether Gate `#1` should continue to assemble against the
currently explicit region actor surface, or whether a later binding extension is
needed to express a wider player-owned fulfillment surface without weakening
authority truth.
