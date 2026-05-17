# 0121 Gate 2.2 Ledger Authority Proof Strategy

## Summary

Define how Coffer will represent ledger value for Gate `2.2` before any ledger
implementation work begins.

This step establishes:

- ledger requires an Authority
- production ledger implementation is deferred
- Gate `2.2` should be proven first with a deterministic test-only ledger
  authority
- future wrappers may target Impactor or other economy or ledger systems
- a first-party ledger may exist later, but is not required now

This is a strategy journal only. It is not implementation work and not a claim
that Gate `2.2` currently works.

## Decision

Represent ledger value in Gate `2.2` as an explicit authority-governed value
surface rather than as selected inventory reality or adapter-local bookkeeping.

The first proof path should use a deterministic test-only ledger authority that
proves Coffer semantics cleanly:

- declared ledger value
- explicit balance truth
- explicit debit or credit truth
- explicit Runtime-bound debit or credit mutation

Production ledger integration remains deferred. No specific external economy
system is mandatory in this step.

## Rationale

Gate `2.2` is the first shop slice where exchange completion may depend on both:

- inventory mutation
- ledger mutation

That makes it unsafe to treat ledger as:

- implicit shop metadata
- inferred pricing state
- a Runtime-only side effect without prior Core truth

The smallest honest path is therefore:

- define ledger as a real authority-bounded value surface
- prove the semantics with a deterministic test authority first
- defer production wrappers and storage choices until after the bounded law is
  proven

This avoids fake confidence from prematurely coupling Coffer semantics to one
plugin or storage system.

## Scope

Included:

- initial Gate `2.2` ledger representation strategy
- initial ledger value semantics
- initial truth and mutation surface definitions
- explicit deferral of production integration choices

Excluded:

- implementation
- production storage design
- configuration format
- valuation implementation
- batch liquidation
- permissions behavior
- reservation semantics

## Authority Strategy

Ledger requires an Authority.

For Gate `2.2`, that means:

- ledger truth must come from an explicit authority participant
- ledger mutation must be represented as authority-owned Runtime work
- Core may authorize only from explicit ledger truth

The first proof authority should be:

- deterministic
- test-only
- bounded
- auditable

It should prove Coffer semantics only. It should not attempt to prove final
plugin integration, production persistence, or multi-plugin compatibility.

## Deferred Production Choices

Production ledger implementation is explicitly deferred.

This step does not require:

- a first-party production ledger
- Impactor specifically
- any specific third-party economy or wallet plugin
- any specific backing store

Future wrappers may target:

- Impactor
- another economy or ledger system
- a later first-party ledger

Those choices remain open until the bounded Gate `2.2` semantics are proven.

## Initial Ledger Value Semantics

Gate `2.2` should start with the following ledger semantics:

- ledger value is declarative, not selected
- players declare ledger amount through the concrete exchange they are asked to
  review and confirm
- shops declare ledger amounts through listing terms
- ledger value is authority-held balance truth
- ledger fulfillment surface is account, unit, and amount based
- amount is a positive integer in minor units
- floating decimal representation is excluded
- negative balances are excluded
- implicit exchange rates are excluded
- hidden mint or burn behavior is excluded
- reservation is deferred unless later pressure proves it necessary

Implications:

- the player does not “select” currency the way inventory is selected
- ledger amount must be explicit in the constructed exchange
- currency unit must be explicit in the constructed exchange
- any conversion between inventory value and ledger value must come from
  declared valuation participation, not from the ledger authority

## Initial Truth And Mutation Surfaces

The first bounded ledger authority should expose exactly these conceptual
surfaces:

- `canDebit(account, unit, amount)`
- `canCredit(account, unit, amount)`
- `debit(account, unit, amount)`
- `credit(account, unit, amount)`

These are semantic proof surfaces, not a final API commitment.

Their initial meaning is:

- `canDebit` proves explicit sufficient available balance truth
- `canCredit` proves explicit receivability truth
- `debit` performs Runtime-bound balance removal
- `credit` performs Runtime-bound balance addition

## Truth And Mutation Discipline

The bounded Gate `2.2` proof must preserve all of the following:

- debit requires explicit sufficient available balance truth
- credit requires explicit receivability truth
- mutation remains Runtime-bound
- Core authorizes only from explicit authority truth
- test-only ledger proves Coffer semantics, not final plugin integration

Implications:

- Runtime success must not be used to backfill missing Core truth
- debit success alone must not imply overall economic completion
- credit success alone must not imply overall economic completion
- if later pressure shows debit and credit need reservation to avoid dishonest
  race handling, reservation can be introduced later, but it should not be
  invented preemptively here

## Smallest Honest Next Proof

The smallest next proof after this document is:

- a test-first construction and refusal proof for a bounded Gate `2.2` listing
  shape that makes valuation participation and ledger participation explicit,
  using a deterministic test-only ledger authority identity rather than a
  production ledger backend

That is the next honest step because it proves:

- ledger is a real declared participant in the exchange
- valuation is a real declared participant in the exchange
- neither is being smuggled in through Runtime or adapter-local assumptions

## Verification

Verification for this journal step consists of:

- `git status --short`
- `git diff --check`

## Uncertainties

- whether the narrowest first Gate `2.2` slice should be player item to shop
  ledger, shop item to player ledger, or a symmetric shape supporting both
- whether future pressure will require reservation semantics earlier than
  expected
- whether the eventual production ledger wrapper will need additional bounded
  truth surfaces beyond debit and credit sufficiency and receivability
