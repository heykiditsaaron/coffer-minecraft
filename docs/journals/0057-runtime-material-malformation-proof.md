# Runtime Material Malformation Proof

## Summary

Added narrow proof for malformed or unrecognizable runtime material at the
Minecraft inventory binding boundary and at the published TVAL runtime
authority seam.

## Decision

This step proves existing behavior rather than introducing any new recovery or
reinterpretation behavior.

Malformed runtime payload binding material and malformed runtime value-set
descriptor material must produce non-success outcomes. They must not mutate
inventories and must not be treated as fake success.

## Rationale

The previous gate audit identified runtime-material failure proof as the next
smallest survivable step. The binding already has narrow collaborator behavior
for:

- rejecting unrecognizable runtime payload binding material
- refusing runtime value-set reconstruction when descriptor material is invalid

The published TVAL runtime authority also already classifies malformed runtime
execution material as `MUTATION_UNKNOWN` with
`MALFORMED_RUNTIME_DESCRIPTOR`.

Proving those paths directly strengthens survivability and accountability
boundaries without broadening runtime meaning or inventing speculative recovery.

## Scope

Included:

- collaborator tests for malformed runtime payload binding material
- collaborator tests for invalid runtime descriptor/value-set reconstruction
- runtime-authority proof that malformed runtime payload or malformed runtime
  value-set material does not report success and does not mutate inventories

Excluded:

- gameplay behavior
- adapter orchestration
- user-facing UX or messaging
- new classification semantics
- speculative recovery or fallback behavior

## Verification

Now proven:

- malformed runtime payload binding material is rejected as unrecognizable
- malformed runtime descriptor maps prevent value-set reconstruction
- one malformed runtime entry prevents partial value-set success
- malformed runtime payload material at runtime execution yields
  `MUTATION_UNKNOWN`
- malformed runtime value-set material at runtime execution yields
  `MUTATION_UNKNOWN`
- those malformed runtime execution paths do not mutate inventories

## Uncertainties

Still not proven:

- timeout-specific runtime-material classification
- disconnect sequencing beyond unavailable-container proof
- broader malformed-runtime cases across every substrate-owned descriptor field
- adapter-harness accountability projection

These behaviors must not be assumed from the current proof surface.
