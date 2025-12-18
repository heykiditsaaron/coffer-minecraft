# PHASE 3C.2 — INVENTORY-BACKED DECLARATION
Status: COMPLETE  
Scope: Fabric Adapter — Declaration Truth  
Authority: Coffer Core Law (unchanged)

---

## PURPOSE

Phase 3C.2 establishes **truthful exchange declaration** based on
**authoritative platform ownership**, prior to valuation or mutation.

This phase answers the question:

> “When is it honest to even attempt an exchange?”

---

## CONTEXT

Prior phases proved:
- Core evaluation can be invoked safely
- Valuation can deny honestly
- Absence is valid

However, a critical ambiguity remained:

**What constitutes ownership on the platform?**

Without answering this, adapters risked:
- inviting intent when none could be fulfilled
- guessing at possession
- deferring refusal until too late

Phase 3C.2 resolves this ambiguity.

---

## DISCOVERY

### Ownership Is Not UI

Through Fabric implementation and runtime testing, we learned:

- UI state (commands, screens, menus) cannot be trusted as ownership
- Selection surfaces must not define truth
- Ownership must be derived from **authoritative server-side inventory state**

This led to a platform-native rule:

> **If the player could place it into a chest, they own it.**

This rule:
- aligns with player intuition
- survives modded inventories
- excludes control surfaces by design
- is enforceable without guessing

---

## IMPLEMENTATION INSIGHT

### Declaration Required Its Own Layer

Attempting to build exchange truth inside commands or translators
introduced implicit assumptions.

The solution was to introduce a dedicated **declaration builder** that:

- observes ownership
- constructs declared facts
- refuses early when truth does not exist
- remains independent of UX and Core semantics

This separation proved essential.

---

## EARLY REFUSAL AS HONESTY

Phase 3C.2 introduced **refusal before Core invocation** when:

- no owned items exist
- metadata relevance cannot be declared honestly

This prevents:
- wasted player effort
- misleading evaluation
- downstream partial failures

Refusal here is not punitive — it is clarifying.

---

## METADATA RELEVANCE (3C.2.A)

Metadata was identified as information, not noise.

Phase 3C.2 does **not** interpret metadata, but it does require
that the adapter take an **explicit stance**:

- RELEVANT
- IGNORED_BY_DECLARATION
- UNDECLARED

A permissive-by-default stance was chosen to:
- avoid punishing exploration
- preserve zero-config boot
- keep all decisions explicit and auditable

No guessing is permitted.

---

## NON-GOALS (INTENTIONAL)

Phase 3C.2 does NOT:
- perform valuation
- mutate inventory
- define sell UX
- aggregate across metadata
- read admin config from disk

These are deferred by design.

---

## OUTCOME

Phase 3C.2 guarantees:

- Core is only invoked with truthful declarations
- Ownership is platform-native and auditable
- Metadata stance is explicit
- Absence is handled early and honestly
- Future UX remains unconstrained

The adapter now constructs truth.
The Core judges it.

---

## CARRY-FORWARD

Future phases may:
- introduce UI-based selection
- load metadata policy from config
- refine aggregation keys
- execute mutation

None of these require revisiting Phase 3C.2.

This phase is complete.
