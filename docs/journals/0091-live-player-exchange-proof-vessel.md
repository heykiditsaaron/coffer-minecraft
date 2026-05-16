# 0091 Live Player Exchange Proof Vessel

## Summary

Define the smallest lawful live Fabric proof vessel capable of honestly
exercising release-gate `#1`:

- player-to-player
- inventory-selected transferable value exchange
- inside live platform reality

This is a vessel-definition step, not a claim that the vessel already exists
in this repository.

It is not final gameplay UX, not release ergonomics, not admin-shop work, not
ledger work, and not rollback design.

## Decision

Treat the first live proof vessel as the minimum operational Fabric interaction
surface that can:

- capture real player-selected inventory material
- carry explicit two-party consent
- submit the resulting lawful request through Core and Runtime
- reach truthful mutation participation in live platform reality
- emit only earned accountability content

The vessel should be intentionally narrow, operational, and developer-heavy.
Its purpose is operational survivability proof, not player-facing polish.

## Rationale

The current repository already proves important fragments:

- player-inventory binding semantics
- Core denial and approval on bounded inventory swap paths
- Runtime success, failure, and unknown on bounded binding/runtime paths
- Fabric lifecycle, construction, Core, and inert Runtime accountability contact

What remains unproven is the live bridge that joins those truths into one
honest player-backed interaction inside Fabric reality.

The smallest lawful vessel therefore needs to prove only the minimal live chain
for gate `#1`, while avoiding overdesigned UX and avoiding counterfeit
readiness claims.

## Scope

Included:

- vessel purpose and boundaries
- the minimum participant interaction surface
- the minimum lawful selection boundaries
- required live operational proofs
- required accountability outputs
- acceptable temporary limitations
- unacceptable shortcuts
- success criteria for the vessel itself

Excluded:

- final gameplay UX
- GUI systems
- release polish
- admin shops
- ledger or currency value
- valuation
- batch liquidation
- permissions
- rollback systems
- final storage tooling

## Vessel Purpose

The vessel exists to prove all of the following and nothing broader:

- operational survivability rather than final UX
- the full lawful exchange chain in live Fabric reality
- truthful Runtime participation after actual Core approval
- truthful mutation-boundary participation
- truthful SER/CER or equivalent accountability participation during live
  contact

The vessel is successful only if it can demonstrate live player-backed contact
without counterfeiting success, completion, or readiness beyond gate `#1`.

## Minimum Participant Interaction Surface

The smallest acceptable interaction model is command-driven and
selection-minimal.

The best-fit minimum vessel is:

- initiator uses a command to target a recipient and open a bounded exchange
  attempt
- each participant's selected value is the item stack currently held in the
  main hand or the currently selected hotbar slot at the moment they explicitly
  confirm
- each participant can invoke a review command to see the currently captured
  exchange state in minimal developer-oriented form
- each participant must use an explicit confirmation command
- the interaction remains narrow to one selected stack per player for the proof
  vessel

Why this is the smallest lawful live model:

- it uses existing live player/inventory reality rather than synthetic
  containers
- it avoids inventing GUI, menus, screens, or declarative offer systems
- it provides a clear explicit-consent moment
- it gives stale/invalidation checks a concrete live boundary

This note does not claim that these commands already exist.

## Minimum Lawful Selection Boundaries

The vessel must stay within all of the following boundaries:

- selected inventory value only
- owned and removable inventory value only
- no declarative value offering
- no ledger value
- no constructed trade containers
- no valuation
- no permissions layer

Additional narrowing for the vessel is acceptable:

- one selected stack per participant
- one recipient per interaction
- one open interaction per participant if needed for auditability

The vessel must not broaden selection semantics beyond what the current player
inventory binding can lawfully attest.

## Required Live Operational Proofs

The vessel is not sufficient unless it can truthfully prove all of the
following in live Fabric reality:

- truthful inventory capture from actual player inventory state
- ownership and removability attestation at the inventory authority boundary
- receivability attestation for the recipient side
- explicit participant consent
- stale or invalidated selection denial
- Core denial participation
- Core approval participation
- Runtime participation after authorization only
- lawful mutation-attempt boundary contact
- no pre-authorization mutation
- no partial mutation for the interaction claims being made

The vessel should also prove these if they are safely reachable:

