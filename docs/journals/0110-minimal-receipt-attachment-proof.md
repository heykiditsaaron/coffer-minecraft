## Summary

Add the smallest test-first proof that participant-facing and admin-facing receipt
surfaces can be derived from the existing accountability and outcome chain for
Gate `#1` selected exchange.

This step does not redefine SER/CER and does not make receipts the source of
truth.

## Decision

Use a bounded derived receipt projection:

- participant receipts stay minimal and actionability-aware
- admin receipts remain reconstructable and diagnostic without becoming an
  unbounded debug dump
- incomplete or stale pre-submission states may surface only as explicit temporary
  state visibility

## Rationale

The current repo already has truthful outcome and accountability surfaces:

- Core denial
- Core approval
- Runtime success
- Runtime failure
- Runtime unknown
- pending/interrupted state visibility

The missing step was whether those can be attached to participant and admin receipt
surfaces without inventing certainty or redefining the underlying lifecycle
records.

The proof therefore derives receipts from:

- accountability stage records for contact-earned outcomes
- explicit temporary state visibility for incomplete or stale pre-submission
  states

## Receipt Behavior Proven

- Core denial derives bounded participant/admin receipts
- Runtime success derives completion receipts
- Runtime failure remains distinct from denial and completion
- Runtime unknown remains unknown
- interrupted/incomplete exchange surfaces only as explicit temporary visibility
- stale/invalidated confirmed exchange surfaces only as explicit temporary
  visibility
- Core approval does not become completion
- mere value selection or incomplete offer formation emits no receipt by default

## Participant/Admin Distinction

Participant receipt:

- status
- temporary vs durable
- actionability
- optional code

Admin receipt:

- the same bounded status/actionability view
- latest stage
- compact ordered stage list for reconstruction
- optional code

This keeps the admin surface diagnostic without turning it into a raw debug dump.

## Deferred

Still deferred:

- final UI or chat presentation
- wording catalog
- richer operator tooling
- receipt persistence policy
- any topology change to substrate receipt law

## Survivability

Gate `#1` still appears operationally survivable after receipt attachment proof.

This step strengthens that assessment by showing that the current geometry can
derive participant/admin-facing outcome surfaces honestly from the already-proven
contact and mutation chain, without inflating approval into completion or unknown
into certainty.

## Verification

This step adds bounded receipt projection and tests only.
