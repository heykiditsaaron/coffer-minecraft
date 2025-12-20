# Phase 3D.4 — Valuation → Credit Planning

Status: **COMPLETE**  
Chronicle Scope: **Adapter-Side Credit Planning Semantics**

---

## Purpose

Phase 3D.4 finalizes the **adapter-side planning semantics** that transform
a Core evaluation result into an explicit, immutable balance credit intent.

This phase exists to answer one question definitively:

> *How does the adapter convert Core-approved value into a frozen credit plan
> without guessing, recomputation, or reinterpretation?*

Phase 3D.4 does **not** perform mutation.  
It prepares mutation truthfully.

---

## Preconditions (Inherited and Reaffirmed)

Phase 3D.4 operates under the following already-frozen guarantees:

- Core evaluation is authoritative for acceptance
- Core valuation is authoritative for value
- Adapter owns all mutation intent
- Execution is atomic and rollback-capable (Phase 3D.3)
- Guessing is forbidden
- Partial truth is forbidden
- Refusal is non-punitive and explicit

These guarantees remain unchanged.

---

## Discovery Outcome (Validated During This Phase)

Inspection of Core interfaces confirmed the following facts:

- All valuation is produced by `ValuationService`
- All valuation snapshots are instances of `ValuationSnapshot`
- `ValuationSnapshot` provides:
  - Accepted vs rejected item resolution
  - A deterministic numeric surface: `totalAcceptedValue`
- Core intentionally stores the snapshot opaquely in `ExchangeEvaluationResult`
- Core does not interpret or consume valuation after evaluation

This discovery enabled lawful credit planning **without violating opacity**.

---

## What Was Introduced

### 1. Explicit Credit Planning Result Model

- `BalanceCreditPlanningResult`
- `BalanceCreditPlanningRefusal`

These types represent the **honest outcome** of attempting to plan credit.

Properties:
- Planning failure is expected and non-punitive
- Refusal reasons are explicit and boring
- No exceptions are used to signal planning failure
- Safe for future UI and audit usage

---

### 2. Explicit Credit Planning Logic

- `BalanceCreditPlanner`

Responsibilities:
- Consume `ExchangeEvaluationResult`
- Require Core PASS
- Require a supported `ValuationSnapshot`
- Require at least one accepted item
- Extract `totalAcceptedValue` without recomputation
- Produce exactly one of:
  - `BalanceCreditPlan`
  - explicit planning refusal

This component:
- Performs no mutation
- Performs no execution
- Performs no persistence
- Introduces no dependencies
- Reinterprets no Core semantics

---

### 3. Reaffirmed Credit Intent Model

- `BalanceCreditPlan` (introduced earlier, now exercised)

Properties:
- Adapter-owned
- Immutable
- Explicit numeric credit
- Bound to a specific player
- Frozen before execution
- Opaque to Core

Phase 3D.4 establishes how this plan is constructed honestly.

---

## Failure Semantics

Planning refusal occurs explicitly when:

- Core evaluation is DENY
- Valuation snapshot is missing
- Valuation snapshot type is unsupported
- No items were accepted
- Total accepted value is zero

In all cases:
- No mutation occurs
- No adapter fault is raised
- No punishment is implied
- The system remains safe and explainable

---

## What This Phase Explicitly Does *Not* Do

Phase 3D.4 does **not**:

- Execute mutation
- Remove Phase 3D.2 or 3D.3 paths
- Introduce persistence
- Introduce UI
- Introduce rounding or currency semantics
- Inspect per-item valuation for execution
- Depend on external APIs or platform economics
- Change Core behavior or meaning

These are intentionally deferred.

---

## Completion Criteria

Phase 3D.4 is considered **complete** because:

- A deterministic, explicit credit planning path now exists
- All planning inputs are validated, not assumed
- All failure cases are explicit and non-punitive
- No execution path can invent or reinterpret value
- Future UI can truthfully say:
  > “This is exactly what will happen.”

The planning semantics defined here are **frozen**.

---

## Result

With Phase 3D.4 complete:

- The system absorbs the burden of valuation honesty
- Players are never surprised at execution
- Partial acceptance is handled without confusion
- Execution proceeds only from frozen, explicit intent
- Vigilance is unnecessary

Phase 3D.4 closes the valuation-to-execution gap.

---

**This concludes Phase 3D.4.**
