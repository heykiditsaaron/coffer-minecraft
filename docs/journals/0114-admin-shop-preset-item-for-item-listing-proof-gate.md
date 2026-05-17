# 0114 Admin Shop Preset Item-for-Item Listing Proof Gate

## Summary

Define what must be proven before Gate `2.1` can count as earned in this
repository:

- admin shop preset item-for-item listing exchange
- player inventory-selected counter-offer
- server/admin-authored standing listing
- no ledger value
- no valuation

This is a proof-gate definition only. It is not implementation work and not a
claim that Gate `2.1` currently works.

## Decision

Treat Gate `2.1` as proven only when a bounded admin shop preset
item-for-item listing exchange can be shown to pass all required listing,
participant, Core, Runtime, mutation, and accountability proofs without
counterfeit certainty.

Gate `2.1` inherits the general Gate `#2` proof discipline, but narrows it to
the first concrete shop shape:

- a persistent server/admin-authored standing offer template
- a preset shop-offered transferable value
- a preset accepted player counter-offer
- a concrete exchange instantiated only when the player engages the listing

This step does not redefine Core law, substrate lifecycle topology, or receipt
topology.

## Rationale

Gate `#2` introduced the asymmetry between:

- player-authored selected inventory intent
- server-authored shop exchange surfaces

Gate `2.1` is the smallest useful slice of that problem because it avoids:

- valuation
- ledger semantics
- broad shop orchestration
- multi-listing marketplace behavior

while still forcing one important new proof distinction:

- infinite faucet listing behavior
- finite faucet listing behavior

Both are lawful admin-shop concepts, but they imply different proof
obligations. Infinite faucet mode proves a server-authored offered value that
does not depend on finite depletion. Finite faucet mode proves the scarcer and
more inventory-sensitive shape where shop-side removability, staleness, and
no-partial-mutation must all be earned explicitly.

## Scope

Included:

- proof conditions for preset item-for-item admin shop listing exchange
- explicit infinite versus finite faucet proof requirements
- identification of the smallest likely next proof after this definition

Excluded:

- implementation
- ledger/currency value
- valuation or pricing logic
- permissions lens behavior
- batch liquidation
- final UI/UX claims
- rollback/recovery tooling
- release-readiness claims
- broader Gate `#2` and Gate `#3` expansion

## Interaction Definition

Gate `2.1` is only:

- one player selecting owned inventory value through the current selected
  authorship surface
- one server/admin-authored preset listing chosen by the player
- one preset item-for-item exchange where the listing defines:
  - the shop-offered transferable value
  - the accepted player counter-offer
- one concrete instantiated exchange created when the player engages that
  listing
- one of two shop supply modes:
  - infinite faucet
  - finite faucet

It does not include:

- ledger value
- valuation or pricing systems
- permissions lens behavior
- batch liquidation
- final UI/UX claims

## Required Flow

The gate is not proven unless the interaction can lawfully demonstrate all of
the following:

- the player selects or chooses a concrete listing
- the listing defines both the offered value and the accepted counter-offer
- the player's selected value must satisfy the accepted counter-offer
- the concrete instantiated exchange can be reviewed before confirmation
- the player confirms explicitly
- Core authorization is required
- Runtime mutation occurs only after Core approval

Implications:

- the persistent listing is a standing server-authored offer template, not a
  completed exchange
- the player does not confirm a generic catalog entry; the player confirms the
  concrete instantiated exchange
- player selection and listing selection remain preparatory state until lawful
  authorization is earned

## Infinite Faucet Proof Requirements

The infinite faucet slice is not proven unless the interaction can lawfully
demonstrate all of the following:

- the listing can provide its offered value without finite depletion
- player-side selected value is still lawfully removed
- player receivability for the offered value is still lawfully attested
- no fake shop inventory depletion is implied
- accountability and receipt projection distinguish infinite supply behavior
  honestly

Implications:

- infinite faucet mode cannot pretend there was finite shop inventory scarcity
  if there was none
- infinite faucet mode still requires lawful player-side removability,
  receivability, Core authorization, and Runtime participation
- infinite faucet mode is not a bypass around inventory truth; it is a different
  supply truth

## Finite Faucet Proof Requirements

The finite faucet slice is not proven unless the interaction can lawfully
demonstrate all of the following:

- shop-side supply and removability are attested explicitly
- stale or unavailable finite shop supply denies or refuses honestly
- no mutation occurs if finite shop supply is unavailable
- successful exchange mutates both player inventory and finite shop supply
  consistently
- no partial mutation occurs across the bounded exchange claim

Implications:

- finite faucet mode carries real scarcity and stale-state obligations
- finite faucet mode must prove shop-side mutation integrity, not only
  player-side truth

## Required Refusal Cases

The gate is not proven unless refusal or non-success handling is explicitly
provable for all of the following categories:

- missing listing
- listing disabled or unavailable
- player selected value does not match the accepted counter-offer
- player value stale or invalidated
- player cannot receive the shop offer
- finite shop supply unavailable or stale
- shop cannot receive player value, if the listing shape requires it
- Core denial
- Runtime failure
- Runtime unknown
- interruption before execution
- interruption during execution attempt

These categories must remain distinct where the topology already distinguishes
them. In particular:

- listing absence must not collapse into player-side stale value
- finite supply unavailability must not collapse into infinite faucet behavior
- Core denial must not collapse into container-boundary refusal
- interruption must not be counterfeited as completion

## Required Accountability And Receipt Expectations

The gate is not proven unless the interaction can truthfully surface all of the
following:

- construction refusal remains pre-Core where appropriate
- Core denial remains Core denial
- Runtime result remains success, failure, or unknown
- infinite versus finite supply mode is reconstructable from the earned record
  chain
- no counterfeit completion is emitted

The current substrate receipt and accountability mirrors remain controlling
here:

- authorization is not execution
- execution is not completion
- preparatory listing or selection state is not a durable completion receipt by
  default

## Explicitly Deferred

Still deferred beyond Gate `2.1`:

- ledger or currency value
- global or base valuation
- permissions integration
- batch liquidation
- final UI/UX
- rollback/recovery tooling
- production operational documentation
- release readiness

## Smallest Next Proof

The smallest likely next proof after this document is:

- a test-first construction and refusal proof for the preset admin shop listing
  surface itself, including:
  - missing listing refusal
  - disabled listing refusal
  - player selected value mismatch against the accepted counter-offer
  - explicit infinite versus finite listing mode identity at construction time

That is the best first step because it resolves the first unique Gate `2.1`
problem:

- how a persistent server-authored listing becomes a concrete lawful exchange
  candidate without conflating listing presence, player consent, and actual
  supply truth

## Verification

Verification for this journal step consists of:

- `git status --short`
- `git diff --check`

## Uncertainties

- whether the smallest truthful preset listing surface should be modeled as a
  dedicated listing descriptor, a bounded authored exchange template, or
  another narrow shop-authored carrier
- whether finite faucet mode can remain on a one-value-per-side bounded claim or
  immediately needs a wider inventory-set shape
- whether infinite faucet mode should be represented as a distinct shop-side
  supply truth or as a distinct bounded authority adapter shape
