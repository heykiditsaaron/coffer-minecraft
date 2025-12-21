# COFFER FABRIC ADAPTER — CONSISTENCY CHARTER

**Purpose:**  
Ensure cognitive clarity, structural honesty, and intuitive flow during consolidation of the Fabric adapter.

This charter is **binding** for cleanup work.  
If code violates this charter, it is considered incorrect **even if it functions**.

---

## 1. CORE MENTAL MODEL (FOUNDATIONAL)

Every adapter component must belong to **exactly one** layer, in this order:

    ENTRY (commands, UI, integrations)
      ↓
    DECLARATION (truth construction + refusal)
      ↓
    EVALUATION (Core boundary, opaque)
      ↓
    PLANNING (freeze mutation intent)
      ↓
    EXECUTION (atomic mutation + rollback)

If a class spans multiple layers, it must be split or simplified.

---

## 2. NAMING RULES

### 2.1 Names Must Encode Responsibility

A reader should infer what a class is *allowed* to do from its name.

**Allowed verbs by layer:**

- ENTRY: `Command`, `Handler`, `Router`
- DECLARATION: `Builder`, `Constructor`, `Declaration`
- EVALUATION: `Translator`, `Executor` (Core-facing only)
- PLANNING: `Plan`, `Context`, `Planner`
- EXECUTION: `Step`, `Transaction`, `Executor`

Names that mix layers (e.g. `ExecutionPlanner`, `DeclarationExecutor`) violate this charter.

---

### 2.2 “Executor” Is Reserved

`Executor` may only:
- invoke Core, or
- apply real-world mutation

It must **never**:
- decide truth
- infer intent
- perform planning

---

### 2.3 “Context” Is Passive

A `Context`:
- contains frozen data
- has no side effects
- has no branching logic
- exists to prevent reconstruction

If it performs behavior, it is not a Context.

---

## 3. PACKAGE RESPONSIBILITY RULES

### 3.1 `command`

- Entry only
- No Core calls
- No mutation
- No planning
- No declaration construction (beyond invoking it)

Commands are *buttons*, not brains.

---

### 3.2 `declaration`

- Adapter-owned truth construction
- Explicit refusal before Core
- No Core imports
- No execution imports
- No mutation planning

Declaration may **observe**, **decide**, and **freeze facts** only.

---

### 3.3 `execution` (top-level)

Conceptually split into:
- Evaluation boundary
- Mutation planning
- Mutation execution

Code here must not:
- guess
- reinterpret Core meaning
- reach back into declaration

---

### 3.4 `execution.step`

- Single, reversible mutation actions
- Must support rollback
- Must not orchestrate
- Must not decide ordering

Steps are *mechanical*, not intelligent.

---

## 4. COMMENTING RULES (CRITICAL)

### 4.1 Comments Explain Purpose and Constraints

Allowed:
- What this file is responsible for
- What it explicitly does NOT do
- What invariants it enforces

Forbidden:
- Phase numbers
- Historical scaffolding
- “Temporary”, “for now”, “later”, “placeholder”

Temporary code should be removed, not explained.

---

### 4.2 Every File Must Answer Three Questions

At the top of each significant file:

1. What this file is responsible for
2. What it explicitly does NOT do
3. What invariants it enforces

If this cannot be stated simply, the file is over-scoped.

---

## 5. RESULT & ERROR MODEL CONSISTENCY

- Prefer boring result types
- No clever enums
- No inheritance trees
- Use small records or simple success/failure objects

Error reasons must describe:
- Which invariant failed
- Not who is at fault
- Not what the user did wrong

---

## 6. REMOVAL OVER ABSTRACTION

If code exists only to:
- preserve phase history
- document past scaffolding
- support unused paths

It should be **deleted**, not generalized.

Clean code beats complete code.

---

## 7. NEW CONTRIBUTOR TEST

After cleanup, a new contributor must be able to:

Trace `/sell` → declaration → evaluation → planning → execution

…without reading historical documentation or receiving oral explanation.

If oral history is required, the cleanup has failed.

---

## STATUS

This charter is **approved and binding** for consolidation work.
