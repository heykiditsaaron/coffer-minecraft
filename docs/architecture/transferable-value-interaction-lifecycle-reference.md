# Transferable-Value Interaction Lifecycle Reference

## Mirror Note

This document is substrate-derived and mirrored here for repo-local reasoning
continuity.

Future `coffer-minecraft` collaborators and agents may not have reliable
cross-repo access to `~/dev/coffer`, so this copy exists to keep the relevant
participant interaction topology available inside this repository.

Minecraft/Fabric work should adapt this topology, not redefine it.

The canonical substrate source currently lives in
`dev/coffer/docs/journals/0071-transferable-value-interaction-lifecycle-topology.md`.

## Summary

Define the first participant-centered experiential topology for
transferable-value interaction with Coffer.

This is a discovery pass over currently visible lawful states, not a final
doctrine, UI flow, protocol, or exhaustive ontology.

## Decision

Adopt a first transferable-value interaction lifecycle topology that describes
participant-facing lawful states around value declaration, counterparty
contact, refusal, authorization, execution contact, and completion.

Treat the topology as intentionally incomplete. Preserve room for additional
states, splits, merges, and sharper distinctions as real adapter contact and
runtime accountability surfaces emerge.

## Rationale

The project now has clearer mission, lawful-reality, experiential-contact, and
trustworthy-outcome framing, but it still lacks a compact participant-centered
topology for transferable-value interaction.

That gap matters because participants do not directly reason in Core/Runtime
class boundaries. They reason through what value is being put forward, whether
the other side is lawfully reachable, whether refusal can be acted upon,
whether Coffer has only authorized change or actually contacted execution, and
whether the interaction ended in a trustworthy completion versus an unresolved
contact boundary.

The topology must therefore preserve explicitness without counterfeiting
certainty. In particular, it must not collapse authorization into completion,
must not imply delivery where only execution was attempted, and must not
pretend a participant can act on refusals that expose no lawful corrective
surface.

## Scope

Included:
- the first known transferable-value interaction lifecycle states
- the lawful condition each state represents
- the participant reasoning surface available at each state
- what must remain omitted at each state to preserve lawful behavior
- actionable versus non-actionable refusal distinction where relevant
- explicit preservation of anti-counterfeit outcome behavior

Excluded:
- UI flows, screens, menus, chat layouts, or wording
- gameplay behavior
- protocol, packet, schema, or payload expansion
- exact denial catalog work
- exhaustive or permanent lifecycle doctrine
- guarantees of execution, delivery, or atomic completion beyond what Coffer
  can lawfully attest

## Lifecycle Topology

The current topology is:

1. value selection
2. offer formation
3. pending attestation
4. awaiting counterparty
5. actionable refusal
6. non-actionable refusal
7. stale or invalidated
8. authorized transfer
9. execution attempted
10. execution unknown
11. lawful completion
12. interrupted contact

These states are not required to be the final complete set, and future work may
introduce additional intermediate, nested, or parallel states without
invalidating this first topology.

### 1. Value Selection

Lawful condition:
Participant has identified candidate transferable value they may put into
contact, but no lawful offer has yet been formed.

Participant reasoning surface:
What am I actually placing into consideration, and is it the value I intend to
declare for exchange?

What remains omitted:
No claim yet exists about counterparty willingness, validity, authorization,
execution, or completion.

Anti-counterfeit note:
Selection must not be represented as commitment, reservation, or pending
transfer.

### 2. Offer Formation

Lawful condition:
Participant-facing interaction has gathered enough declared intent to frame a
prospective transferable-value exchange for submission into lawful contact.

Participant reasoning surface:
Is this the offer I mean to make, to whom is it directed, and is the declared
value relationship the one I intend to place before Coffer?

What remains omitted:
No attestation result yet exists. No approval, no denial, and no execution
claim should be implied.

Anti-counterfeit note:
Offer formation must not be presented as if the exchange is already pending on
the other side unless lawful contact has actually begun.

### 3. Pending Attestation

Lawful condition:
Coffer has been asked to evaluate the declared transferable-value interaction,
but lawful truth arbitration is not yet known to the participant.

Participant reasoning surface:
Has the proposed interaction been lawfully admitted, refused, or left without a
final attested result yet?

What remains omitted:
No participant should be told that the interaction is valid, refused, or
authorized before attestation is actually available.

Anti-counterfeit note:
This state preserves uncertainty explicitly rather than speculating about likely
outcomes.

### 4. Awaiting Counterparty

Lawful condition:
The interaction has not yet terminated, but progress now depends on a distinct
counterparty contact or participation boundary rather than on unilateral local
selection alone.

Participant reasoning surface:
Has the other side entered lawful contact yet, and is the interaction still
live enough to proceed if they do?

What remains omitted:
Do not imply acceptance, refusal, or future completion merely because the
counterparty has not yet answered.

Anti-counterfeit note:
Silence or absence of response is not counterfeit consent.

### 5. Actionable Refusal

Lawful condition:
Coffer or its lawful contact boundary has refused the interaction in a way that
exposes a participant-relevant corrective surface.

