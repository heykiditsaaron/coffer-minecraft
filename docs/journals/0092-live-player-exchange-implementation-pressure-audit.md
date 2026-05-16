# 0092 Gate #1 Live Player Exchange Implementation-Pressure Audit

## Summary

Audit the repo-local components already present across `~/dev/coffer` and
`~/dev/coffer-minecraft` for Gate `#1`:

- player-to-player
- inventory-selected transferable value exchange

This is an implementation-pressure audit only.

It does not claim that live exchange already works.
It does not introduce code, schema, Core law, or new operational APIs.

## Decision

Treat Gate `#1` as partially paved but not yet bridged.

The current repos already contain:

- lawful Core arbitration
- Runtime execution coordination
- reusable TransferableValue authority construction/core/runtime pieces
- real Minecraft inventory binding semantics
- Fabric startup/Core/inert-Runtime accountability probes
- test-only ghost adapter accountability projection support

The missing work is not primarily inside Core law.
The missing work is the live operational bridge between:

- real selected player state
- explicit dual-party confirmation
- bounded stale/invalidation checks
- truthful Fabric submission
- truthful live mutation participation
- receipt/accountability attachment at participant and admin surfaces

The strongest current bridge risk is selection compatibility:
the binding can reason about inventory regions, but Gate `#1` asks for a live
selected hand or hotbar value, which is narrower than the currently proven
region semantics.

## Source Repos Inspected

`~/dev/coffer`

- `docs/reference/foundation/core_law_current_canon.md`
- `docs/journals/0071-transferable-value-interaction-lifecycle-topology.md`
- `docs/journals/0072-minimal-lawful-transaction-receipt-topology.md`
- `docs/journals/0073-transferable-value-lifecycle-to-receipt-mapping-pressure-test.md`
- `docs/journals/0057-runtime-execution-integrity-readiness-review.md`
- `docs/journals/0064-fake-inventory-mutation-mechanics-classification-proof.md`
- `coffer-core/src/main/java/...`
- `coffer-runtime/src/main/java/...`
- `first-party/authorities/transferable-value/src/main/java/...`
- `first-party/authorities/transferable-value/src/test/java/...`

`~/dev/coffer-minecraft`

- `docs/contracts/inventory-binding.md`
- `docs/journals/0061-minimal-ghost-ser-cer-projection-proof.md`
- `docs/journals/0074-fabric-runtime-seam-contact.md`
- `docs/journals/0078-fabric-mutation-vessel-readiness.md`
- `docs/journals/0080-fabric-mutation-contact-refusal.md`
- `docs/journals/0081-boundary-classification-review.md`
- `docs/journals/0085-runtime-mutationplan-semantics-audit.md`
- `docs/journals/0090-player-to-player-selected-inventory-exchange-proof-gate.md`
- `bindings/inventory/src/main/java/...`
- `bindings/inventory/src/test/java/...`
- `platforms/fabric/src/main/java/...`
- `platforms/fabric/src/test/java/...`
- `test-support/ghost-adapter/src/main/java/...`

## Reusable Components Found

### Core arbitration pieces

- `coffer-core/src/main/java/org/coffer/core/arbitration/CofferCore.java`
  already provides lawful approve or deny arbitration.
- `coffer-core/src/main/java/org/coffer/core/model/mutation/MutationPlan.java`
  and `AuthorizedMutation.java` already preserve the approved mutation path.
- `coffer-core/src/main/java/org/coffer/core/model/outcome/Outcome.java`
  already preserves denial vs approval without implying execution.

Safe reuse judgment:
These are ready for Gate `#1` as-is.
No audit evidence suggests Core law redesign is needed for the first proof
vessel.

### Runtime pieces

- `coffer-runtime/src/main/java/org/coffer/runtime/CofferRuntime.java`
- `coffer-runtime/src/main/java/org/coffer/runtime/execution/ExecutionCoordinator.java`
- `coffer-runtime/src/main/java/org/coffer/runtime/model/execution/ExecutionResult.java`
- `ExecutionStatus` and per-mutation statuses already preserve success,
  failure, partial, and unknown distinctions.

