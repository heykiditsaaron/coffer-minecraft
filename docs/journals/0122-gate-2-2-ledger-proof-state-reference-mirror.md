# 0122 Gate 2.2 Ledger Proof State Reference Mirror

## Summary

Add a substrate-derived Gate `2.2` ledger proof-state mirror into
`coffer-minecraft` for repo-local reasoning continuity.

This step does not implement code, redefine ledger semantics, add valuation, or
add Minecraft integration behavior.

## Decision

Mirror the current substrate ledger proof status into `docs/architecture` as a
local reference note and keep that note explicitly non-canonical.

Source docs read:

- `~/dev/coffer/docs/journals/0078-test-only-ledger-authority-proof-contract.md`
- `~/dev/coffer/docs/journals/0079-ledger-participation-construction-refusal-proof.md`
- `~/dev/coffer/docs/journals/0080-ledger-authority-truth-proof.md`
- `~/dev/coffer/docs/journals/0081-ledger-runtime-mutation-proof.md`
- `~/dev/coffer/docs/journals/0082-ledger-candidate-emission-runtime-handoff-proof.md`

Target path:

- `~/dev/coffer-minecraft/docs/architecture/gate-2-2-ledger-proof-state-reference.md`

## Rationale

`coffer-minecraft` now has direct Gate `2.2` pressure through admin-shop
valuation and ledger planning journals.

That pressure benefits from a repo-local summary of what the substrate has
actually proven so Minecraft/Fabric reasoning does not drift into either of
these errors:

- assuming production ledger readiness when only test-only substrate proof
  exists
- assuming ledger mutation authorship or Runtime handoff remains unresolved
  after substrate has already proven it

The mirror keeps current repo-local reasoning continuous even if future agents
or collaborators do not have reliable cross-repo access.

## Scope

Included:

- one architecture/reference mirror summarizing current substrate ledger proof
  status
- explicit non-canonical mirror labeling
- explicit source and target path recording
- local explanation of why the mirror supports Gate `2.2`

Excluded:

- code changes
- ledger semantic redefinition
- valuation work
- Minecraft integration work
- production ledger claims

## How This Supports Gate 2.2

This mirror supports Gate `2.2` in `coffer-minecraft` by preserving the current
substrate answers to the questions local planning actually depends on:

- ledger value is declarative account/unit/amount participation
- integer minor-unit discipline is already part of the substrate proof
- explicit debit and credit truth is already proven
- authority-owned candidate mutation emission is already proven
- Core authorization is already bounded to authority-provided candidates
- Runtime execution is already bounded to authorized candidates
- production ledger, wrappers, persistence, reservation, and valuation remain
  deferred

That allows Minecraft/Fabric-side reasoning to stay honest about what is
already available as substrate proof and what still remains outside the proven
surface.

## Verification

Verification for this step consists of:

- `git status --short`
- `git diff --check`

in both `~/dev/coffer` and `~/dev/coffer-minecraft`.

## Uncertainties

This mirror resolves local continuity, not future Minecraft adaptation detail.

Still deferred here:

- how Gate `2.2` ledger proof should later attach to Minecraft admin-shop
  listing construction
- how valuation surfaces should eventually join the ledger proof
- how substrate execution/result evidence should later be mirrored locally once
  that next proof exists