- truthful unknown handling
- interruption handling before execution
- interruption handling during or after execution attempt

If a reachable live path produces uncertainty, the vessel must surface that
uncertainty rather than collapsing it into failure or success.

## Required Accountability Outputs

The vessel must emit or otherwise preserve the minimum earned operational
record surfaces for live contact:

- SER participation where pre-Core or non-Core contact is the truthful record
- CER participation where Core, Runtime, or later earned contact actually
  occurs
- denial/accountability visibility
- approval visibility
- Runtime or execution participation visibility
- unknown visibility
- completion visibility where actually earned

This step does not authorize:

- redesigning SER/CER shape
- adding final storage tooling
- broadening the envelope beyond currently earned content without new proof

The live vessel must adapt to the existing accountability discipline rather
than redefining it.

## Acceptable Temporary Limitations

The following temporary limitations are acceptable for the live proof vessel:

- command-heavy interaction
- no GUI
- low polish
- incomplete participant ergonomics
- developer-oriented output
- intentionally narrow interaction scope
- one-stack-only exchange scope
- one-open-exchange-per-player restrictions
- explicit re-review or re-confirm requirements after invalidation

These limitations are acceptable because the vessel's purpose is truthful live
proof, not ergonomic completeness.

## Unacceptable Shortcuts

The following shortcuts are explicitly unacceptable:

- fake success
- hidden mutation
- silent failure
- inferred ownership
- inferred consent
- bypassed authorization
- bypassed Runtime participation
- mutation before approval
- hidden partial mutation
- counterfeit completion
- synthetic inventory capture that does not come from live player state
- using stale captured selection as if it were still current without renewed
  attestation

## Success Criteria

The vessel is sufficient only if all of the following become true in a bounded
and testable live path:

- the minimal player-backed interaction can be initiated, reviewed, confirmed,
  denied, approved, and executed inside live Fabric reality
- selection remains bounded to real player inventory material
- Core and Runtime participation remain explicit and ordered
- mutation occurs only after authorization
- no partial mutation is hidden or tolerated within the claimed interaction
- stale or invalid selection is refused truthfully
- accountability surfaces distinguish denial, approval, Runtime participation,
  unknown, and completion without counterfeiting any of them

If those conditions cannot be reached honestly, the vessel is not sufficient
and must be narrowed or deferred.

## Failure Conditions For The Vessel

The vessel should be considered not yet viable if any of the following prove
true:

- live player selection cannot be captured without inventing new container
  meaning the current binding cannot attest
- explicit two-party consent cannot be preserved without premature mutation or
  counterfeit state
- live Runtime/mutation participation cannot be crossed truthfully on Fabric
  with bounded scope
- accountability cannot describe live outcomes without redesigning receipt law
  first
- no-partial-mutation proof cannot be earned for the claimed live interaction

## Current Evidence The Vessel Can Reuse

Current repository-local evidence already supports parts of this vessel:

- `platforms/fabric/src/main/java/.../CofferMinecraftFabricService.java`
  provides a live Fabric-side exchange submission and server-thread execution
  shape for prebuilt payloads
- `bindings/inventory` and its tests prove current player inventory binding
  semantics and bounded Runtime results
- Fabric probe journals prove existing live accountability participation only
  through lifecycle, construction, Core, and inert Runtime contact

That evidence is necessary but not yet sufficient to claim the full vessel.

## Known Gaps

The following gaps remain visible from current repo-local evidence:

- no verified command or equivalent live participant interaction surface
- no verified live dual-party confirmation flow
- no verified live stale/invalidation path for selected player inventory value
- no verified truthful live mutation-boundary accountability proof
- no verified live participant-facing receipt surface
- no verified live admin-facing reconstructable receipt attachment beyond the
  current minimal contact records

## Uncertainties

The most important current uncertainties are:

- whether main-hand or selected-hotbar-stack capture is sufficient to stay
  within current binding semantics without a new compatibility layer
- whether live mutation participation on Fabric can be proven without
  reintroducing the coupling concerns that previously deferred the mutation
  seam
- whether current accountability line shape is sufficient for the first live
  vessel or whether a still-minimal earned extension becomes necessary

The exact command names, state-carrier shape, and review wording remain
deliberately unresolved here because this step defines the vessel boundary, not
its final implementation.