Safe reuse judgment:
These are reusable for a bounded one-mutation exchange path.
They already enforce "Runtime after approval" and preserve unknown honestly.

Constraint:
Runtime remains single-attempt and globally allows partial statuses.
Gate `#1` must therefore avoid multi-step exchange plans if it wants
no-partial-mutation interaction claims.

### TransferableValueAuthority pieces

- `TransferableValueExchangePayloadConstruction.constructAtomicSwap(...)`
  already constructs the paved atomic-swap payload.
- `TransferableValueCoreAuthority`
  already attests removability, receivability, and atomic-swap feasibility.
- `TransferableValueRuntimeAuthority`
  already reconstructs runtime material and executes the authorized mutation
  only from the authorized descriptor.
- `TransferableValueAuthorityFakeBindingEndToEndTest`
  already proves approve, deny, and runtime failure behavior in a bounded path.

Safe reuse judgment:
These are directly reusable for Gate `#1`.
They already express the right law and the right Core-to-Runtime separation.

### Minecraft inventory binding pieces

- `MinecraftDescriptorFactory`
- `MinecraftItemDescriptor`
- `MinecraftItemMatcher`
- `MinecraftContainerResolver`
- `MinecraftPlayerInventoryContainer`
- `MinecraftRuntimePayloadFactory`
- `MinecraftRuntimePayloadInterpreter`
- `MinecraftRuntimeValueSetResolver`

Existing proof:

- `MinecraftPlayerInventoryContainerTest`
- `MinecraftTransferableValueEndToEndTest`
- `MinecraftCofferCollaboratorsTest`

What is already proven:

- Minecraft item and exact NBT identity matching
- removability and receivability checks
- atomic-swap simulation and application
- Core denial on insufficient quantity
- Runtime success on a valid bounded swap
- Runtime failure after post-approval drift
- Runtime unknown on malformed runtime material or container disappearance

Safe reuse judgment:
These are the main reusable Minecraft semantics.

### Fabric startup/contact/accountability probes

- `CofferMinecraftFabricConstructionContactProbe`
- `CofferMinecraftFabricCoreContactProbe`
- `CofferMinecraftFabricApprovedCoreContactProbe`
- `CofferMinecraftFabricRuntimeContactProbe`
- `CofferMinecraftLifecycleAccountability`

Existing proof:

- `CofferMinecraftFabricConstructionContactProbeTest`
- `CofferMinecraftFabricCoreContactProbeTest`
- `CofferMinecraftFabricApprovedCoreContactProbeTest`
- `CofferMinecraftFabricRuntimeContactProbeTest`
- `CofferMinecraftFabricMutationContactCarrierTest`

What is already proven:

- startup lifecycle `SER`
- construction-refusal `SER`
- Core-denial `CER`
- Core-approval `CER`
- inert Runtime-unknown `CER`
- flat JSONL line shape with optional `seam` and `code`

Safe reuse judgment:
Useful as accountability precedent and seam proof.
Not yet sufficient as the live exchange accountability surface.

### SER/CER emission surfaces

- `CofferMinecraftLifecycleAccountability`
  emits append-only Fabric JSONL records.
- `GhostAdapterAccountabilityProjection`
  and `GhostAdapterAccountabilityJsonlEmitter`
  prove a second minimal accountability shape in test support.

Safe reuse judgment:
These already show minimal truthful stage projection and append-only emission.

### Test support and ghost adapter support

- `GhostAdapterExchangeHarness`
- `GhostAdapterAccountabilityProjection`
- `GhostAdapterAccountabilityJsonlEmitter`
- `GhostAdapterExchangeHarnessTest`
- `GhostAdapterAccountabilityProjectionTest`

What is already proven:

- construction refusal before Core
- Core denial with no Runtime invocation
- Runtime success
- Runtime failure
- Runtime unknown
- small JSONL-friendly accountability projection

