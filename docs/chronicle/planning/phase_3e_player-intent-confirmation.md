# Phase 3E — Player Intent Confirmation (Planning Chronicle)

Status: **PLANNING (NON-BINDING)**  
Chronicle Type: **Exploratory / UX-Guiding**  
Authority Level: **Advisory (Superseded by Final Phase Chronicle)**

---

## Purpose of This Document

This document defines the **intent, constraints, and safety rails** for Phase 3E
before implementation begins.

It is deliberately:
- non-binding
- amendable
- experience-focused
- explicit about what must NOT happen

Its purpose is to prevent UX-driven logic drift while enabling a humane,
low-tedium player experience.

All authoritative meaning for Phase 3E will be recorded only in the
**final phase chronicle**, written after a successful build.

---

## Phase Question

Phase 3E exists to answer one question only:

> How does the system communicate frozen economic intent to the player
> clearly, gently, and safely *before* execution occurs?

This phase **does not determine truth**.  
It **reveals truth** that already exists.

---

## Core Objectives (Reaffirmed)

Phase 3E is governed by the following objectives:

- Remove tedium for players and administrators
- Remove the burden of honesty from humans
- Ensure outcomes are always explainable
- Prevent surprise, punishment, or dismissal
- Allow safe exploration without commitment
- Preserve system integrity without supervision

UX exists to **communicate**, not to decide.

---

## Inputs Available to Phase 3E (Read-Only)

Phase 3E may read the following adapter-owned outputs:

- `DeclaredExchangeRequest`
- `BalanceCreditPlanningResult`
  - `BalanceCreditPlan` (when present)
  - `BalanceCreditPlanningRefusal` (when present)
- Explicit refusal codes and messages
- Adapter-owned identity bindings

These inputs are:
- already validated
- already frozen
- already honest

Phase 3E must not alter them.

---

## Explicit Constraints

Phase 3E MUST NOT:

- Recompute value
- Reinterpret Core or adapter rules
- Perform mutation
- Perform execution
- Guess player intent
- Hide refusal reasons
- Introduce side effects
- Depend on UI state for correctness
- Create alternative logic paths

UX must never become a logic source.

---

## UX Semantics (Intent-Level)

The UX produced in Phase 3E should communicate:

- What items are accepted
- What items are rejected
- What value will be credited
- Why some items are not eligible
- That nothing has happened yet
- That confirmation agrees to outcome, not rules

Partial acceptance is normal and expected.

The tone must be:
- calm
- factual
- instructional by behavior
- never punitive

---

## Confirmation Semantics

Confirmation in Phase 3E represents:

> “I agree to this exact outcome.”

It does NOT represent:
- agreement to policies
- agreement to hidden rules
- acceptance of risk
- acknowledgment of fault

Once confirmed, execution may proceed using
already-frozen intent (Phase 3D.3 + 3D.4).

---

## Failure & Refusal Presentation

Planning refusals must be presented as:

- informational
- non-alarming
- reversible
- explainable

Examples:
- “Some items can’t be sold right now.”
- “Nothing here has value yet.”
- “No changes were made.”

Refusal is never an error state.

---

## Non-Goals of This Phase

Phase 3E intentionally does NOT:

- Define final UI layout
- Introduce persistence
- Handle admin configuration
- Modify economic rules
- Replace command-based flows
- Remove scaffolding
- Optimize visuals

Those belong to later phases.

---

## Open Questions (To Be Resolved During Phase)

- How is inventory selection surfaced safely?
- How are partial results visually grouped?
- How much explanation is enough?
- What affordances reduce admin support load?
- Where does command-based UX coexist with UI-based UX?

These questions must be answered through
inspection and iteration, not assumption.

---

## Success Criteria (Planning-Level)

Phase 3E will be considered successful when:

- Players can explore selling safely
- Outcomes are always understandable
- No hidden logic exists in the UI
- No execution occurs without confirmation
- Admins are not required to explain behavior
- The system remains honest without supervision

Only then may a **final Phase 3E chronicle** be written.

---

## Non-Binding Nature

This document:
- may be annotated
- may be corrected
- may be partially invalidated

It exists to guide implementation,
not to freeze semantics.

All binding truth for Phase 3E will live in the
final chronicle written after a successful build.

---

End of planning chronicle.
