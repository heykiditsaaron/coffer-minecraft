NEW FILE (PLANNED): docs/chronicle/planning/phase_3d3_balance-mutation-credit.md

# PHASE 3D.3 — BALANCE MUTATION (CREDIT)
Status: PLANNING COMPLETE (Implementation Not Started)  
Chronicle Type: Planning Chronicle — Atomic Mutation Introduction  
Authority: Coffer Core Law & Adapter Contract (Unchanged)  
Scope: Fabric Adapter — Execution Layer Only  
Core: Unchanged (Sovereign)

---

## PURPOSE

Phase 3D.3 exists to introduce **balance mutation (credit)** such that:

> Inventory removal and balance credit together form a **single atomic economic action**.

This phase answers one question only:

> How can value be credited **only after** inventory removal succeeds, while guaranteeing rollback or safe halting under failure?

This phase is not about economic usefulness or polish.  
It is about **truthful atomic mutation**.

---

## FOUNDATIONAL PREMISE

Core approval is **not** a mutation plan.

Core verdict determines:
- what is permitted

Adapters own:
- how mutation is applied
- how rollback is guaranteed
- how platform state integrity is preserved

Mutation must be:
- explicitly planned
- bound to verified truth
- executed without reconstruction or inference

---

## SCOPE

### In Scope
- Balance credit after successful inventory removal
- Atomic orchestration of:
  1. inventory removal  
  2. balance credit
- Explicit rollback guarantees:
  - restore inventory if credit fails
  - revert credit if rollback is required
- Fail-closed behavior on any invariant breach
- Transition to an adapter fault state if rollback cannot be guaranteed

### Out of Scope (Explicit)
- Persistence (database, file-backed balances)
- Currency pluralism, exchange rates, denominations
- UI or UX changes (sell menu, confirmation surface, decoration)
- Policy changes or new judgment
- Async execution, retries, or background recovery
- Economic balance or usefulness

---

## ENTRY CONDITIONS

Phase 3D.3 implementation may proceed only if all of the following are true:

1. Core evaluation remains authoritative and unchanged
2. Execution boundary exists and is post-PASS
3. Mutation intent is adapter-owned and frozen (`MutationContext`)
4. No mutation occurs unless all binding invariants pass
5. Any existing balance logic is treated as temporary and replaceable

No re-evaluation is permitted during mutation.

---

## EXISTING ARTIFACTS (OBSERVED)

The following artifacts are treated as **frozen inputs**:

- `MutationContext`  
  Adapter-owned, immutable plan of intended inventory removals

- `FabricMutationExecutor`  
  Execution boundary enforcing binding invariants

- `InMemoryBalanceStore`  
  Adapter-owned, non-persistent balance mutation primitive  
  (explicitly temporary and non-authoritative)

No long-term currency system is assumed.

---

## REQUIRED NEW CONCEPTS (INTRODUCED BY THIS PHASE)

### 1. Balance Credit Plan
An adapter-owned, immutable planning artifact representing **exact credit intent**.

Minimum conceptual contents:
- target player UUID
- aggregate credit amount
- currency identifier only if one already exists explicitly

Rules:
- constructed before execution
- derived from already-established truth
- never recomputed inside execution
- contains no behavior

---

### 2. Atomic Mutation Transaction
An adapter-owned orchestrator coordinating mutation steps as a single unit.

Responsibilities:
- execute steps in strict order
- track which steps have applied
- rollback completed steps if a later step fails
- halt safely on irrecoverable failure

The transaction owns sequencing and rollback — not meaning.

---

### 3. Mutation Steps (Composable)
For Phase 3D.3, exactly two steps exist:

#### Step A — Inventory Removal
- Applies only the removals declared in `MutationContext`
- Records sufficient information to restore items during rollback

#### Step B — Balance Credit
- Credits the exact planned amount to the target account
- Supports rollback by applying the inverse delta

No step:
- calls Core
- re-derives intent from evaluation output
- assumes undeclared platform truth
- mutates outside its own responsibility

---

## CANONICAL EXECUTION ORDER

The only lawful order is:

1. Binding invariants verified at execution boundary
2. Inventory removal applied
3. Balance credit applied
4. If balance credit fails:
   - rollback inventory removal
5. If rollback fails:
   - enter adapter fault state
   - refuse further economic execution until review

PASS indicates permission, not completion.  
Mutation occurs only inside the transaction.

---

## FAILURE & HALT RULES

### Binding Failure
- No mutation occurs
- Execution halts safely
- Adapter-level refusal or audit may occur

### Apply Failure
- All prior successful steps are rolled back
- No partial success is allowed

### Rollback Failure (Terminal)
If rollback cannot be completed truthfully:
- adapter enters a faulted state
- all further economic execution is refused
- the condition must be surfaced for administrative review
- no silent recovery or approximation is permitted

This behavior is intentional.

---

## AUDIT EXPECTATIONS

This phase must ensure that audit output can reflect:
- attempted atomic exchange
- success
- denial
- apply failure
- rollback failure
- entry into fault state

Audit structure (single vs per-step) must be explicit and consistent.
No audit may imply mutation occurred when it did not.

---

## NON-PERSISTENCE DISCLOSURE

Phase 3D.3 does not introduce persistence.

If an in-memory store is used:
- balances reset on restart
- this behavior must not be hidden
- no UX may imply permanence

Persistence is deferred intentionally.

---

## COMPLETION CONDITIONS

Phase 3D.3 is complete when:

1. Balance credit occurs only after successful inventory removal
2. Inventory removal and balance credit behave atomically
3. No mutation occurs under binding mismatch
4. Apply failure triggers deterministic rollback
5. Rollback failure enters a faulted state and refuses execution
6. Mutation intent is never reconstructed during execution
7. Core remains unchanged

---

## TRANSITION NOTE

Phase 3D.3 introduces atomicity across inventory and balance mutation.

Phase 3D.4 may extend:
- failure classification
- rollback hardening
- fault inspection and recovery surfaces

Phase 3D.3 must remain minimal, explicit, and correct.

END OF PLANNING CHRONICLE
