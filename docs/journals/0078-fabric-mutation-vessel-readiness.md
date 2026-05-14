# 0078 Fabric Mutation Vessel Readiness

## Summary

Design the smallest inert mutation vessel that could later support lawful
mutation-boundary contact without gameplay, commands, UX, or real player
inventory.

This is a readiness note, not an implementation plan and not a schema change.

## Decision

Treat the inert mutation vessel as a future boundary carrier whose job would be
to prove contact, not success.

If introduced later, it should prove only that:

- a mutation-boundary contact occurred
- the contact was inert and non-player-backed
- the contact remained distinct from Core approval and Runtime contact
- no inventory exchange, gameplay flow, or execution claim was implied

## Rationale

The current topology already proves:

- lifecycle `SER`
- construction-refusal `SER`
- Core-denial `CER`
- Core-approval `CER`
- Runtime-unknown `CER`

The mutation seam was deferred because the available path depended on
bootstrapped item and registry behavior that is too coupled to stable mutation
proof in this repository.

An inert mutation vessel would need to avoid:

- real player inventory mutation
- gameplay/session/command/UX behavior
- successful mutation claims
- nested mutation objects
- broad mutation schemas
- envelope changes
- any suggestion that mutation readiness is production readiness

## Scope

Included:

- what the vessel would need to prove
- what it must avoid
- where it should likely live
- which existing harnesses can inform it
- what lawful mutation-boundary contact would mean
- what would still not be claimed
- the smallest next implementable step, if one exists

Excluded:

- code changes
- tests
- gameplay
- commands
- UX
- schema fields
- inventory mutation
- runtime expansion
- claims of production readiness

## Proposed Vessel Shape

The smallest plausible vessel is a dedicated Fabric-side inert contact probe or
test harness colocated with the existing startup/contact probes in
`platforms/fabric`.

It should be able to sit after Core and Runtime contact if needed, but it
should not depend on live player state or real inventory exchange.

The current Fabric contact probes and tests that can inform it are:

- `CofferMinecraftFabricCoreContactProbe`
- `CofferMinecraftFabricRuntimeContactProbe`
- `CofferMinecraftFabricApprovedCoreContactProbeTest`
- `CofferMinecraftFabricRuntimeContactProbeTest`
- `CofferMinecraftLifecycleAccountabilityTest`

Those harnesses already demonstrate the envelope shape, flat JSONL ordering,
and seam selectivity at earlier contact depths.

## Lawful Contact

Lawful mutation-boundary contact would mean an inert carrier actually
participated at the mutation boundary and produced only minimal earned contact
content, such as a seam marker if and only if that boundary was truly reached.

It would still not mean:

- a mutation succeeded
- a player existed
- inventory changed
- gameplay occurred
- a production mutation flow was ready

## Smallest Next Step

If any next step is taken, it should be a test-first definition of an inert
mutation vessel contract, colocated with the existing Fabric contact probes,
that can only report contact/no-contact and cannot simulate exchange success.

If the repository cannot support that without reintroducing the same bootstraps
that deferred mutation in the first place, the correct next step remains
deferral rather than invention.

## Verification

No implementation changed for this note.

## Uncertainties

The repository does not yet prove what the prerequisite vessel must be. It may
need a dedicated carrier seam, a stable inert mutation token, or a separate
boundary adapter before mutation contact can be earned truthfully.
