# Minimal Lawful Transaction Receipt Reference

## Mirror Note

This document is substrate-derived and mirrored here for repo-local reasoning
continuity.

Future `coffer-minecraft` collaborators and agents may not have reliable
cross-repo access to `~/dev/coffer`, so this copy exists to keep the receipt
topology available inside this repository.

Minecraft/Fabric work should adapt this receipt topology, not redefine it.

The canonical substrate source currently lives in
`dev/coffer/docs/journals/0072-minimal-lawful-transaction-receipt-topology.md`.

## Summary

Define the first minimal lawful transaction receipt topology for Coffer.

This is a foundational reconstructable commerce artifact pass, not a storage
schema, protocol definition, rollback mechanism, or UI/chat layout.

The purpose is to identify the minimum truthful receipt surfaces participants
and administrators can safely reason from after transferable-value interaction
occurs.

## Decision

Adopt a receipt topology that preserves interaction meaning without pretending
to expose every internal detail.

Receipts must answer: what meaningful reality transition occurred?

They must not answer by dumping opaque internal state, and they must not
collapse contact, authorization, execution, and completion into the same thing.

## Rationale

The repository now has enough bounded-lawful, participant-centered geometry to
name a receipt surface without turning it into generic accounting language.

The missing piece is not bookkeeping. The missing piece is a truthful,
reconstructable surface that can tell participants and administrators what
actually changed, what only became authorized, what was attempted, what
remained uncertain, and what never lawfully occurred.

That requires a receipt topology that preserves omission discipline, boundary
clarity, and anti-counterfeit certainty.

## Scope

Included:
- what constitutes a lawful transaction receipt
- the minimum reconstructable information that must exist
- what must remain omitted
- participant-facing vs administrator-facing receipt surfaces
- actionable vs informational receipt content
- uncertainty visibility
- authorization vs execution vs completion distinctions
- refusal representation
- interruption representation
- non-actionable infrastructure/container-boundary visibility
- anti-counterfeit receipt behavior

Excluded:
- storage schema
- packet or network structures
- rollback implementation
- GUI or chat layouts
- exact wording catalogs
- generic accounting terminology
- guarantees of execution or completion where only contact or authorization
  exists

## Minimal Receipt Topology

The minimum lawful receipt topology is:

1. receipt identity
2. interaction meaning transition
3. participant-facing receipt surface
4. administrator-facing receipt surface
5. actionability classification
6. uncertainty classification
7. boundary classification
8. omission discipline

These are conceptual receipt obligations, not storage fields.

### 1. Receipt Identity

Lawful condition:
A receipt must be identifiable as the record for one meaningful interaction
transition.

Participant reasoning surface:
Which interaction result is this receipt about?

Administrator reasoning surface:
Which lawful contact event or transition is being reconstructed?

What must remain omitted:
Do not require an opaque internal dump to identify the receipt.

Anti-counterfeit note:
Identity must be enough to tie the receipt to the real interaction, not enough
to expose every internal detail.

### 2. Interaction Meaning Transition

Lawful condition:
The receipt must state what meaningful reality transition occurred.

This can be one of a small number of truthful categories:
- refusal
- interruption
- authorization without execution
- execution attempted with known outcome
- execution attempted with unknown outcome
- lawful completion
- boundary-only non-actionable contact

Participant reasoning surface:
What changed in the world of the interaction?

Administrator reasoning surface:
What transition must be reconstructed, audited, or explained later?

What must remain omitted:
Do not present a transition that never occurred.

Anti-counterfeit note:
The receipt must preserve the distinction between contact and certainty.

### 3. Participant-Facing Receipt Surface

Lawful condition:
The participant receipt must preserve the minimum truth a participant needs to
understand the interaction outcome.

Participant-facing content should answer:
- what happened
- whether the result is actionable
- whether the outcome is certain, uncertain, refused, interrupted, or
  completed
- what should not be inferred

What must remain omitted:
Do not expose exhaustive internal state, incidental infrastructure detail, or
implementation noise.

Actionable vs informational:
Participant content should be actionable only when the participant can lawfully
do something with it. Otherwise it should remain informational and bounded.

### 4. Administrator-Facing Receipt Surface

Lawful condition:
The administrator receipt must preserve enough reconstructable detail to
understand and audit the transition without pretending to be a full internal
dump.

