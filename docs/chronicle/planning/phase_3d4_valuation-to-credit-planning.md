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
  - Treated as opaque
  - Only explicitly exposed surfaces may be used
  - No inference or interpretation is permitted

### Adapter-Side
- `DeclaredExchangeRequest`
- `DeclaredItem`
- `ExchangeIntent`
- Metadata relevance declarations
- Installed `ValuationService`
- Installed policy layers

If any of these assumptions are invalid, this document must be annotated.

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
- Core PASS with no creditable value
- Core PASS with partial acceptance that cannot be planned safely
- Metadata relevance prevents valuation
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

Planning must be boring, explicit, and auditable.

---

## Proposed Internal Responsibilities (Tentative)

The following responsibilities are proposed for Phase 3D.4.  
They may change as implementation reveals facts.

### 1. Evaluation Surface Inspection
- Determine what numeric or structured value, if any, is available from `ExchangeEvaluationResult`
- Document what can be safely consumed
- Refuse planning if surfaces are insufficient

### 2. Credit Planning Rules
- Define adapter-owned rules for turning evaluation output into a numeric credit
- Rules must be explicit and deterministic
- Rules must not leak Core semantics

### 3. Credit Planning Component
- Likely artifact: `BalanceCreditPlanner`
- Inputs:
  - `ExchangeEvaluationResult`
  - `DeclaredExchangeRequest`
- Output:
  - `BalanceCreditPlan` or explicit refusal
- No side effects

### 4. Non-Activating Wiring
- Allow callers (e.g., sell command or future UI) to request planning
- Do not enable execution paths
- Preserve Phase 3D.2 and 3D.3 behavior

---

## Open Questions (To Be Resolved)

The following questions are intentionally unanswered:

- Does `ExchangeEvaluationResult` expose total value, per-item value, or neither?
- How are rejected items represented post-evaluation?
- Are zero-value PASS results valid?
- Are rounding or precision rules required?
- Should partial acceptance ever produce a credit plan?

These must be answered by inspection, not assumption.

---

## Success Criteria (Planning-Level)

Phase 3D.4 will be considered successful when:

- A `BalanceCreditPlan` can be produced deterministically when possible
- All failure cases are explicit and non-punitive
- No execution depends on implicit value
- No future UI must guess what will happen
- All assumptions made here are either validated or documented as incorrect

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
