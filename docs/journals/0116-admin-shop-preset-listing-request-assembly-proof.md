## Summary

Add the smallest test-first Gate `2.1` proof for Core-facing request assembly
from a valid preset admin shop listing.

This step proves only:

- lawful ExchangeRequest assembly from listing-construction readiness
- player selected value identity preservation
- shop offered value identity preservation
- explicit infinite versus finite supply-mode identity after assembly
- pre-Core refusal for missing, disabled, or mismatched listings

It does not add Runtime execution, mutation, receipt projection, valuation,
ledger behavior, permissions, batch liquidation, or final UX.

## Decision

Use a narrow Fabric-side assembly helper that:

- wraps the already-proven preset listing construction/refusal surface
- assembles a bounded atomic-swap payload only after listing readiness exists
- models shop-side participation as an explicit admin shop listing actor
- carries supply-mode identity as shop actor metadata
- leaves missing, disabled, and mismatched listings as pre-Core refusal

## Rationale

Gate `2.1` now has a lawful construction surface, but that is still only
prepared shop intent. The next honest step is not Runtime or mutation. It is
whether that prepared listing plus selected player counter-offer can become a
lawful Core-facing request shape without counterfeiting:

- finite stock depletion
- guaranteed availability
- Runtime or mutation participation

This proof therefore stays narrowly on request assembly.

## Behavior Proven

The repository now proves:

- valid infinite faucet listing permits lawful ExchangeRequest assembly
- valid finite faucet listing permits lawful ExchangeRequest assembly
- player selected transferable-value identity is preserved into assembly
- shop offered transferable-value identity is preserved into assembly
- infinite versus finite supply mode identity remains reconstructable after
  assembly
- missing, disabled, and mismatched listings remain pre-Core refusal
- no mutation occurs during assembly
- no Runtime participation occurs during assembly
- infinite faucet assembly does not counterfeit finite depletion semantics
- finite faucet assembly does not counterfeit guaranteed availability

## Infinite Versus Finite Assembly Semantics

Infinite faucet assembly carries:

- explicit listing identity
- explicit `infinite_faucet` supply mode
- listing-surface identity

It does not carry:

- finite supply-container identity
- implied depletion state

Finite faucet assembly carries:

- explicit listing identity
- explicit `finite_faucet` supply mode
- finite supply-container identity

It does not carry:

- guaranteed availability
- infinite faucet identity

## Scope

Included:

- concrete listing construction readiness to ExchangeRequest assembly
- bounded shop-side authored offer surface
- shop actor metadata for supply-mode reconstruction
- refusal output before Core when readiness is absent

Excluded:

- Core arbitration
- Runtime participation
- mutation
- valuation
- ledger behavior
- permissions
- batch liquidation
- receipt projection

## Remaining Gate `2.1` Gaps

Still unresolved for Gate `2.1`:

- lawful authority/Core truth for finite supply staleness or unavailability
- lawful authority/Core truth for infinite faucet offered-value participation
- player confirmation of the concrete listing-instantiated exchange
- Runtime participation and mutation behavior
- accountability and receipt attachment for shop exchange outcomes

## Smallest Next Proof

The smallest next proof after this step is:

- Core authorization denial and approval behavior for the preset listing
  request shape, especially:
  - finite supply unavailable or stale
  - player cannot receive
  - player value stale after listing selection
  - infinite faucet shop-side truth without fake depletion

That is the next honest step because the listing now assembles lawfully, but
its shop-side truth has not yet been attested or arbitrated.

## Verification

Verify with:

- `git status --short`
- `git diff --check`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

## Uncertainties

- whether infinite faucet mode will require a distinct authority carrier rather
  than a finite-container-like interpretation
- whether finite faucet truth can remain on the current inventory binding
  surfaces or needs a narrow shop-specific compatibility layer