Participant reasoning surface:
What can be changed, withdrawn, re-selected, re-targeted, or re-attempted if
the participant wants to pursue a lawful interaction?

What remains omitted:
Do not imply that every refusal can be bypassed, negotiated away, or retried
without meaningful change.

Actionable distinction:
This state exists only when the participant can reasonably alter declared
conditions or contact posture in response to the refusal.

Anti-counterfeit note:
Actionability must come from a real lawful correction surface, not from vague
encouragement to try again.

### 6. Non-Actionable Refusal

Lawful condition:
The interaction has been refused, but the participant-facing surface exposes no
meaningful lawful next action inside the current contact frame.

Participant reasoning surface:
The interaction did not proceed, and there is no immediate lawful corrective
move available from this state alone.

What remains omitted:
Do not invent fix paths, hidden prerequisites, or speculative explanations that
the system cannot lawfully attest.

Non-actionable distinction:
The refusal is real and final for the present interaction even if future
external circumstances might someday differ.

Anti-counterfeit note:
Non-actionable refusal must remain an explicit stop, not disguised as temporary
delay.

### 7. Stale or Invalidated

Lawful condition:
A once-viable interaction frame is no longer trustworthy because its contact
basis, declared value reality, timing, or participant context can no longer be
treated as current.

Participant reasoning surface:
Is this still the same lawful interaction, or must it be re-formed before any
further trust can be placed in it?

What remains omitted:
Do not imply that a stale interaction is still pending, still authorized, or
safe to resume without renewed contact.

Anti-counterfeit note:
Invalidated context must not inherit prior legitimacy automatically.

### 8. Authorized Transfer

Lawful condition:
Core has lawfully authorized the transferable-value interaction and produced the
authorized mutation intent, but execution has not thereby been guaranteed.

Participant reasoning surface:
The interaction was admitted and authorized. Has execution contact happened
yet, or does this remain authorization without completed transfer?

What remains omitted:
Do not collapse authorization into completion, success, possession change, or
durable outcome.

Anti-counterfeit note:
Authorized means permitted and specified, not necessarily executed.

### 9. Execution Attempted

Lawful condition:
Runtime contact has been attempted against the authorized transferable-value
mutation path.

Participant reasoning surface:
Execution was actually contacted rather than merely authorized. What final
attested execution result, if any, is known?

What remains omitted:
Do not imply success merely because execution began or was attempted.

Anti-counterfeit note:
Attempted execution is evidence of contact, not evidence of completion.

### 10. Execution Unknown

Lawful condition:
Execution contact was entered, but the participant does not have a trustworthy
final result surface establishing whether lawful completion occurred.

Participant reasoning surface:
Something crossed into runtime contact, but what can actually be trusted about
the outcome right now?

What remains omitted:
Do not synthesize success, failure, rollback, or atomicity claims from missing
contact evidence.

Anti-counterfeit note:
Unknown execution outcome must remain explicitly unknown even when inference is
tempting.

### 11. Lawful Completion

Lawful condition:
The participant has a trustworthy basis to treat the transferable-value
interaction as completed within the bounds Coffer can lawfully attest.

Participant reasoning surface:
The interaction reached a trustworthy terminal outcome. What is now settled,
and what no longer requires participant doubt inside this contact boundary?

What remains omitted:
Do not overclaim beyond the completion surface actually attested. Completion
must not silently expand into guarantees outside the lawful boundary.

Anti-counterfeit note:
Completion requires trustworthy outcome grounding, not optimistic projection.

### 12. Interrupted Contact

Lawful condition:
The participant-facing interaction lost continuity before a trustworthy terminal
state could be established, leaving contact broken rather than lawfully
completed.

Participant reasoning surface:
Did the interaction actually finish, or did the contact path break in a way
that prevents trustworthy closure?

What remains omitted:
Do not translate interruption into refusal, completion, or safe retry
assumptions without a new lawful basis.

Anti-counterfeit note:
Broken contact is not evidence of no-op and not evidence of success.

## Topology Notes

This lifecycle is intentionally participant-centered rather than
component-centered. It describes what a participant can lawfully understand about the
interaction, not the internal method or message sequence used by adapters,
Core, Runtime, or authorities.

The topology also preserves explicit silence as a valid state. Not every moment
of interaction requires positive outcome language, and absence of trustworthy
attestation must not be cosmetically replaced with certainty.

Refusal remains split because participant reasoning differs materially between a
surface that exposes lawful corrective action and a surface that does not.

Authorization, execution contact, unknown outcome, and lawful completion also
remain separated because collapsing them would counterfeit trust and violate the
repository's explicit outcome discipline.

## Verification

Verification for this step consists of:
- `git status --short`
- `git diff --check`

## Uncertainties

This topology should be treated as foundational but emergent.

Future work may reveal:
- sub-states inside counterparty contact or execution contact
- sharper distinctions between invalidation and interruption
- lawful terminal states not yet visible from the present transferable-value
  proof layer
- multi-party or delegated-participation cases that require additional
  participant reasoning surfaces

Those additions would refine this topology rather than invalidate the core
decision to model transferable-value interaction through explicit lawful states.
