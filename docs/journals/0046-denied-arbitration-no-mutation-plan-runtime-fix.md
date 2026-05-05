# Denied Arbitration No Mutation Plan Runtime Fix

## Summary

Clarify Fabric arbitration completion logging so denied arbitration results are handled without inspecting mutation contents.

## Decision

Fabric logs arbitration completion with request ID, outcome decision, and mutation plan presence only. Denied arbitration still returns `Denied` before runtime execution. Mutation contents are read only after approval and a non-null mutation plan check.

## Rationale

Core denied arbitration results may not include mutation plans. Fabric must not read mutation mutations until the arbitration decision is approved and the plan is known to exist.

## Scope

Included:
- Fabric exchange arbitration logging.
- Documentation of the denied arbitration mutation plan guard.

Excluded:
- Core schema changes.
- Adapter changes.
- Runtime behavior changes.

## Verification

- `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava` passed.
- `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:build` passed.

## Uncertainties

None.
