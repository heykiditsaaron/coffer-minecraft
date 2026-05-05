# Arbitration Null Mutation Plan Handling

## Summary

Guard Fabric exchange execution against nullable Core arbitration mutation plans.

## Decision

Fabric now returns `Denied` immediately when Core arbitration denies an exchange. Runtime execution only starts after an approved arbitration result has a non-null mutation plan. An approved result without a mutation plan returns platform `Unavailable` with reason code `APPROVED_WITHOUT_MUTATION_PLAN`.

## Rationale

Denied Core arbitration results intentionally do not include mutation plans. Fabric must not read or execute a mutation plan until the arbitration decision is known.

## Scope

Included:
- Fabric exchange result handling after Core arbitration.
- Defensive unavailable handling for invariant drift.

Excluded:
- Core arbitration API changes.
- Runtime execution behavior changes.
- Adapter-facing API shape changes.

## Verification

`gradle :platforms:fabric:compileJava` passed.

## Uncertainties

None.
