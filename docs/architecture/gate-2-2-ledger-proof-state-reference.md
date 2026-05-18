# Gate 2.2 Ledger Proof State Reference

## Mirror Note

This document is substrate-derived and mirrored here for repo-local reasoning
continuity.

It summarizes the current bounded substrate proof state for Gate `2.2` ledger
participation as established in `~/dev/coffer`.

This mirror is not independently canonical.

It is intended to support Minecraft/Fabric adaptation reasoning, not to
redefine ledger semantics or imply production ledger readiness.

Canonical source context currently lives in:

- `~/dev/coffer/docs/journals/0078-test-only-ledger-authority-proof-contract.md`
- `~/dev/coffer/docs/journals/0079-ledger-participation-construction-refusal-proof.md`
- `~/dev/coffer/docs/journals/0080-ledger-authority-truth-proof.md`
- `~/dev/coffer/docs/journals/0081-ledger-runtime-mutation-proof.md`
- `~/dev/coffer/docs/journals/0082-ledger-candidate-emission-runtime-handoff-proof.md`

## Summary

The current substrate proof state for Gate `2.2` is a bounded, test-only
ledger authority proof.

What is proven is not a production ledger.
What is proven is the substrate seam that ledger participation can be:

- declared explicitly in exchange construction
- evaluated through explicit debit and credit truth
- emitted as an authority-owned mutation candidate
- authorized by Core without Core inventing or rewriting the mutation
- executed by Runtime only after authorization

## Ledger Value Shape

The proven ledger value shape is declarative ledger participation with explicit:

- `ledgerId`
- `accountId`
- `unitId`
- `amount`

This value is account/unit/amount participation, not implicit adapter state and
not a production storage record.

## Numeric And Economic Constraints

The current substrate proof remains intentionally narrow:

- `amount` is positive integer minor units only
- floating decimal representation is excluded
- negative value declaration is excluded
- implicit exchange rates are excluded
- implicit valuation is excluded
- hidden mint behavior is excluded
- hidden burn behavior is excluded

These constraints matter because Gate `2.2` pressure is about lawful explicit
ledger participation, not fuzzy economic interpretation.

## Explicit Truth Surfaces

The substrate proof now includes explicit ledger authority truth for:

- `canDebit(account, unit, amount)`
- `canCredit(account, unit, amount)`

The proven behavior is:

- sufficient balance can attest debit truth
- insufficient balance denies honestly
- unsupported unit denies honestly
- unavailable or refused ledger truth remains explicit
- unknown ledger truth remains distinct from ordinary denial
- credit receivability is explicit and required

This means Core-facing approval does not come from Runtime side effects or
adapter-local assumptions.
It comes from explicit authority truth only.

## Authority-Owned Candidate Mutation Emission

The substrate proof now also includes authority-owned mutation authorship.

When explicit `canDebit` plus `canCredit` truth succeeds, the test-only ledger
authority can emit one explicit debit-plus-credit mutation candidate for the
bounded ledger transfer claim.

That candidate remains:

- test-only
- deterministic
- reconstructable from explicit ledger/account/unit/amount fields
- owned by the ledger authority rather than invented by Core

If truth does not hold, no mutation candidate is emitted.

That includes:

- insufficient balance
- unsupported unit
- unavailable or refused truth
- unknown truth

## Core Authorization Boundary

The substrate proof now demonstrates that Core authorizes only
Authority-provided ledger mutation candidates.

Core does not:

- invent ledger mutations
- modify ledger mutation fields
- complete missing ledger mutation meaning
- reinterpret the mutation into a different ledger operation

If the authority does not provide a lawful candidate, Core approval does not
produce an authorized ledger mutation.

## Runtime Handoff Boundary

The substrate proof now demonstrates that Runtime executes only authorized
candidates.

The bounded Runtime proof is:

- deterministic
- in-memory
- atomic for the bounded debit-plus-credit claim
- explicit about success, failure, and unknown execution
- explicit about no-partial-mutation for the bounded ledger-only proof

No ledger mutation occurs before authorization.

Runtime does not backfill missing truth.
Runtime executes the authorized mutation shape it is given.

## What Remains Deferred

The substrate proof is still intentionally not a production ledger
implementation.

The following remain deferred:

- production ledger implementation
- production persistence
- external ledger wrappers
- reservation semantics
- valuation logic
- mixed inventory plus ledger settlement

This mirror should therefore be used as adaptation guidance for Gate `2.2`
reasoning, not as grounds to claim production ledger readiness inside
`coffer-minecraft`.
