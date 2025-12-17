# PHASE 3 — FABRIC ADAPTER
## Chronicle & Freeze

**Project:** Coffer  
**Phase:** 3 — Canonical Fabric Adapter  
**Status:** FROZEN  
**Authority:** Coffer Core Law (Phases 0–2)

---

## PURPOSE OF THIS PHASE

Phase 3 exists to **prove that the frozen Coffer Core can be expressed honestly inside a live platform (Minecraft Fabric)** without:

- inventing meaning,
- introducing shortcuts,
- relying on hacks,
- or locking future expressiveness.

Fabric is the **reference adapter**, not an authority.

This phase establishes **structural correctness**, not features.

---

## WHAT PHASE 3 DOES

Phase 3 implements a **canonical adapter spine** with the following guarantees:

### 1. Single Runtime Door
- A single Fabric runtime entry exists.
- All callers (commands, UI, other mods, future integrations) must route through it.
- Adapter readiness is explicit and authoritative.

### 2. Explicit Operational State
The adapter operates in explicit states:

- UNINITIALIZED  
- INITIALIZING  
- READY  
- RELOADING  
- FAILED  

Any state other than READY results in **explicit adapter-level refusal**.

No undefined behavior is permitted.

---

### 3. Boundary Declarations (No Inference)

Phase 3 defines **adapter-side declaration envelopes** that capture facts only:

- Declared intent
- Declared identities
- Declared items
- Declared metadata stance
- Invocation context (who called vs who is targeted)

No valuation, policy, or mutation meaning exists in the adapter.

The adapter may only:
- **declare facts**, or
- **refuse explicitly**.

---

### 4. Commands Are Callers, Not Owners

All commands introduced in Phase 3 are **diagnostic access surfaces only**.

Commands:
- do not own logic,
- do not bypass the adapter,
- do not invent meaning,
- do not have special authority.

Non-command invocation is explicitly preserved and supported.

---

### 5. Diagnostic Command Surface (Only)

The following commands exist as **diagnostic entry points**:

- `/coffer` — base adapter readiness check
- `/shop open` — shop access acknowledgement (no UI)
- `/sell` — bulk liquidation entry acknowledgement (no aggregation)

These commands:
- route through the runtime door,
- refuse explicitly when unavailable,
- do not open UI,
- do not parse items,
- do not call Core,
- do not mutate state.

They exist solely to prove **clean access and refusal plumbing**.

---

## PERMISSION MODEL (PHASE 3)

### Explicit-Only Restrictions
- Permissions are enforced **only when explicitly declared**.
- There is **no implicit deny**.
- **Omission implies permission**.

This applies to:
- shop access,
- per-item access,
- future UI interactions.

Commands do not own permissions; configurations do.

---

## ITEM FUNGIBILITY & METADATA

### Non-Fungibility
- Items are treated as **potentially non-fungible by default**.
- Fungibility is never assumed.
- Any uncertainty results in explicit refusal.

### Metadata Relevance
Metadata (NBT or equivalent) follows a strict rule:

- Metadata may influence valuation or policy **only if explicitly declared relevant**.
- Metadata may be ignored **only if explicitly declared irrelevant**.
- Undeclared metadata relevance **must result in refusal**.

### Metadata Ignorance (Explicit & Auditable)
Adapters MAY ignore metadata **only** when:
- the choice is explicit,
- the choice is auditable,
- the scope is limited to valuation/policy relevance.

Silent normalization or accidental omission is forbidden.

---

## REFUSAL DOCTRINE

Phase 3 prefers **explicit refusal over approximation**.

Refusal is:
- truthful,
- non-semantic,
- adapter-owned,
- auditable.

Adapter refusals are **not Core denials** and must never be represented as such.

---

## WHAT PHASE 3 EXPLICITLY DOES NOT DO

Phase 3 intentionally does **not** implement:

- valuation logic,
- policy logic,
- Core invocation,
- UI screens,
- inventory workspaces,
- aggregation semantics,
- persistence,
- async execution,
- permission systems,
- metadata parsing logic,
- NBT ordering or specificity rules.

Any of the above introduced here would constitute **premature lock-in**.

---

## CONSIDERED & DEFERRED

### Metadata-Aware Valuation Ordering
Phase 3 explicitly considered a **breadth-first, tiered valuation ordering model** to support metadata-aware valuation (e.g., NBT-specific rules overriding base identity rules).

This model emphasized:
- explicit rule tiers,
- declared precedence,
- refusal on ambiguity,
- auditability of the chosen rule.

This model was **intentionally deferred** because:
- Phase 3’s purpose is to prove the engine, not valuation expressiveness.
- Metadata ordering is platform-specific.
- Premature implementation risked freezing the wrong abstraction.

The adapter and Core boundaries were designed to **permit this model later without refactor**.

---

## PHASE 3 COMPLETION STATEMENT

Phase 3 is complete and frozen.

It establishes:
- a canonical Fabric adapter spine,
- honest access surfaces,
- explicit refusal everywhere,
- and zero semantic drift.

Future phases may safely build:
- valuation,
- policy,
- UI,
- aggregation,
- persistence,
- and expressiveness

on top of this foundation **withou**
