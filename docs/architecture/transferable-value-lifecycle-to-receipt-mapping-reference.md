# Transferable-Value Lifecycle to Receipt Mapping Reference

## Mirror Note

This document is substrate-derived and mirrored here for repo-local reasoning
continuity.

Future `coffer-minecraft` collaborators and agents may not have reliable
cross-repo access to `~/dev/coffer`, so this copy exists to keep the lifecycle
to receipt mapping available inside this repository.

Minecraft/Fabric work should adapt this mapping, not redefine it.

The canonical substrate source currently lives in
`dev/coffer/docs/journals/0073-transferable-value-lifecycle-to-receipt-mapping-pressure-test.md`.

## Summary

Pressure-test the minimal lawful transaction receipt topology against the
transferable-value interaction lifecycle topology.

This is a mapping exercise, not a new lifecycle doctrine and not a receipt
redefinition.

## Decision

Treat only the later lifecycle states as receipt-worthy by default.

Earlier states remain participant-visible interaction states, but they do not
yet earn durable lawful receipts because they are still preparatory or
contact-pending rather than meaningful reality transitions.

## Rationale

The lifecycle topology includes both preparatory visibility and true reality
transitions. The receipt topology should track only the latter unless a state
has enough lawful contact pressure to justify reconstructable receipt content.

If we emit receipts too early, we counterfeit certainty and overstate the
meaning of mere interaction visibility.

If we wait too long, we lose reconstructability where it actually matters.

The right split is therefore between:
- temporary participant visibility
- deferred/unknown pressure-bound states
- genuine receipt-worthy transitions

## Mapping

| Lifecycle state | Receipt surface | Why |
| --- | --- | --- |
| value selection | temporary participant feedback/state visibility only | Preparatory selection is not yet a lawful reality transition. |
| offer formation | temporary participant feedback/state visibility only | The participant is still forming the offer, not yet earning a receipt. |
| pending attestation | deferred/unknown pending future pressure | The interaction is under lawful evaluation, but the outcome surface is not yet earned. |
| awaiting counterparty | deferred/unknown pending future pressure | The interaction depends on external contact and should stay omitted until the counterparty boundary resolves. |
| actionable refusal | both participant and administrator receipt | This is a meaningful transition with a participant-corrective surface and reconstructable admin value. |
| non-actionable refusal | both participant and administrator receipt | The refusal itself is meaningful even when no participant correction is available. |
| stale or invalidated | both participant and administrator receipt | The interaction frame has become untrustworthy and that transition should be reconstructable. |
| authorized transfer | both participant and administrator receipt | Authorization is a real transition, but the receipt must preserve that it is not completion. |
| execution attempted | both participant and administrator receipt | Execution contact is a meaningful transition even when certainty has not been earned yet. |
| execution unknown | both participant and administrator receipt | The uncertainty itself is receipt-worthy because it records contact without counterfeit certainty. |
| lawful completion | both participant and administrator receipt | Completion is the strongest reconstructable terminal transition and should be receipted. |
| interrupted contact | both participant and administrator receipt | Broken contact is a meaningful end-state and must remain reconstructable. |

## States That Earn Receipts

These states should produce durable lawful receipts:
- actionable refusal
- non-actionable refusal
- stale or invalidated
- authorized transfer
- execution attempted
- execution unknown
- lawful completion
- interrupted contact

## States That Do Not Yet Earn Receipts

These states should remain temporary participant visibility or deferred/unknown
pressure rather than durable receipts:
- value selection
- offer formation
- pending attestation
- awaiting counterparty

## Participant/Admin Distinction

The participant surface should preserve only the minimum truth needed to reason
safely about what happened.

The administrator surface should preserve the minimum reconstructable context
needed to audit the transition later.

Both surfaces are warranted for the receipt-worthy states because the topology
is about meaningful reality transitions, not opaque internal dumps.

## Omission And Anti-Counterfeit Rules

Receipts must continue to omit:
- internal detail that does not change the meaning of the transition
- certainty where only contact or authorization exists
- synthetic recovery paths where none are lawfully available
- execution success where execution is still unknown

Receipts must continue to preserve:
- contact vs certainty distinctions
- authorization vs execution vs completion distinctions
- reconstructability
- bounded lawful reality framing

## Unresolved Questions

The main unresolved question is whether `pending attestation` and `awaiting
counterparty` should remain permanently outside receipt topology or whether
future adapter pressure will justify a narrow administrative receipt for one or
both states.

For now, they remain deferred because they are contact-pending states rather
than meaningful reality transitions.

Another open question is whether `stale or invalidated` should ever split into
separate stale and invalidated receipt categories if future pressure makes that
distinction operationally meaningful.

## Verification

Verification for this step consists of:
- `git status --short`
- `git diff --check`

## Uncertainties

This mapping is intentionally provisional.

It should be revised only when real commerce pressure, adapter pressure, or
runtime accountability pressure produces clearer evidence that a currently
deferred state has earned a lawful receipt surface.
