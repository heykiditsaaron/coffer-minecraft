# Inventory Binding Proof Gate Audit

## Summary

Audited the current Minecraft inventory binding against the first real platform
binding proof gates and added narrow test coverage for explicit
`CONTAINER_UNAVAILABLE` classification paths.

## Decision

The next smallest survivable step is to strengthen failure-classification proof
at the binding boundary, not to broaden behavior.

This step proves that unresolved Minecraft inventory containers are classified
as unknown/unavailable across removal, receivability, simulation, and mutation
application.

## Rationale

Current tests already prove substantial descriptor, container, runtime
material, mutation, and drift behavior, including one partial-side-effect edge.
The most immediate survivability gap in the implemented source was explicit
proof for the unavailable-container classification path that already exists in
the binding.

That gap can be closed with test-only work, without introducing gameplay,
adapter behavior, or new binding semantics.

## Scope

Included:

- auditing current gate coverage from source, tests, and binding docs
- adding test-only proof for `minecraft.container.unavailable` classification

Excluded:

- gameplay behavior
- commands or player-facing UX
- new Fabric features
- live mutation readiness claims
- new timeout, disconnect, or adapter orchestration behavior
- feedback/accountability projection work

## Verification

Current gate read after this step:

- Proven: descriptor and equivalence
- Proven: container boundary parsing for supported actor shape
- Proven: runtime material and value-set reconstruction for current descriptor
  path
- Proven: mutation mechanics for simulated and applied atomic swap behavior
- Proven: state drift causing non-success outcomes
- Proven in part: failure classification for unavailable container, invalid
  runtime material, denial/failure reasons, and one partial-side-effect path

## Uncertainties

Not yet proven:

- timeout-specific classification
- disconnect behavior beyond unresolved-container classification
- exception classification beyond fail-safe local returns
- adapter-harness acceptance with real adapter collaborators
- feedback/accountability projection
- explicit live mutation gates

These must not be assumed from the current proof surface.
