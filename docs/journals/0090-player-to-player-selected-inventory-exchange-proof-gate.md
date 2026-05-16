# 0090 Player-to-Player Selected Inventory Exchange Proof Gate

## Summary

Define what must be proven before release-gate interaction `#1` can count as
earned in this repository:

- player-to-player
- inventory-selected transferable value exchange
- no ledger value
- no valuation
- no admin shop

This is a readiness and proof-conditions note, not implementation work and not
a claim that the current repository already satisfies the gate.

## Decision

Treat release-gate `#1` as proven only when a bounded player-to-player
inventory-selected exchange can be shown to pass all required participant,
Core, Runtime, mutation, and accountability proofs without counterfeit
certainty.

Use the existing substrate lifecycle and receipt mirrors, the current
Minecraft inventory binding contract, the ghost-adapter accountability proofs,
and the Fabric/Core/Runtime seam journals as the governing reference surfaces.

Do not redefine Core law, substrate lifecycle topology, or receipt topology in
this step.

## Rationale

The repository already contains meaningful proof fragments:

- Minecraft inventory binding semantics for player inventories
- substrate-shaped atomic-swap construction and Core arbitration tests
- Runtime success, failure, and unknown proofs in binding and ghost-adapter
  spaces
- Fabric-side construction, Core, and inert Runtime seam contact/accountability
  proofs

Those proofs are necessary but not yet sufficient for the first release-gate
interaction.

Release-gate `#1` is not merely "can two inventories be mutated in a happy
path." It is "can Coffer truthfully mediate a selected inventory exchange
between two players, preserve explicit consent and stale-state refusal, and
produce trustworthy participant/admin outcome surfaces without counterfeiting
completion."

## Scope

Included:

- proof conditions for player-to-player inventory-selected transferable value
  exchange
- the minimum implementation roadmap needed to earn those proofs
- what current repo-local evidence can already support
- what remains unresolved or externally dependent

Excluded:

- gameplay implementation
- UI, chat, commands, menus, or polish
- permissions work
- admin shops
- ledger/currency value
- valuation
- batch liquidation
- rollback tooling
- operational runbooks
- release-readiness claims

## Interaction Definition

The first release-gate interaction is only:

- one initiating player selecting owned inventory value
- one recipient player selecting owned inventory value
- a proposed exchange of those selected transferable values
- explicit dual-party review and confirmation before any mutation contact

It does not include:

- ledger value
- valuation or price discovery
- admin shop semantics
- batch liquidation
- permissions lens behavior
- declarative value offering beyond current selected value
- gameplay polish claims

## Required Participant Flow

The gate is not proven unless the interaction can lawfully demonstrate all of
the following:

- the initiator can select owned inventory value for the proposed exchange
- the recipient can select owned inventory value for the proposed exchange
- each party can review the current exchange state before confirmation
- each party confirms explicitly
- stale or invalidated selected value blocks approval
- no mutation occurs before lawful authorization and explicit confirmation

Implications:

- silence is not consent
- prior validity does not survive stale or invalidated context automatically
- selection visibility before receipt-worthy transition remains temporary
  interaction state, not durable success evidence

## Required Coffer Participation

The gate is not proven unless the interaction can lawfully demonstrate all of
the following:

- the adapter captures platform-provided inventory reality rather than
  inventing local substitutes
- the inventory authority attests ownership, removability, receivability, or
  equivalent required truths at the Minecraft boundary
- Core arbitrates explicitly
- Runtime boundary participates only after Core approval
- mutation/execution occurs only through the authorized mutation plan
- accountability distinguishes earned stages for:
  - contact/capture
  - denial
  - approval
  - Runtime participation
  - execution or mutation participation where actually earned

Implications:

- approval is not execution
- Runtime contact is not completion
- no platform path may mutate inventory before the lawful arbitration boundary
  has been crossed

## Required Receipt And Accountability Expectations

The gate is not proven unless the interaction can truthfully surface all of the
following:

- participant-facing receipt surface for:
  - refusal
  - interruption
  - unknown execution
  - lawful completion
- administrator-facing reconstructability through accountability records such as
  SER/CER and related earned-content projection
- no counterfeit success
- unknown remains unknown
- authorization is not execution
- execution is not completion

The current substrate mirrors remain controlling here:

- preparatory states such as selection, offer formation, pending attestation,
  and awaiting counterparty are not durable success receipts by default
- receipt-worthy states begin only when a meaningful lawful transition has been
  earned

