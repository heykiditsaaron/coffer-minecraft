# Runtime Ambiguity Disappearance Proof

## Summary

Pressure-tested runtime execution ambiguity at the existing container-resolution
boundary by simulating actor/container disappearance after approval but before
runtime mutation execution.

## Decision

This step proves only the existing survivability classification at the lawful
participation boundary.

When runtime execution can no longer resolve one side of an already-approved
Minecraft atomic swap, the result must remain explicit non-success. It must not
pretend determinism, and it must not mutate inventories.

## Rationale

Current proof already covered:

- approval-time denial on invalid exchanges
- runtime failure on post-approval inventory drift
- unknown classification for malformed runtime execution material
- unavailable-container classification at the container boundary

The next narrow ambiguity seam is disappearance of a runtime execution surface
after approval. That can be exercised through the existing resolver boundary
without introducing timeout policy, retry behavior, rollback, or session
orchestration.

## Scope

Included:

- proving runtime actor/container disappearance after approval yields explicit
  unknown classification
- proving that disappearance path leaves both inventories unchanged

Excluded:

- disconnect sequencing policy
- timeout invention
- retries or remediation orchestration
- gameplay, UX, commands, or adapter behavior
- SER/CER implementation

## Verification

Now proven:

- one runtime-contact ambiguity path exists where approval remains valid but
  runtime execution loses container resolution
- that path produces explicit unknown classification with
  `minecraft.container.unavailable`
- that path does not mutate either inventory

## Uncertainties

Still unresolved:

- ordered disconnect semantics across larger execution lifecycles
- timeout-specific classification
- distinction between different external causes of runtime contact loss
- any broader accountability projection above the current reason-code surface

These behaviors must not be assumed from the current proof surface.