Administrator-facing content should answer:
- what interaction was contacted
- what boundary was crossed or refused
- what was authorized, attempted, unknown, completed, or interrupted
- what minimal operational context is needed to reconstruct the transition
- what boundary or infrastructure condition limited the result

What must remain omitted:
Do not require administrators to receive every internal implementation detail
for the receipt to be truthful.

Actionable vs informational:
Administrator content may be more diagnostic than participant content, but it
still must stay bounded to what is reconstructably relevant.

### 5. Actionability Classification

Lawful condition:
The receipt must distinguish whether the result exposes any lawful next step.

Participant reasoning surface:
Can I act on this, or is this only informational?

Administrator reasoning surface:
Does this result require operational follow-up, boundary review, or no further
action?

What must remain omitted:
Do not manufacture a retry path, correction path, or remediable path where none
exists.

Anti-counterfeit note:
Actionability must come from a real lawful surface, not encouragement.

### 6. Uncertainty Classification

Lawful condition:
The receipt must say when contact occurred without certainty.

Examples include:
- execution attempted but unknown
- interruption before closure
- non-actionable boundary contact with no further surface

Participant reasoning surface:
What is known, and what is still unknown?

Administrator reasoning surface:
What needs further investigation, if anything, and what does not?

What must remain omitted:
Do not infer success, failure, rollback, or completion from missing evidence.

Anti-counterfeit note:
Unknown must remain unknown.

### 7. Boundary Classification

Lawful condition:
The receipt must distinguish ordinary outcome content from boundary-only or
infrastructure/container-boundary visibility.

This includes cases where the meaningful result is that the interaction could
not proceed because a boundary was encountered.

Participant reasoning surface:
Was this a refusal, an interruption, or a boundary limit rather than a normal
outcome?

Administrator reasoning surface:
Did an infrastructure, container, authority, or runtime boundary shape the
result without turning that boundary into a false outcome claim?

What must remain omitted:
Do not treat infrastructure or container boundaries as if they were the same
thing as lawful completion.

Anti-counterfeit note:
Boundary visibility is informational evidence, not counterfeit success.

### 8. Omission Discipline

Lawful condition:
The receipt must preserve what was not lawfully known or not lawfully
attestable.

Omission is part of the receipt topology, not a defect in it.

Participant reasoning surface:
What was intentionally not claimed?

Administrator reasoning surface:
What was omitted because it was not earned, not visible, or not lawful to
state?

What must remain omitted:
Do not fill every gap with synthetic certainty.

Anti-counterfeit note:
Receipts should preserve meaning, not exhaust every internal detail.

## Receipt Surface Rules

### Participant Receipt

Participant-facing receipts should be short, truthful, and bounded.

They should preferentially show:
- the reality transition
- whether the outcome is actionable
- whether the outcome is uncertain
- whether refusal or interruption occurred
- whether completion is actually lawful

They should not show:
- opaque internal dumps
- internal coordination detail that does not help reconstruct meaning
- hidden container or infrastructure detail unless it changed the meaning of
  the result

### Administrator Receipt

Administrator-facing receipts should be reconstructable and minimally diagnostic.

They should preferentially show:
- what contact occurred
- where the boundary was
- whether the transition was authorization, execution attempt, unknown outcome,
  refusal, interruption, or completion
- enough context to reconstruct the transition honestly later

They should not become:
- an unbounded debug log
- a generalized accounting ledger
- a substitute for explicit lifecycle or execution artifacts

## Topology Notes

This receipt topology is intentionally smaller than a full audit model.

It should be enough to answer the question:
what meaningful reality transition occurred?

It should not be expanded into a universal history system, a persistence schema,
or a replay log.

The distinction between participant and administrator surfaces is important
because the same event can need different amounts of detail without changing the
underlying truth.

The distinction between authorization, execution, and completion is also
important because collapsing them would counterfeit certainty.

## Verification

Verification for this step consists of:
- `git status --short`
- `git diff --check`

## Uncertainties

This is the first minimal topology, not the final one.

Real commerce pressure may reveal:
- narrower receipt categories
- better boundary classification
- a sharper split between informational and actionable administrator detail
- additional lawful receipt surfaces for delegated or multi-party cases

Those future refinements should extend the topology only after they are earned
by actual contact pressure.
