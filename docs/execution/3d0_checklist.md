# PHASE 3D.0 — MUTATION & CONFIRMATION  
## DESIGN CHECKLIST (WORKING DOCUMENT)

**Status:** Draft  
**Scope:** Adapter responsibility only  
**Nature:** Design checklist (non-binding, non-law)  
**May evolve:** Yes

---

## 1. CORE INVARIANTS (NON-NEGOTIABLE)

These must remain true regardless of implementation details.

- ☐ Mutation never occurs during evaluation
- ☐ Mutation never occurs without Core PASS
- ☐ Mutation never occurs without explicit player confirmation
- ☐ Confirmed payload must exactly match evaluated payload
- ☐ Any mismatch invalidates confirmation
- ☐ Mutation is atomic (all-or-nothing)
- ☐ Rollback must restore pre-mutation state
- ☐ Audit must reflect the final outcome truthfully

If any of these cannot be satisfied, **mutation must not proceed**.

---

## 2. CONFIRMATION — SEMANTIC REQUIREMENTS

Confirmation is **consent**, not validation.

- ☐ Confirmation does not re-run Core evaluation
- ☐ Confirmation does not re-run policy layers
- ☐ Confirmation does not re-run valuation
- ☐ Confirmation does not infer or adjust meaning
- ☐ Confirmation exists solely to express:  
  *“I understand and accept this outcome.”*

### Confirmation Payload Rules

- ☐ Confirmation references a specific evaluated result
- ☐ Confirmation is invalidated if:
  - inventory changes,
  - valuation configuration changes,
  - or context changes materially
- ☐ Confirmation timeout behavior (if any) must be explicit

---

## 3. PARTIAL ACCEPTANCE HANDLING

The system prefers honesty over all-or-nothing behavior.

- ☐ Mixed outcomes (accepted + denied items) are allowed
- ☐ Accepted items may proceed without penalizing denied items
- ☐ Denied items remain untouched
- ☐ Player must be informed what *will* happen before confirming
- ☐ Player is not required to understand *why* an item was denied to proceed

Partial acceptance is **a feature**, not an error.

---

## 4. MUTATION EXECUTION (ADAPTER SIDE)

Mutation occurs only after confirmation.

### Required Properties

- ☐ Inventory removal and balance credit are treated as a single operation
- ☐ If any mutation step fails:
  - all prior steps are rolled back
- ☐ No mutation retries without a fresh evaluation
- ☐ Mutation code must be isolated from evaluation code

### Explicit Non-Goals

- ☐ No “best effort” mutation
- ☐ No partial mutation
- ☐ No silent correction

---

## 5. FAILURE & RECOVERY SEMANTICS

Failures are possible even after PASS.

### Player-Facing Behavior

- ☐ Player is informed calmly
- ☐ Message is generic and non-punitive
- ☐ Player is never blamed
- ☐ Player is never left guessing whether something changed

Example intent (not wording):

> “Nothing was changed.”

### System Behavior

- ☐ Rollback is guaranteed
- ☐ Audit records the failure
- ☐ System remains consistent

---

## 6. AUDIT REQUIREMENTS (PHASE 3D BASELINE)

For Phase 3D, auditing should be simple and complete.

- ☐ One audit record per exchange attempt
- ☐ Audit includes:
  - evaluation result
  - confirmation status
  - mutation attempted (yes/no)
  - final outcome (success / rollback)
- ☐ No audit suppression
- ☐ Audit remains machine-facing

Granularity may increase in future phases.

---

## 7. PLAYER-FACING DECORATION (AWARENESS ONLY)

Not implemented here, but must not be blocked.

- ☐ Canonical reasons preserved internally
- ☐ Adapter may decorate outputs for humans
- ☐ Decoration does not alter outcomes
- ☐ Decoration does not invent meaning

---

## 8. OPEN QUESTIONS (INTENTIONALLY DEFERRED)

These are not required to solve now.

- ☐ Exact confirmation UX (command vs GUI vs hybrid)
- ☐ Timeout rules for confirmation
- ☐ Multi-step mutation visualization
- ☐ Advanced audit visualization
- ☐ Localization strategy

Leaving these open is **intentional and safe**.

---

## 9. EXIT CRITERIA FOR PHASE 3D DESIGN

Phase 3D design is considered “ready” when:

- ☐ All invariants are agreed upon
- ☐ No checklist item contradicts Core law
- ☐ Mutation can be implemented without guessing semantics
- ☐ Future contributors can understand *why* constraints exist

---

## CLOSING NOTE

Phase 3D is the moment where **truth becomes action**.

The purpose of this checklist is not to force answers early,
but to ensure that when answers arrive,
they do not violate honesty, trust, or law.

---

END OF CHECKLIST
