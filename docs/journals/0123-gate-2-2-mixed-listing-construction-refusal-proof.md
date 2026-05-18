# 0123 Gate 2.2 Mixed Listing Construction Refusal Proof

## Summary

Add the smallest test-first Gate `2.2` proof for mixed inventory-plus-ledger
admin shop listings in `coffer-minecraft`.

This step proves only:
- mixed listing construction readiness
- explicit ledger participation preservation
- explicit optional listing-local valuation participation preservation
- honest refusal before any Core-facing assembly when the mixed listing is
  missing, disabled, malformed, or mismatched

It does not prove Core submission, Runtime participation, ledger mutation,
inventory mutation, receipt projection, valuation execution, or Gate `2.2`
completion.

## Decision

Introduce a new narrow Fabric-side mixed listing construction helper rather
than widening the existing item-for-item preset listing helper.

The mixed helper:
- accepts a server-authored listing with one explicit inventory term and one
  explicit ledger term
- supports both directions needed for the bounded proof:
  - inventory offered, ledger accepted
  - ledger offered, inventory accepted
- preserves optional listing-local valuation participation when present
- refuses before Core submission when explicit ledger participation is missing
  or malformed

## Rationale

Gate `2.2` needs proof that ledger participation becomes part of the
constructed Minecraft exchange surface rather than remaining hidden shop
metadata or future Runtime behavior.

The smallest honest first proof is not Core truth or Runtime mutation. It is
showing that a Minecraft admin shop listing can instantiate a bounded mixed
inventory-plus-ledger exchange shape with:
- explicit inventory identity
- explicit ledger authority/account/unit/amount identity
- explicit refusal when that declared participation is absent or malformed

Keeping this proof at construction/refusal level prevents counterfeit progress.
It shows that Gate `2.2` has a lawful authored shape in Minecraft without
claiming that the exchange can already be authorized or executed.

## Behavior Proven

The repository now proves:
- listing with explicit inventory offered value and explicit ledger accepted
  value can construct readiness
- listing with explicit ledger offered value and explicit inventory accepted
  value can construct readiness
- missing ledger participation refuses honestly before Core submission
- missing ledger authority identity refuses honestly before Core submission
- invalid ledger amount refuses honestly before Core submission
- unsupported ledger unit refuses honestly before Core submission
- missing listing refuses honestly before Core submission
- disabled listing refuses honestly before Core submission
- player selected inventory mismatch refuses honestly when inventory is the
  player counter-offer
- construction preserves explicit inventory value identity
- construction preserves explicit ledger authority, ledger, account, unit, and
  amount identity
- construction preserves optional listing-local valuation participation when
  present
- construction does not introduce implicit exchange-rate behavior

## Explicit Ledger Participation Preservation

The mixed construction surface now carries ledger participation explicitly as:
- authority identity
- ledger identity
- account identity
- unit identity
- positive integer amount

This is preserved as declared listing participation only.

No debit truth, credit truth, balance sufficiency, Runtime mutation, or
production ledger behavior is implied by construction readiness.

## Scope

Included:
- mixed listing construction helper
- mixed listing construction/refusal tests
- optional listing-local valuation participation preservation
- pre-Core refusal codes for missing or malformed ledger participation

Excluded:
- Core request assembly for mixed listings
- authority truth evaluation for ledger participation
- Runtime execution
- ledger mutation
- inventory mutation
- receipt projection
- valuation engine behavior
- config loading
- permissions
- batch liquidation

## Remaining Gate 2.2 Gaps

Still unresolved after this step:
- lawful Core-facing assembly for mixed listings
- explicit ledger authority truth participation in Minecraft Gate `2.2`
  requests
- valuation participation handoff beyond preserved listing-local identity
- Core approval and denial behavior for mixed inventory-plus-ledger listings
- Runtime execution and no-partial-mutation proof across mixed inventory and
  ledger surfaces
- interruption, unknown, and receipt/accountability behavior for mixed
  settlement

## Smallest Next Proof

The smallest honest next proof after this step is:

- a test-first mixed listing request-assembly proof that carries the proven
  mixed construction shape into explicit Core-facing declared participation and
  truth requirements without yet claiming Runtime mutation

That is the next step because the mixed listing can now be constructed
honestly, but it still has not been shown to enter lawful Core participation
with explicit ledger and valuation surfaces intact.

## Verification

Verify with:
- `git status --short`
- `git diff --check`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

## Uncertainties

- whether mixed listing request assembly should first cover one direction only
  before keeping the symmetric construction support
- whether listing-local valuation participation will remain an optional carrier
  identity or need a sharper declared surface during Core-facing assembly
- whether later Gate `2.2` pressure will require a more explicit shop-side
  ledger account identity split for treasury versus participant accounts
