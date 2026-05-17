## Summary

Implement the test-only shared mutation seam recommended by `0106`.

This connects:

- Fabric selected-authored submission chain output
- Core approval and denial boundary
- the already-safe bindings Runtime mutation vessel
- Fabric accountability projection

## Decision

The shared mutation seam is crossed for this proof scope.

Use the bindings-safe bootstrap environment to host real `ItemStack`-backed
mutation while exercising the actual Fabric submission-chain and accountability
classes.

## Rationale

`0105` showed that direct live mutation proof in the `platforms/fabric` test
geometry was blocked by unsafe Minecraft bootstrap behavior. `0106` recommended a
shared seam instead of deeper Fabric bootstrap work.

This proof follows that recommendation:

- the Fabric-side confirmation, assembly, submission, and accountability classes
  are used directly
- the bindings-side bootstrap-safe Runtime mutation vessel provides the real
  inventory mutation execution
- Core and Runtime remain distinct
- no Fabric bootstrap hack is introduced

## Seam Shape

The seam is test-only and narrow:

- dual-confirmed Fabric exchange state becomes a submitted Fabric payload
- Core arbitration runs through real transferable-value authority truth
- approved mutation enters real `CofferRuntime` execution through the bindings-safe
  Runtime authority vessel
- Fabric accountability projection records the resulting Core/Runtime outcome

## Mutation Behavior Proven

- Core denial leaves inventories untouched
- no mutation occurs before Core approval
- Core approval can enter real Runtime execution
- successful execution mutates inventories truthfully
- failure remains distinct and honest
- unknown remains distinct and honest
- no counterfeit execution success is emitted

## Accountability Behavior Proven

Fabric-side accountability projection matches the shared seam outcome:

- denial produces only `fabric_core_denied`
- success produces `fabric_core_approved` then `fabric_runtime_succeeded`
- failure produces `fabric_core_approved` then `fabric_runtime_failed`
- unknown produces `fabric_core_approved` then `fabric_runtime_unknown`

## Scope

Included:

- test-only shared mutation seam
- success, failure, unknown, and denial proof paths
- real inventory mutation through the existing bindings Runtime vessel

Excluded:

- final gameplay UX
- receipts
- broader admin or valuation systems
- production placement claims

## Verification

This closes the `0105` compatibility bridge for proof purposes:

- the repo now has one safe proof surface joining Fabric submission-chain output
  to real Runtime mutation behavior
- the bridge works without forcing unsafe Fabric-native bootstrap hacks

## Uncertainties

Still unresolved for live Gate `#1`:

- production placement of the bridge, if any
- higher-fidelity platform proof directly inside a safe Fabric-native mutation
  harness
- live player orchestration, review, confirmation UX, and receipts
