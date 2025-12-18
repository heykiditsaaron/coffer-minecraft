# PHASE 3C.3 — CONFIGURATION-BACKED VALUATION (CHRONICLE)

STATUS: COMPLETE  
AUTHORITY: Coffer Core Law (unchanged)  
SCOPE: FABRIC ADAPTER — VALUATION TRUTH  
RELATED PHASES: 3C.1, 3C.2

---

## PURPOSE

Phase 3C.3 establishes **explicit, configuration-backed valuation** in the Fabric adapter.

This phase replaces placeholder valuation logic with an **honest, deny-by-default valuation model**, proving that:

- items have **no economic meaning** unless explicitly granted,
- absence of configuration is a valid and preferred state,
- and the adapter bears the burden of supplying valuation truth to Core.

This phase intentionally preserves evaluation-only behavior.
No mutation occurs here.

---

## WHAT WAS IMPLEMENTED

The following adapter-side capabilities were introduced:

1. **Adapter-local valuation configuration**
   - Item values are defined explicitly by administrators.
   - Unlisted items have no value.
   - Values less than or equal to zero deny participation.

2. **Explicit deny-by-default behavior**
   - Any item without a positive configured value is rejected.
   - Rejection uses canonical Core reason codes (`INVALID_VALUE`).

3. **Lawful Core integration**
   - The adapter supplies valuation results exclusively through
     Core-provided factory methods.
   - The adapter does not construct valuation semantics directly.

4. **Aggregate valuation semantics**
   - Total value is computed as:
     ```
     unit_value × quantity
     ```
   - Core receives aggregate value, not inferred unit meaning.

5. **Zero-configuration boot invariant preserved**
   - The system boots and runs with no valuation configured.
   - In this state, all valuation is denied explicitly and audibly.

---

## OBSERVED BEHAVIOR (VERIFIED)

When running `/sell` with any item and no valuation configured:

- The Core evaluates the request.
- Valuation denies all items with reason `INVALID_VALUE`.
- No mutation occurs.
- The system remains stable and auditable.

This behavior is **intentional** and confirms the honesty invariant:
absence is valid, refusal is preferred over guessing.

---

## DISCOVERIES DURING INTEGRATION

Several critical truths surfaced **during implementation**, not beforehand.
These are recorded here as witnessed learning, not retroactive intent.

### 1. Core Enforces Valuation Construction Authority

`ValuationItemResult` constructors are private.

Adapters **cannot** fabricate valuation outcomes.
They must use Core-provided factory methods:

- `accepted(item, quantity, totalValue)`
- `rejected(item, quantity, denialReason)`

This enforcement guarantees:
- centralized invariants,
- consistent semantics,
- and audit-safe outcomes.

### 2. Aggregate Value Is Canonical

Core valuation operates on **aggregate value**, not unit price.

Adapters are responsible for:
- computing total value honestly,
- supplying quantity explicitly,
- and never implying unit semantics beyond configuration.

### 3. Adapters Supply Truth, Core Judges It

This phase clarified a key architectural boundary:

> **The Core judges truth.  
> Adapters construct truth.**

Adapters do heavy lifting.
Core does not guess, infer, or reinterpret.

### 4. Player-Facing Language Is a Separate Concern

Canonical denial reasons (e.g., `INVALID_VALUE`) surfaced directly to players
during testing.

This revealed—not a flaw—but a **future adapter responsibility**:
player-facing decoration must be handled at the adapter/UI layer,
without altering canonical reasons.

No changes were made in this phase.
This discovery is recorded for Phase 3D and beyond.

---

## NON-GOALS (CONFIRMED)

Phase 3C.3 does **not**:

- introduce defaults,
- mutate inventory or balances,
- soften or decorate denial reasons,
- interpret player intent,
- or modify Core law or semantics.

All such behavior remains out of scope.

---

## PHASE OUTCOME

Phase 3C.3 successfully proves that:

- valuation truth can be fully explicit,
- denial can be honest and calm,
- absence can be first-class,
- and the system remains lawful under friction.

This phase completes the **valuation truth layer** of Phase 3C.

The system is now ready to design **mutation and confirmation**
without ambiguity or hidden assumptions.

---

## CARRY-FORWARD NOTES

- Chronicles should accompany PRs completing phases.
- When discovery occurs during integration, it must be witnessed explicitly.
- Player-facing decoration is allowed but must be lossless and adapter-local.
- No future phase should weaken deny-by-default valuation.

---

END OF CHRONICLE
