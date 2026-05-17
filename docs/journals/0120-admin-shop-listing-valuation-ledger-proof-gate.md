# 0120 Admin Shop Listing Valuation And Ledger Proof Gate

## Summary

Define what must be proven before Gate `2.2` can count as earned in this
repository:

- admin shop listing exchange with explicit valuation participation
- admin shop listing exchange with explicit ledger participation
- player selected inventory authorship surface
- server/admin-authored listing surface
- no batch liquidation
- no final UX claim

This is a proof-gate definition only. It is not implementation work and not a
claim that Gate `2.2` currently works.

## Decision

Treat Gate `2.2` as proven only when a bounded admin shop listing exchange with
explicit valuation and explicit ledger participation can be shown to pass all
required participant, listing, valuation, ledger, Core, Runtime, mutation, and
accountability proofs without counterfeit certainty.

Gate `2.2` inherits the Gate `#2` and Gate `2.1` proof discipline, but adds
two new explicit participation surfaces:

- valuation participation
- ledger participation

Neither may be treated as implicit shop behavior, inferred pricing, or hidden
Runtime side effect.

This step does not redefine Core law, prior gates, substrate lifecycle
topology, or receipt topology.

## Rationale

Gate `2.1` proved a bounded shop exchange where both sides were item-bounded
transferable values. Gate `2.2` is the first bounded shop slice where the
exchange can lawfully include abstract value representation instead of only
item-for-item equivalence.

That introduces new proof obligations:

- valuation must be explicit rather than assumed
- ledger truth must be explicit rather than assumed
- economic completion must not be inferred from item mutation alone
- item success must not be inferred from ledger success alone

The first honest proof gate must therefore require explicit declared
participation by valuation and ledger surfaces before any mutation proof is
attempted.

## Scope

Included:

- proof conditions for bounded admin shop listing exchange with valuation and
  ledger participation
- explicit requirements for valuation semantics
- explicit requirements for ledger semantics
- required refusal, interruption, mutation-integrity, and accountability
  obligations
- identification of the smallest likely next proof after this definition

Excluded:

- implementation
- assumptions about a specific ledger backend
- permissions behavior
- batch liquidation
- final UI/UX
- rollback or recovery tooling
- production economy balancing
- release-readiness claims
- broader Gate `#3` expansion

## Interaction Definition

Gate `2.2` is only:

- one player selecting owned transferable value through the current selected
  inventory authorship surface
- one server/admin-authored persistent listing chosen or engaged by the player
- one bounded exchange where the listing defines:
  - the shop-offered value or value condition
  - the accepted player-side value or ledger condition
  - explicit valuation participation where needed
  - explicit ledger participation where needed
- one concrete instantiated exchange created when the player engages that
  listing

It does not include:

- batch liquidation
- final UI/UX claims
- permissions lens behavior
- production economy balancing assumptions

## Required Valuation Semantics

Gate `2.2` is not proven unless the interaction can lawfully demonstrate all
of the following:

- listing-local valuation may override broader or base valuation if such a
  broader surface exists
- valuation participation is explicit and reconstructable in the concrete
  exchange
- no hidden pricing assumptions exist
- no undeclared exchange-rate behavior exists
- valuation truth is not inferred from Runtime mutation results

Implications:

- “shop price” is not enough unless the valuation surface is declared
- value conversion must not be smuggled in as unexplained adapter metadata
- a Runtime success line by itself must not be used as proof that valuation was
  lawful

## Required Ledger Semantics

Gate `2.2` is not proven unless the interaction can lawfully demonstrate all
of the following:

- ledger participation is explicit
- ledger truth comes from declared authority participation
- ledger mutation remains Runtime-bound
- insufficient ledger value denies at Core
- no counterfeit currency creation or destruction is emitted

Implications:

- ledger balance or spendability must not be inferred locally in the adapter
- “player paid” is not truth unless lawful authority and Runtime contact earned
  it
- successful item delivery must not imply successful ledger mutation

## Required Participant And Server Flow

The gate is not proven unless the interaction can lawfully demonstrate all of
the following:

- the player selects inventory-authored value or engages a listing surface
- the listing defines the offered value and the accepted ledger/value
  conditions explicitly
- the concrete instantiated exchange can be reviewed before confirmation
- the player confirms explicitly
- Core authorization is required
- Runtime executes only after Core approval

Implications:

- the listing remains a server-authored standing offer template, not a
  completed exchange
- valuation participation and ledger participation are part of the concrete
  instantiated exchange, not hidden policy behind it
- player review must be over the concrete exchange state, not over an abstract
  shop category

## Required Refusal And Interruption Cases

The gate is not proven unless refusal or non-success handling is explicitly
provable for all of the following categories:

- missing or disabled listing
- stale player value
- stale ledger truth
- insufficient ledger value
- player cannot receive offered value
- valuation mismatch
- Runtime failure
- Runtime unknown
- interruption during execution

These categories must remain distinct where the topology already distinguishes
them. In particular:

- stale player value must not collapse into ledger insufficiency
- valuation mismatch must not collapse into generic construction refusal if it
  is actually a Core-truth problem
- interruption must not be counterfeited as denial or completion
- unknown must remain unknown

## Required No-Partial-Mutation Obligations

Gate `2.2` is not proven unless the interaction can lawfully demonstrate all
of the following:

- no mutation occurs before Core approval
- denial leaves all relevant surfaces unchanged
- failed ledger mutation does not counterfeit item mutation success
- failed item mutation does not counterfeit ledger success
- no hidden partial completion exists
- unknown remains unknown

Implications:

- bounded economic completion must be atomic for the claim being made, or else
  the gate remains open
- if the mutation topology spans item and ledger surfaces without a proven
  no-partial-mutation story, that is a real blocker, not deferred polish

## Required Accountability And Receipt Expectations

Gate `2.2` is not proven unless the interaction can truthfully surface all of
the following:

- ledger participation remains reconstructable
- valuation participation remains reconstructable
- Runtime success, failure, and unknown remain explicit
- no counterfeit economic completion is emitted
- receipts derive from accountable contact rather than becoming the source of
  truth

The current substrate receipt and accountability mirrors remain controlling
here:

- authorization is not execution
- execution is not completion
- valuation contact is not ledger completion
- preparatory review and listing-browsing state are not durable completion
  receipts by default

## Explicitly Deferred

Still deferred beyond Gate `2.2`:

- batch liquidation
- broad economy balancing
- production config UX
- rollback or recovery tooling
- release readiness
- large-scale operational tooling

## Smallest Likely Next Proof

The smallest likely next proof after this document is:

- a test-first construction and refusal proof for the bounded admin shop
  listing surface when valuation and ledger participation become explicit,
  covering:
  - missing or disabled listing refusal
  - explicit valuation-carrier identity
  - explicit ledger-carrier identity
  - refusal when the concrete exchange cannot be lawfully instantiated without
    declared valuation or ledger participation

That is the best first step because it forces the new Gate `2.2` asymmetry into
the open before Core, Runtime, or mutation behavior is attempted:

- value conversion cannot remain implicit
- ledger contact cannot remain implicit

## Verification

Verification for this journal step consists of:

- `git status --short`
- `git diff --check`

## Uncertainties

- whether the smallest truthful valuation carrier should be expressed as a
  listing-local valuation descriptor, a bounded declared authority surface, or
  another narrow authored exchange carrier
- whether the first bounded ledger proof should model spend-only, receive-only,
  or two-sided ledger participation
- whether the narrowest honest Gate `2.2` slice should be item-to-ledger,
  ledger-to-item, or a symmetric listing shape that supports both
