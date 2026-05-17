## Summary

Add the smallest test-first construction/refusal proof for Gate `2.1` preset
admin shop listings.

This step proves only:

- server/admin-authored preset listing construction surface
- player selected counter-offer validation
- explicit infinite versus finite listing mode identity
- readiness or refusal before Core submission

It does not implement full shop exchange, Runtime participation, mutation,
valuation, ledger behavior, permissions, batch liquidation, or final UX.

## Decision

Use a narrow Fabric-side preset listing construction helper that:

- accepts an optional server/admin-authored listing
- validates the player's selected inventory value against the listing's accepted
  counter-offer
- refuses missing, disabled, or mismatched listings before Core contact
- returns a concrete exchange-readiness surface only when the selected player
  value satisfies the preset listing
- represents infinite and finite supply modes explicitly and reconstructably at
  construction time

## Rationale

Gate `2.1` adds the first real shop-authored surface, but the smallest lawful
first proof is not Runtime or mutation. It is whether a preset listing can be
treated as a bounded authored standing offer template without:

- pretending listing presence is player consent
- pretending finite and infinite supply mean the same thing
- entering Core or Runtime before a concrete instantiated exchange exists

This proof therefore stays narrowly on construction readiness and refusal.

## Behavior Proven

The repository now proves:

- missing listing refuses honestly before Core submission
- disabled or unavailable listing refuses honestly before Core submission
- player selected value mismatch refuses honestly before Core submission
- valid player selected value permits concrete exchange construction readiness
- infinite faucet listing identity is explicit and reconstructable
- finite faucet listing identity is explicit and reconstructable
- no mutation occurs during listing construction or refusal
- no Runtime participation occurs during listing construction or refusal
- infinite faucet readiness does not imply finite shop inventory depletion

## Infinite Versus Finite Semantics

Infinite faucet mode is represented as explicit listing-surface supply identity:

- it identifies a standing infinite listing surface
- it does not imply a finite stock container

Finite faucet mode is represented as explicit finite supply identity:

- it identifies a backing finite supply container or stock surface
- it remains distinguishable from infinite mode before any later authority or
  mutation proof is attempted

## Scope

Included:

- preset listing object/surface
- explicit supply-mode representation
- player selected counter-offer validation
- readiness/refusal output only

Excluded:

- Core submission
- Runtime participation
- mutation
- valuation
- ledger behavior
- permissions
- batch liquidation
- final UI/UX

## Remaining Gate `2.1` Gaps

Still unresolved for Gate `2.1`:

- lawful Core-facing assembly from player-selected value plus concrete listing
- finite faucet stale supply denial at authority/Core truth
- infinite faucet lawful offered-value truth at authority/Core truth
- confirmation flow for the concrete listing-instantiated exchange
- Runtime participation and mutation behavior
- accountability and receipt attachment for shop exchange outcomes

## Smallest Next Proof

The smallest next proof after this step is:

- lawful Core-facing assembly and refusal for the concrete listing-instantiated
  exchange, including explicit missing or stale finite shop supply refusal

That is the next step because the construction surface is now explicit, but the
listing has not yet been shown to enter authority/Core lawfully.

## Verification

Verify with:

- `git status --short`
- `git diff --check`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

## Uncertainties

- whether infinite faucet mode will eventually need its own bounded authority
  carrier distinct from finite shop inventory surfaces
- whether finite faucet listing truth will fit the current inventory binding
  surfaces directly or need a narrow shop-side compatibility layer
