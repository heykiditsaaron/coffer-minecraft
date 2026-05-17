## Summary

Audit the smallest safe compatibility bridge for the deferred live mutation seam
from `0105`.

Compare:

- a Fabric-safe bootstrapped player inventory mutation harness
- a shared adapter seam that lets the Fabric submission chain reuse the already-safe
  bindings mutation vessel

## Decision

Recommend the shared adapter seam.

Do not prioritize a Fabric-native bootstrapped mutation harness as the next step.

## Rationale

### Option 1: Fabric-Safe Bootstrapped Player Inventory Mutation Harness

This path is less safe right now.

The failed `0105` attempt already showed that the current `platforms/fabric` test
geometry is not a stable place to prove real `ItemStack`-backed mutation:

- explicit bootstrap triggered `IllegalAccessError`
- omission of bootstrap left Minecraft item initialization unbootstrapped

Trying to force this path next would introduce extra assumptions unrelated to the
lawful exchange geometry itself:

- assumptions about Fabric test bootstrap order
- assumptions about Minecraft registry/module compatibility in the test runner
- assumptions that a passing harness would reflect exchange-law correctness rather
  than environment-specific initialization luck

That makes this path more likely to create fake live-mutation confidence.

### Option 2: Shared Adapter Seam Reusing the Safe Bindings Mutation Vessel

This path is safer and introduces fewer assumptions.

The repository already has two proven halves:

- the Fabric-side selected submission chain for authorship, confirmation,
  assembly, Core gating, and accountability projection
- the bindings-side selected exchange Runtime vessel for truthful real inventory
  mutation success, failure, and unknown

The smallest safe bridge is therefore not a new bootstrap-heavy Fabric harness.
It is a shared seam that allows the Fabric submission chain to hand its approved
payload or mutation plan into the already-safe bindings mutation vessel under
controlled test support.

This preserves boundaries better:

- Fabric still owns authorship, confirmation, submission orchestration, and
  accountability projection
- bindings still own inventory truth, runtime descriptor interpretation, and real
  inventory mutation behavior
- Core and Runtime semantics remain unchanged

## Recommendation

Choose the shared adapter seam.

This path:

- is safest
- introduces fewer non-exchange assumptions
- best preserves Core/Runtime/Authority boundaries
- best avoids fake live mutation confidence caused by unstable Fabric bootstrap

## Rejected Bridge

Rejected as the immediate next proof target:

- Fabric-native bootstrapped player inventory mutation harness

Reason:

- the environmental failure mode is already stronger than the exchange-law signal
- next effort there would mostly debug test bootstrap compatibility rather than
  prove the lawful mutation seam

## Risks

Risks of the recommended shared seam:

- it may under-represent truly Fabric-specific inventory behavior if the seam is
  made too abstract
- it must not quietly bypass the selected submission chain and jump straight into
  the bindings mutation vessel
- it must preserve explicit handoff artifacts so the proof remains auditable

Those risks are smaller than the bootstrap-harness risks because they can be
controlled directly in test design.

## Smallest Next Proof

The first proof to attempt next should be:

- a test-support seam that accepts the Fabric-selected submission chain output
  after dual confirmation and Core approval
- then executes the approved mutation through the already-safe bindings Runtime
  mutation vessel
- while proving:
  - no mutation before Core approval
  - approved mutation enters real Runtime execution
  - success mutates inventory truthfully
  - failure/unknown remain honest
  - Fabric-side accountability projection still reflects the same outcome

This should remain a test-only bridge first, not production wiring.

## Verification

This step is documentation-only.

No code changes.

## Uncertainties

- the exact seam shape should be chosen to expose the minimum handoff artifact:
  likely approved payload or approved mutation plan plus controlled inventory-slot
  provider
- once that seam exists, a later Fabric-native mutation harness may still be worth
  pursuing for higher-fidelity platform proof, but only after the shared seam has
  already established the lawfully connected mutation path
