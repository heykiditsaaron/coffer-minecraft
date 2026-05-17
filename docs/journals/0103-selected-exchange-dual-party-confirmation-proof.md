## Summary

Add the smallest test-first dual-party confirmation proof for Gate `#1`.

This step proves only submission readiness gating for selected inventory exchange.
It does not implement final UX, Core submission, Runtime participation, or
mutation.

## Decision

Use a narrow adapter-side confirmation model keyed to an explicit selected-exchange
state fingerprint.

Submission readiness now requires:

- both participants have materialized selected-authored values
- both participants have explicitly confirmed the same exchange state

Any state change invalidates prior confirmation.

## Rationale

The current repo already proves selected capture, Core-facing assembly, authority
truth, and Runtime gating in isolation. The missing seam for this step was the
smallest lawful participant confirmation model before Core submission.

This proof keeps confirmation intentionally narrow:

- initiator selected value alone is not enough
- recipient selected value alone is not enough
- one-sided confirmation is not enough
- stale confirmation cannot be reused across changed exchange state

The confirmation state is tied to authored exchange state, not to later lawful
pool-scoped fulfillment truth. This preserves the distinction between:

- authored state both players review and confirm
- later authority/Core attestation of lawful fulfillment

## Scope

Included:

- adapter-side exchange state fingerprinting
- dual-party confirmation ledger
- submission readiness proof
- invalidation when any selected state changes

Excluded:

- final UX or chat/GUI flow
- Core submission orchestration
- Runtime participation
- mutation
- receipts

## Verification

The proof now shows:

- initiator selected value alone is not enough
- recipient selected value alone is not enough
- one-sided confirmation is not enough
- matching dual confirmation permits submission readiness
- changed selected value/state invalidates prior confirmation
- stale confirmation cannot be reused
- confirmation surface does not expose Runtime, mutation, or submission behavior

## Uncertainties

Still unresolved for live Gate `#1`:

- how live player interaction presents review and confirmation
- whether confirmation invalidation should emit adapter-local accountability
  records
- how confirmation state is stored or expired in a live Fabric service
