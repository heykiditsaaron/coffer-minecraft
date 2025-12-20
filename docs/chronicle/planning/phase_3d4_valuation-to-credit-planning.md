# Phase 3D.4 — Valuation → Credit Planning (Planning Chronicle)

Status: **PLANNING (NON-BINDING)**  
Chronicle Type: **Exploratory / Guiding**  
Authority Level: **Advisory (Superseded by Final Phase Chronicle)**

---

## Purpose of This Document

This document exists to **clarify intent and scope** for Phase 3D.4 before implementation begins.

It is deliberately:
- non-binding
- amendable
- incomplete by design

Its purpose is to **prevent guessing**, not to freeze semantics.

All authoritative meaning for Phase 3D.4 will be recorded only in the **final phase chronicle**, written after a successful build.

---

## Phase Question

Phase 3D.4 exists to answer one question only:

> How does the adapter transform a Core evaluation result into a fully explicit, immutable `BalanceCreditPlan` without guessing, reinterpretation, or recomputation?

This phase **does not execute mutation**.  
It prepares mutation truthfully.

---

## Known Inputs (As of Planning)

The following inputs are believed to be available based on prior phases.  
Their actual sufficiency must be validated during implementation.

### Core-Side
- `ExchangeEvaluationResult`
  - Treated as opaque at the boundary
  - Only explicitly exposed surfaces may be consumed
  - No inference or reinterpretation is permitted

- `ValuationService`
  - Produces a `ValuationSnapshot`
  - Does not mutate state

- `ValuationSnapshot`
  - Immutable Core-produced valuation output
  - Contains:
    - `List<ValuationItemResult>`
    - `long totalAcceptedValue`
  - Encodes acceptance decisions and value totals upstream
  - Must be treated as authoritative when present

### Adapter-Side
- `DeclaredExchangeRequest`
- `DeclaredItem`
- `ExchangeIntent`
- Metadata relevance declarations
- Installed `ValuationService`
- Installed policy layers

If any of these assumptions are invalid, this document must be annotated.

---

## Discovery Annotation — Valuation Surface Reality

**Discovery:**  
Inspection of Core valuation interfaces confirmed that all valuation snapshots are produced by `ValuationService` and are instances of `ValuationSnapshot`.

**Implications:**
- Although `ExchangeEvaluationResult` stores the snapshot as `Object`, the producer contract is typed and stable.
- `ValuationSnapshot.totalAcceptedValue()` provides a single, explicit numeric credit surface.
- Acceptance and rejection decisions are already resolved by Core.
- The adapter must not recompute, reinterpret, or infer value beyond this surface.

**Constraints Reaffirmed:**
- Planning must refuse if the snapshot is:
  - missing
  - not a `ValuationSnapshot`
  - reports zero accepted value
- The adapter must not depend on:
  - external APIs
  - third-party libraries
  - platform-provided economic systems
- All credit planning must rely solely on Core-produced truth and adapter-owned logic.

This discovery enables Phase 3D.4 planning **without violating the no-guessing rule**.

This annotation is **non-binding** and must be reconciled in the final Phase 3D.4 chronicle.

---

## Intended Output

Phase 3D.4 intends to produce exactly one of the following outcomes:

### 1. `BalanceCreditPlan`
- Adapter-owned
- Immutable
- Explicit numeric credit amount
- Bound to a specific player UUID
- Frozen before execution
- Never recomputed during mutation

### 2. Explicit Planning Refusal
Examples (non-exhaustive):
- Core PASS with no accepted value
- Snapshot missing or unsupported
- Metadata relevance blocks valuation
- Ambiguous or unavailable valuation surfaces

No silent fallback is permitted.

---

## Explicit Constraints

The following constraints are reaffirmed:

- No guessing
- No recomputation of value
- No mutation
- No execution
- No persistence
- No UI concerns
- No policy changes
- No Core introspection beyond public surfaces
- No reinterpretation of Core meaning
- No reliance on external dependencies or APIs

Planning must be boring, explicit, and auditable.

---

## Proposed Internal Responsibilities (Tentative)

The following responsibilities are proposed for Phase 3D.4.  
They may change as implementation reveals facts.

### 1. Evaluation Surface Inspection
- Require `ExchangeEvaluationResult.allowed() == true`
- Require snapshot to be an instance of `ValuationSnapshot`
- Refuse planning otherwise

### 2. Credit Planning Rules
- Use `ValuationSnapshot.totalAcceptedValue()` as the sole credit source
- Treat zero-value acceptance as a refusal
- Do not inspect per-item valuation for planning

### 3. Credit Planning Component
- Likely artifact: `BalanceCreditPlanner`
- Inputs:
  - `ExchangeEvaluationResult`
  - Player identity (direct or derived)
- Output:
  - `BalanceCreditPlan` or explicit refusal
- No side effects

### 4. Non-Activating Wiring
- Allow callers to request planning
- Do not enable mutation execution
- Preserve Phase 3D.2 and 3D.3 behavior

---

## Open Questions (To Be Resolved)

- Should zero-value PASS ever produce a credit plan?
- Should planning refusal be distinguishable from Core denial?
- Should partial acceptance be surfaced later for UI only?

These must be resolved by inspection and testing, not assumption.

---

## Success Criteria (Planning-Level)

Phase 3D.4 will be considered successful when:

- A `BalanceCreditPlan` can be produced deterministically
- All refusal cases are explicit and non-punitive
- No future execution depends on implicit value
- All assumptions in this document are validated or corrected

Only then may a **final phase chronicle** be written.

---

## Non-Binding Nature

This document:
- may be annotated
- may be corrected
- may be partially invalidated

It exists to **guide**, not to legislate.

All binding truth for Phase 3D.4 will live in the final chronicle, written after a successful build.

---

End of planning chronicle.
