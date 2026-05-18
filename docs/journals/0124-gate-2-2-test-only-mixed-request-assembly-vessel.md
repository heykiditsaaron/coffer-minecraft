# 0124 Gate 2.2 Test-Only Mixed Request Assembly Vessel

## Summary

Recover honestly from the failed mixed ledger request-assembly attempt by
moving the mixed inventory-plus-ledger Core-facing proof into a test-only
Fabric vessel.

This step proves:
- test-only mixed request assembly shape
- explicit mixed inventory-plus-ledger truth participation in one payload
- pre-Core refusal carry-through from mixed construction
- Core approval, denial, and refusal behavior for the mixed truth shape

It does not establish a production ledger dependency for `platforms:fabric`.

## Boundary Discovered

The previous attempt tried to place mixed ledger request assembly in
`platforms:fabric` main code while importing substrate test-only ledger proof
classes.

That failed because the Fabric main compile classpath does not include those
substrate ledger proof classes.

This is a real boundary:
- the substrate ledger proof exists
- the Fabric main compile surface does not currently depend on that proof
- forcing that dependency now would counterfeit production integration

## Decision

Reject a new production/main Fabric dependency on substrate test-only ledger
proof classes.

Use a test-only mixed request assembly vessel under
`platforms/fabric/src/test/java` instead.

The test-only vessel:
- keeps ledger proof shape local to the test layer
- preserves explicit ledger identity
- preserves explicit inventory identity
- preserves listing-local valuation identity as declared context only
- proves one Core-facing payload can carry both inventory and ledger truth
  requirements
- avoids claiming any production ledger contract is already available to Fabric
  main code

## Rationale

The goal of this Gate `2.2` step is proof, not production coupling.

If the repository had broadened Fabric main dependencies just to make the
proof compile, it would have blurred an important distinction:
- a lawful test vessel proving mixed payload semantics
- a real production integration surface for Minecraft ledger participation

That distinction matters because no non-test ledger contract artifact has been
earned yet for Fabric main code in this repository.

The honest move is therefore:
- keep mixed ledger request assembly test-local
- prove the Core-facing semantics there
- leave production dependency and production integration decisions deferred

## Test-Only Proof Shape Chosen

The chosen vessel is:
- one test-local mixed listing request assembly helper
- one test-local mixed ledger truth authority
- one test-local no-op mutation seam used only to satisfy approval-path
  arbitration coverage

The assembly helper:
- wraps the already-proven mixed construction/refusal seam
- carries inventory and ledger values into one `ExchangePayload`
- emits one inventory authority requirement and one ledger authority
  requirement
- emits no inventory or ledger mutation requirement
- keeps valuation identity only on declared listing/shop context

The Core truth vessel:
- proves approval when both inventory and ledger truth succeed
- proves denial when inventory truth fails
- proves denial when ledger truth fails
- proves authority refusal when ledger truth is unavailable or unknown
- proves no Runtime participation occurs
- proves no inventory mutation or ledger mutation occurs

## Behavior Proven

This repository now proves:
- inventory-offered plus ledger-accepted listings can assemble a Core-facing
  mixed payload in a test-only vessel
- ledger-offered plus inventory-accepted listings can assemble a Core-facing
  mixed payload in a test-only vessel
- missing, disabled, and mismatched mixed construction outcomes remain pre-Core
  refusal
- explicit inventory truth and explicit ledger truth can coexist in one
  payload without semantic conflict
- Core approval requires both explicit inventory truth and explicit ledger
  truth
- inventory truth denial blocks approval
- ledger truth denial blocks approval
- unavailable or unknown ledger truth blocks approval as authority refusal
  rather than counterfeit success or ordinary denial
- no Runtime participation occurs in this proof
- no inventory mutation occurs in this proof
- no ledger mutation occurs in this proof
- no receipt projection occurs in this proof
- no implicit valuation or exchange-rate logic is introduced
- listing-local valuation identity remains reconstructable only as declared
  shop/listing context

## Production Dependency Rejected

The repository does not now:
- add substrate test-only ledger proof classes to `platforms:fabric` main
  compile dependencies
- import substrate test-only ledger proof classes into Fabric production code
- establish a production ledger dependency for Gate `2.2`

That rejection is intentional and part of the proof.

## Scope

Included:
- test-only mixed request assembly helper
- test-only mixed assembly tests
- test-only mixed Core truth tests
- journaled recovery from the compile boundary failure

Excluded:
- production/main Fabric ledger dependency
- Runtime execution
- ledger mutation
- inventory mutation
- production ledger integration
- valuation engine behavior
- config loading
- receipts
- permissions
- batch liquidation

## Remaining Production Gate 2.2 Gaps

Still unresolved after this recovery step:
- a non-test production ledger contract artifact appropriate for Fabric main
  code
- lawful production mixed request assembly outside the test vessel
- production ledger truth authority integration for Minecraft
- production mixed inventory-plus-ledger Runtime execution
- no-partial-mutation proof for production mixed settlement
- production interruption, unknown, and receipt behavior for mixed settlement

## Smallest Next Proof

The smallest honest next proof after this step is:

- define or adopt a non-test ledger contract surface that Fabric main code may
  lawfully depend on, or else continue subsequent Gate `2.2` mixed behavior
  proof in test-only vessels without widening production dependencies

That decision must be explicit before any production/main mixed ledger assembly
is attempted again.

## Verification

Verify with:
- `git status --short`
- `git diff --check`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

## Uncertainties

- whether a later non-test ledger contract should come from substrate
  publication work or a Minecraft-local integration boundary
- whether mixed truth proof should remain debit-only until a production credit
  account surface is earned
- whether later production request assembly should preserve the same test-vessel
  actor/context shape or use a sharper published contract