Safe reuse judgment:
Useful as test support for future adapter-facing proof.
Not a live Fabric interaction surface.

## Existing But Not Yet Sufficient

### Inventory capture from live player state

Existing:

- `CofferMinecraftFabricService.resolvePlayerInventorySlots(...)`
  can resolve live player inventory regions on the server thread.
- `MinecraftPlayerInventoryContainer.Region`
  supports `MAIN`, `HOTBAR`, `ARMOR`, and `OFFHAND`.

Not yet sufficient:

- no repo-local live selected-slot capture component exists
- no participant-facing capture state exists
- no proof that "selected main hand" can be expressed without broadening from
  selected slot to whole region

### Participant confirmation surface

Existing:

- none beyond historical documents and test-only harnesses

Not yet sufficient:

- no live review surface
- no dual-party confirmation state
- no invalidation-on-change confirmation reset surface

### Live mutation participation

Existing:

- live Fabric service can submit a prebuilt `ExchangePayload`
- real inventory mutation is proven only in bounded tests
- Fabric seam work proves only inert Runtime contact, not earned live mutation
  seam contact

Not yet sufficient:

- no live player-backed Gate `#1` mutation path is proven
- `fabric_mutation` seam remains deferred

### Execution result reporting

Existing:

- `FabricCofferExecutionResult` distinguishes denied, executed, and platform
  unavailable
- `ExecutionResult` already preserves Runtime status truth

Not yet sufficient:

- no participant/admin-facing live projection attaches these results to a
  player exchange interaction
- no receipt attachment surface exists for live exchange records

### Participant and admin receipt attachment

Existing:

- lifecycle and ghost-adapter projections prove minimal stage projection only

Not yet sufficient:

- no participant receipt surface exists
- no admin receipt attachment beyond minimal JSONL stage lines exists
- current line shape does not attach counterparties, selected-value lineage,
  or reconstructable exchange-local state

### Interruption and unknown proof surfaces

Existing:

- runtime unknown is proven in bounded tests
- Fabric runtime seam unknown is proven inertly

Not yet sufficient:

- no live participant interaction interruption surface
- no live pending-exchange stale or disconnect proof
- no live execution-attempt interruption proof tied to real player exchange

## Bridge Gaps

### Selected hand or hotbar capture compatibility

Current binding semantics are region-based, not selected-slot-based.

Important current fact:

- `HOTBAR` resolves to the whole hotbar list
- `MAIN` resolves to the non-hotbar main inventory list
- no actor or container shape currently names "selected hotbar slot"

Pressure:

- Gate `#1` asks for the item currently held in the main hand or currently
  selected hotbar slot
- current removability checks aggregate matching quantity across the resolved
  region

Bridge risk:

- a selected-stack claim could be accidentally widened into "matching material
  anywhere in the hotbar region"
- stale selected-slot changes could be masked if another matching stack still
  exists in the region

Audit judgment:
Current binding semantics do not yet prove selected-slot fidelity.
A narrow compatibility layer appears likely unless the vessel is narrowed to a
selection surface already representable as a single bounded container region.

### Live Fabric mutation participation without unsafe seam coupling

Current fact:

- the repo has intentionally deferred `fabric_mutation`
- current mutation-carrier work proves only local representation of contact,
  not lawful seam crossing

Bridge gap:

- no current safe participant proves mutation-boundary contact distinct from
  bootstrap-coupled or gameplay-coupled behavior

### Core approval to Runtime to mutation path

Current fact:

- the path exists for already-constructed payloads through
  `CofferMinecraftFabricService.submitExchange(...)`
- inventory-bound approve and execute behavior is proven in tests

Bridge gap:

- no live player-backed capture and confirmation layer feeds that path
- no current exchange-local state carrier proves that the payload submitted to
  Fabric still corresponds to the current confirmed live selection state

### No pre-authorization mutation

Current evidence:

- Core and Runtime are already separated correctly
- Runtime only starts after approval in reusable repo-local code

Remaining gap:

