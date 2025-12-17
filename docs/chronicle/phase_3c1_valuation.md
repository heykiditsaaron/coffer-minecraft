# CHRONICLE — PHASE 3C.1 (FABRIC)
## REAL VALUATION (MINIMAL, HONEST) — EVALUATION ONLY

STATUS: COMPLETE (EVALUATION TRUTH PROVEN)  
SCOPE: FABRIC ADAPTER ONLY  
CORE: UNCHANGED (SOVEREIGN)  
CONTRACT: ENFORCED  

---

## PURPOSE

Phase 3C.1 replaces valuation pretending with valuation truth.

This phase proves:
- the adapter can construct a declared exchange request,
- Core can evaluate it,
- valuation can accept or reject items honestly,
- metadata relevance is enforced explicitly,
- and no mutation occurs as a result of evaluation.

This phase does NOT attempt to prove inventory truth or real selling.

---

## BINDING CONSTRAINTS (ENFORCED HERE)

### Valuation Absence Invariant (Adapter Contract v1.1)
If no valuation rules are declared, the system MUST remain operational and MUST reject all economic actions with `INVALID_VALUE`. No default or inferred values may be applied.

Phase 3C.1 preserves this invariant by ensuring valuation remains deny-first unless explicit truth exists.

### Metadata / Item Identity Handling
Metadata is treated as information. It MUST NOT be silently ignored.

Valuation is permitted ONLY when metadata relevance is explicitly declared as ignored. Any other metadata relevance state is rejected.

This phase does not parse or interpret NBT; it only enforces explicit declaration boundaries.

---

## WHAT WAS IMPLEMENTED

### 1) Honest Valuation (Hard-Coded, Self-Confessing)
The Fabric adapter valuation implementation is intentionally hard-coded and non-authoritative.

Hard-coded truth in Phase 3C.1:
- Only `minecraft:dirt` is accepted
- Unit value: 1
- All other items are rejected with `DenialReason.INVALID_VALUE`
- Items are rejected unless metadata relevance is explicitly declared as ignored

This implementation exists to prove valuation structure and denial honesty without introducing configuration or persistence.

### 2) Sell Command is Evaluation-Only (NO MUTATION)
The `/sell` command executes Core evaluation and reports results, but performs NO balance mutation and NO inventory mutation.

This was a deliberate correction: Phase 3B mutation scaffolding (“money for nothing”) was removed to preserve honesty.

---

## CRITICAL TRUTH DISCLOSURE (DO NOT MISREAD)

### The current `/sell` exchange declaration is NOT inventory-backed.
In Phase 3C.1, `/sell` constructs a declared exchange request using a placeholder declared item:

- Declared item: `minecraft:dirt` x1
- Metadata relevance: explicitly ignored

This declaration is NOT derived from the player’s actual inventory.

Therefore:
- Core PASS does NOT mean the player actually possessed dirt
- The adapter is NOT yet proving real-world sufficiency, ownership, or inventory truth

This is intentional and is the boundary between Phase 3C.1 and Phase 3C.2.

### Why a PASS can occur even if the player has no dirt
Core evaluates only what is declared.
Core is opaque to platform reality and cannot inspect Fabric inventories.

If the adapter declares “1 dirt offered,” valuation can accept that declaration and Core can return PASS.

This is NOT an exploit in Phase 3C.1 because:
- No mutation occurs
- No balance changes occur
- No items are removed or granted

Phase 3C.1 is proving valuation honesty, not sell truth.

---

## OBSERVED RUNTIME BEHAVIOR (EXPECTED)

Server audit output may include:
- `[Coffer][Audit] allowed=true reason=null`

This indicates Core returned PASS for the declared request. This is expected given the placeholder declaration.

In-game output may include:
- `[Coffer] Sell evaluated successfully`

This indicates evaluation succeeded without mutation.

---

## WHAT THIS PHASE DOES NOT DO (DEFERRED INTENTIONALLY)

Phase 3C.1 does NOT:
- read player inventory
- prove the player possesses offered items
- enforce sufficiency or ownership from real platform state
- open a bulk liquidation screen
- perform balance mutation
- perform inventory mutation
- implement configuration-backed valuation
- implement system-wide metadata ignore configuration
- implement NBT-aware valuation or policy ordering

---

## WHAT MUST HAPPEN NEXT (PHASE 3C.2 REQUIREMENT)

Phase 3C.2 must replace placeholder declaration with truthful, inventory-backed declaration.

Minimum requirement for Phase 3C.2:
- Adapter must only declare items the player actually possesses
- An empty or meaningless sell attempt must not be evaluated as a real exchange
- The declared exchange request must become a truthful translation of platform reality

Until Phase 3C.2 is complete, `/sell` MUST be treated as an evaluation harness, not a real selling mechanic.

---

## COMPLETION STATEMENT

Phase 3C.1 is complete when:
- valuation is honest and deny-first,
- metadata relevance is explicitly enforced,
- evaluation produces auditable PASS/DENY outcomes,
- and mutation is impossible.

These conditions are met.

The system is operational, truthful, and intentionally incomplete in declared exchange realism until Phase 3C.2.
