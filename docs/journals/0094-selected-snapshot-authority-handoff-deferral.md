## Summary

Pressure-test Gate `#1` from selected inventory snapshot toward lawful authority/Core
handoff without entering mutation, Runtime execution, confirmation, receipts, or
gameplay flow.

## Decision

Defer the handoff.

Do not bridge the selected snapshot into the current authority/Core path yet.

## Rationale

The current collaborator seam is region-based:

- `player:<uuid>:inventory:hotbar` resolves to the whole hotbar region
- removability is proven by matching quantity anywhere in that region
- descriptor creation carries item material and quantity, not selected-slot boundary

That means an adapter-local selected snapshot can be captured truthfully, but the
current authority handoff cannot preserve the claim "this exact selected slot is the
thing being attested." Reusing the existing hotbar-region path would silently widen
the claim and could approve removal after the originally selected slot drifted empty
while duplicate matching material remained elsewhere in the hotbar.

An adapter-only compatibility layer is therefore not sufficient.

## Scope

Included:

- proof tests for the current widening behavior
- proof that empty selected capture does not produce a truthful current authority
  value on its own
- documentation of the missing bridge

Excluded:

- mutation changes
- Runtime changes
- Core law changes
- confirmation or receipt work
- gameplay interaction

## Verification

Relevant proof coverage added in Fabric tests to show:

- current hotbar authority handoff would accept duplicate matching material outside
  the originally selected slot
- current resolver surface has no selected-slot identity carrier beyond hotbar region
- current descriptor flow ignores selected boundary metadata

## Uncertainties

The most likely next bridge is a binding extension, not an adapter-only shim.

That extension would need a lawful slot-scoped attestation surface such as:

- a slot-specific actor/container identity, or
- another binding-level authority carrier that proves selected-slot removability
  without widening to region semantics

If the existing TransferableValue request and runtime surfaces can carry such a
slot-scoped binding identity unchanged, no substrate semantic change is needed.
If they cannot, a substrate change may be required later, but that should be proven
only after the binding-level shape is specified.
