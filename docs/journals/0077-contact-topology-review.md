# 0077 Contact Topology Review

## Summary

Record the current SER/CER contact topology after the mutation-seam deferral.
This is a boundary map, not a schema expansion.

## Decision

Keep the proven contact layers as they are:

- lifecycle `SER`
- construction-refusal `SER`
- Core-denial `CER`
- Core-approval `CER`
- Runtime-unknown `CER`

Keep the current stable envelope unchanged:

- `timestamp`
- `interactionId`
- `recordType`
- `stage`
- optional earned detail

## Rationale

The current earned content has a narrow and lawful shape:

- `code`
- `seam:"fabric_core"`
- `seam:"fabric_runtime"`

Each of those appears only where its boundary participation has been proven.
Nothing in the current topology warrants broadening into mutation, execution,
player, inventory, gameplay, session, command, or UX content.

The mutation seam remains deferred because the previously considered path was
too coupled to bootstrapped item and registry behavior to serve as a safe
boundary proof without drifting into live inventory or gameplay semantics.

A prerequisite seam or vessel may still be needed before mutation contact can
be proven truthfully. The likely requirement is a stable mutation-boundary
carrier that can participate without implying real exchange, player flow, or
successful mutation.

## Scope

Included:

- the current proven contact topology
- the currently earned content
- the intentionally omitted content
- the reason mutation contact remains deferred
- the likely need for a prerequisite vessel before mutation proof

Excluded:

- schema changes
- code changes
- tests
- gameplay
- commands
- player-facing UX
- inventory mutation
- mutation execution claims
- production readiness claims
- nested runtime or mutation objects

## Verification

No code path changed for this review. The repository remains governed by the
existing envelope and seam tests.

## Uncertainties

The exact prerequisite vessel for mutation contact is not yet proven. The
boundary may require a dedicated inert mutation carrier or another stable
contact layer before any mutation-boundary seam can be earned.
