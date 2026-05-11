# 0071 Fabric Inert Core Approval Without Runtime

## Summary

Introduced the smallest safe real-platform Core-approval seam on the Fabric
startup path.

After lifecycle, construction-refusal, and Core-denial startup probes, Fabric
now performs one inert Core-approval probe that intentionally stops before
Runtime.

## Decision

Use a startup-bounded paved-construction success plus an inert approving Core
authority.

When Core approves and produces a mutation plan, append one minimal `CER`
record:

- `interactionId`
- `recordType`
- `stage`

with stage `fabric_core_approved`.

Do not invoke Runtime or emit execution/mutation participation in this step.

## Rationale

This is the smallest safe approval seam because it:

- reaches actual Core arbitration on the real Fabric path
- proves approval is distinguishable from denial
- intentionally stops before Runtime
- avoids gameplay, player interaction, and inventory mutation

Runtime remains absent by design because this proof only asks whether
accountability survives a deeper Core boundary, not whether execution should
occur.

## Scope

Included:

- startup-bounded approved Core contact
- minimal `CER` append under `logs/coffer/fabric-lifecycle.jsonl`
- tests proving approval is distinct from denial
- tests proving Runtime/execution/mutation remain omitted

Excluded:

- Runtime invocation
- mutation execution
- gameplay or session flow
- production adapter behavior

## Verification

Covered by tests and combined build validation:

- lifecycle, construction-refusal, and Core-denial lines remain unchanged
- approved Core contact emits a distinct `CER` line
- approved does not imply Runtime participation
- JSONL shape remains minimal and readable

## Uncertainties

This approval seam uses an inert approving authority rather than live binding or
player participation.

Still deferred:

- binding-aware live-platform Core approval
- Runtime non-invocation proof after binding-aware approval
- any truthful execution participation