## Required Refusal Cases

The gate is not proven unless refusal or non-success handling is explicitly
provable for all of the following categories:

- missing consent
- stale selected value
- selected value no longer owned or no longer removable
- recipient cannot receive
- authority unavailable or other non-actionable infrastructure boundary
- Core denial
- Runtime non-success or unknown
- interruption before execution
- interruption during or after execution attempt

These categories must remain distinct where the topology already distinguishes
them. In particular:

- actionable refusal must not collapse into non-actionable refusal
- Coffer-intrinsic denial must not collapse into container-boundary refusal
- interruption must not be counterfeited as denial or completion

## Required Tests And Proofs Before Release-Gate #2

The repository should not move to release-gate `#2` until all of the following
proof categories are earned for gate `#1`:

- construction/contact refusal tests
- Core denial tests
- Core approval tests
- Runtime participation tests
- mutation success, failure, and unknown tests where safely reachable
- no-partial-mutation proof for the bounded interaction claims being made
- stale/invalidation proof
- participant receipt eligibility proof
- administrator receipt/accountability eligibility proof
- SER/CER line-shape and earned-content proof
- verification command expectations for the proving step

Minimum verification expectation for a proof-bearing implementation step:

- targeted tests for the specific proof category being added
- relevant aggregate build/test command(s) for the touched area
- `git status --short`
- `git diff --check`

## Current Repo-Local Evidence

This repository already contains evidence relevant to the gate, but only in
bounded pieces:

- `docs/contracts/inventory-binding.md` and inventory tests establish current
  Minecraft-specific descriptor, matching, container, and mutation semantics
  for player inventory material
- `bindings/inventory/.../MinecraftTransferableValueEndToEndTest.java`
  demonstrates Core denial, Core approval, Runtime success, Runtime failure,
  and Runtime unknown for a bounded two-player inventory swap construction path
- ghost-adapter tests and journals establish a minimal accountability
  projection shape where capture, Core denial, and Runtime result categories
  can be distinguished without counterfeiting absent participation
- Fabric probe journals establish proven construction contact, Core contact, and
  inert Runtime contact, while explicitly deferring a truthful mutation seam

This evidence supports the roadmap, but it does not yet prove the whole
release-gate interaction.

## Still Unproven For This Gate

As of this note, this repository does not yet verify all of the following for
the full release-gate interaction:

- explicit two-party review and confirmation flow
- stale or invalidated selection refusal across a player-to-player interaction
- participant-facing receipt surfaces for refusal, interruption, unknown, and
  lawful completion
- administrator-facing reconstructability for the full interaction rather than
  only narrow probe/projection paths
- interruption classification before execution versus during/after execution
  attempt on the release path
- truthful live mutation-boundary participation on Fabric
- final no-partial-mutation proof for the complete release-gate claim

None of these should be assumed from the current code or journals.

## Minimum Implementation Roadmap

The smallest defensible roadmap implied by current repo evidence is:

1. Define the bounded interaction capture shape for two-player selected
   inventory exchange without adding gameplay polish or declarative offering.
2. Prove explicit dual-party confirmation and stale/invalidation blocking before
   mutation authorization.
3. Prove the authority/Core path distinguishes denial from approval for this
   exact interaction shape.
4. Prove Runtime participation only after approval, with unknown remaining
   unknown where certainty is not earned.
5. Prove mutation success/failure/unknown behavior for the claimed interaction
   surface, including no-early-mutation and no-partial-mutation obligations.
6. Prove participant/admin receipt eligibility and earned accountability
   content for the receipt-worthy lifecycle states of this gate.

This roadmap is intentionally proof-shaped, not feature-shaped.

## Explicitly Deferred

Still deferred beyond this gate:

- ledger or currency value
- valuation
- admin shops
- batch liquidation
- permissions integration
- rollback tooling
- operational documentation
- release readiness

## Verification

Verification for this journal step consists of:

- `git status --short`
- `git diff --check`

## Uncertainties

The largest current dependencies or unknowns are:

- what the smallest lawful player-to-player confirmation surface looks like in
  this repository without drifting into gameplay/UI design
- what proof vessel can earn truthful live mutation participation on Fabric
  without violating the current mutation-seam cautions
- how participant/admin receipt surfaces should attach to the existing minimal
  accountability topology without redefining substrate receipt law

Those questions must be resolved by later narrow proof steps, not guessed into
this gate definition.