- the live interaction layer that gathers selection and confirmation does not
  yet exist, so no proof yet shows that pending interaction handling cannot
  mutate inventory before submission

### No partial mutation

Current evidence:

- Gate `#1` can stay on a single authorized atomic-swap mutation, which avoids
  multi-step Runtime partial-success pressure at the plan level

Remaining gap:

- real live mutation atomicity is not yet proven in Fabric player reality
- `MinecraftPlayerInventoryContainer.applyAtomicSwap(...)` is simulation-first,
  but no live-player proof yet establishes that post-simulation mutation cannot
  encounter an unmodeled partial-side-effect boundary

### Stale or invalidation proof

Current evidence:

- post-approval drift is already proven as Runtime failure
- lifecycle docs and walkthroughs already define stale or invalidated states

Remaining gap:

- no pre-Core pending interaction state exists to become stale
- no live selection fingerprint, selected-slot identity, or confirmation reset
  proof exists
- no proof yet distinguishes "same descriptor elsewhere in region" from "same
  originally selected stack"

### SER/CER line-shape sufficiency for live proof

Current evidence:

- flat line shape is already proven for lifecycle, Core, inert Runtime, and
  ghost-adapter stage projection

Remaining gap:

- current shape is sufficient for seam proof, not yet for live Gate `#1`
  reconstructability
- no line currently attaches participant pair, interaction-local confirmation
  state, or selected-value lineage
- no participant/admin attachment surface is proven

Audit judgment:
Some compatibility or projection layer appears likely.
The gap does not yet prove that Core law or canonical receipt topology needs
change.
It does suggest the current minimal line shape is too small for the full live
proof vessel if participant/admin receipt attachment is in scope.

## What Must Not Be Assumed

- Do not assume selected main-hand capture is already represented by current
  binding actor refs.
- Do not assume hotbar-region semantics are equivalent to selected-slot
  semantics.
- Do not assume any live command, review, or confirmation surface already
  exists.
- Do not assume stale or invalidated pending exchange behavior exists.
- Do not assume current Fabric probes prove mutation-boundary contact.
- Do not assume current JSONL lines are already sufficient for participant or
  admin live receipts.
- Do not assume player inventory mutation on a live Fabric server is already
  proven safe, atomic, or interruption-classified.
- Do not assume a compatibility layer can be avoided.
- Do not assume offhand, hotbar, and selected main-hand can share one meaning
  without an explicit contract.
- Do not assume duplicate matching stacks across a region are harmless for
  selected-value claims.
- Do not assume Runtime failure and Runtime unknown cover all live interruption
  cases that Gate `#1` cares about.
- Do not assume the existing Fabric service input shape is the same thing as a
  live exchange interaction contract.

## Smallest Safe Next Implementation Step

Add one narrow test-first Fabric capture proof for selected inventory reality
only.

The step should prove, without Core submission or mutation, whether a live
player's currently selected hand or hotbar value can be captured into a bounded
exchange-local snapshot without widening into whole-region semantics.

Why this is the smallest safe next step:

- it converts the highest-risk bridge gap into evidence
- it does not force commands, confirmation flow, Runtime, or mutation seam work
- it can reveal whether a compatibility layer is actually required before
  broader Gate `#1` work proceeds

## Verification

Inspected command results before editing:

- `~/dev/coffer`: `git status --short` clean
- `~/dev/coffer`: `git diff --check` clean
- `~/dev/coffer-minecraft`: `git status --short` showed only
  `?? docs/journals/0091-live-player-exchange-proof-vessel.md`
- `~/dev/coffer-minecraft`: `git diff --check` clean

Verification after this audit entry should re-run:

- `git status --short`
- `git diff --check`

## Uncertainties

- whether selected main-hand capture can be kept fully inside current binding
  semantics without a new compatibility layer
- whether live mutation participation can be earned on Fabric without a new
  mutation-boundary bridge
- how much additional accountability projection is truly required before live
  participant and admin receipt attachment becomes honest rather than
  premature
