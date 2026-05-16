# 0093 Selected Inventory Reality Capture Proof

## Summary

Add the smallest test-first proof for selected inventory reality capture only.

This step proves a Fabric-side, adapter-local snapshot surface for the
currently selected hotbar or main-hand value without entering Core, Runtime,
mutation, exchange execution, confirmation flow, or receipt implementation.

## Decision

Introduce a narrow Fabric-local selected capture surface in `platforms/fabric`
that:

- records the selected boundary as hotbar slot index plus selection kind
- snapshots item identity, count, and optional NBT exactly from the selected
  stack
- represents an empty selected slot as empty captured value
- refuses capture when main-hand and selected-hotbar inputs disagree

Do not add a new substrate or binding API in this step.
Do not claim this solves later actor-ref or Core-submission narrowing.

## Rationale

The current bridge gap was whether selected inventory reality could be captured
precisely without widening into region semantics.

The current binding remains region-based:

- `HOTBAR` means the whole hotbar region
- removability logic aggregates across the resolved region

That means the truthful next step is not to force selected-slot meaning into
the binding. The truthful next step is to prove an adapter-local snapshot that
preserves selected-slot reality before any later authority or Core handoff is
attempted.

## Scope

Included:

- Fabric-side selected capture utility
- tests for exact selected-slot capture
- tests proving duplicate matching material elsewhere is not captured
- tests proving empty selected slot is represented honestly
- tests proving capture is read-only
- tests proving the surface does not expose Core, Runtime, or mutation entry
  behavior

Excluded:

- Core submission
- Runtime participation
- mutation
- exchange execution
- confirmation UX
- receipt implementation
- gameplay polish
- new binding or substrate semantics

## Proof Result

The repository now proves that a selected player inventory value can be
captured into a bounded adapter-local snapshot safely enough for the next Gate
`#1` bridge step.

What is proven:

- selected boundary is precise to one hotbar slot
- captured item identity and count come from that selected slot only
- matching material elsewhere in the hotbar does not widen the capture
- empty selected slot remains empty rather than fabricated into value
- capture does not mutate inventory
- capture remains outside Core, Runtime, and mutation contact

## What Remains Omitted

- no ownership claim beyond the captured selected value
- no claim that current actor-ref semantics can already carry this selected
  boundary into authority/Core without further bridge work
- no stale or invalidation policy
- no participant confirmation flow
- no mutation-boundary proof
- no participant/admin receipt attachment

## Gate #1 Effect

This reduces one concrete bridge gap:
selected inventory reality can now be captured narrowly and honestly at the
Fabric adapter layer.

Gate `#1` remains unproven because the larger bridge still lacks:

- dual-party confirmation
- stale or invalidation handling
- truthful live submission from selected snapshot into lawful authority/Core
  contact
- live mutation-boundary participation
- receipt and accountability attachment for the full interaction

## Verification

This step should verify with:

- `git status --short`
- `git diff --check`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

## Uncertainties

- whether a later compatibility layer is needed to carry selected-slot
  narrowing into authority/Core flow without widening back to hotbar-region
  semantics
- whether selected main-hand and selected hotbar should remain one capture kind
  or split later if offhand or other selection surfaces appear
