## Summary

Add the smallest test-first Gate `2.1` proof for authority/Core truth behavior
 over infinite versus finite admin shop supply.

This step proves only:

- Core authorization approval or denial for the preset listing request shape
- finite versus infinite shop supply truth behavior
- player-side truth remaining required
- denial remaining a Core outcome rather than construction refusal

It does not add Runtime execution, mutation, receipt projection, valuation,
ledger behavior, permissions, batch liquidation, or final UX.

## Decision

Use a narrow Core-truth proof vessel that:

- starts from the already-proven preset listing request assembly shape
- reaches real `CofferCore.arbitrate(...)`
- uses a scripted shop-listing authority response keyed to the assembled
  required-truth surface
- keeps finite and infinite supply semantics distinct at Core

This is a proof vessel, not a production shop authority implementation.

## Rationale

The current repository does not yet contain a production shop-side actor
resolver or shop inventory authority surface analogous to player inventory
resolution.

That means the smallest honest next step is not to pretend the full production
carrier already exists. The smallest honest step is to prove that the current
request shape can reach Core and that Core distinguishes:

- valid finite supply
- stale or unavailable finite supply
- valid infinite supply
- player-side truth failure
- receivability failure

without entering Runtime or mutation.

## Finite Supply Truth Behavior Proven

The repository now proves:

- valid finite faucet supply can authorize when the shop offered value is
  attested available
- finite faucet supply denies when the shop offered value is stale or
  unavailable
- finite faucet denial remains a Core denial rather than a construction refusal
- shop/server inability to receive the player counter-offer denies at Core when
  that shop-side truth is modeled as required

## Infinite Supply Truth Behavior Proven

The repository now proves:

- valid infinite faucet supply can authorize without finite inventory depletion
  semantics
- infinite faucet supply remains distinct from finite stock behavior
- infinite supply does not counterfeit a finite stock-container identity or
  finite depletion requirement at Core

## Core Outcomes Proven

The repository now proves:

- approval occurs when all required player and shop truths are explicitly
  attested non-conflictingly
- denial occurs when player removability fails
- denial occurs when player receivability fails
- denial occurs when finite shop supply is unavailable
- denial occurs when modeled shop receivability fails
- no Runtime participation occurs in this proof
- no mutation occurs in this proof

## Scope

Included:

- preset listing request payload to Core arbitration
- scripted finite/infinite shop supply truth attestation
- approval and denial outcome proof

Excluded:

- Runtime participation
- mutation
- receipt projection
- valuation
- ledger behavior
- permissions
- batch liquidation
- final UX

## Remaining Gate `2.1` Gaps

Still unresolved for Gate `2.1`:

- a production or compatibility shop-side authority carrier beyond this proof
  vessel
- confirmation of the concrete listing-instantiated exchange
- Runtime participation and mutation behavior
- accountability and receipt attachment for shop exchange outcomes

## Smallest Next Proof

The smallest next proof after this step is:

- Runtime-participation gating for the preset listing request shape after Core
  approval, while keeping infinite and finite supply semantics distinct and
  honest

That is the next step because Core truth is now proven for the current request
shape, but the shop exchange path still stops before Runtime.

## Verification

Verify with:

- `git status --short`
- `git diff --check`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

## Uncertainties

- whether infinite faucet mode will ultimately want a dedicated production
  authority collaborator rather than a shop-actor compatibility layer
- whether finite faucet truth should eventually reuse inventory-binding
  container semantics directly or stay on a separate shop carrier
