# COFFER FABRIC ADAPTER — CONSOLIDATION MAP

**Purpose:**  
Provide an explicit, auditable plan for consolidating the Fabric adapter into a clean,
concise, intuitive, and law-aligned codebase.

This document does **not** implement changes.  
It defines **what will be changed and why**, under the approved Consistency Charter.

---

## SCOPE

This consolidation map is derived from:

- Full architectural and code audit (Segments 1–5)
- Approved **Consistency Charter**
- Coffer Core Law and Concept Documentation
- Stated project goal: *clarity, honesty, and intuitive structure*

No new features are introduced.
No behavior changes are implied.

---

## SECTION A — FILES THAT ARE ALREADY CORRECT (PRESERVE)

These files are conceptually correct, well-scoped, and aligned with the Law.
They may receive **comment cleanup only** (removal of phase references, clarification of purpose).

### Boundary / Declaration Models

Preserve structure and behavior:

- `boundary/DeclaredExchangeRequest`
- `boundary/DeclaredItem`
- `boundary/DeclaredIdentity`
- `boundary/ExchangeIntent`
- `boundary/InvocationContext`
- `boundary/InvokerKind`
- `boundary/MetadataRelevance`

**Rationale:**  
These define adapter-owned truth envelopes cleanly and without inference.
They are boring, explicit, and correct.

---

### Evaluation Boundary

Preserve structure and behavior:

- `execution/FabricCoreExecutor`
- `execution/FabricToCoreTranslator`

**Rationale:**  
The adapter → Core boundary is minimal, opaque, and sovereign.
No consolidation should weaken or decorate this boundary.

---

### Mutation Planning Core

Preserve structure and behavior:

- `execution/MutationContext`

**Rationale:**  
This file correctly freezes mutation intent, enforces identity binding,
and prevents reconstruction. It is a foundational invariant.

---

## SECTION B — FILES REQUIRING RENAMING OR RE-SCOPING (NO LOGIC CHANGE)

These files are functionally correct but contribute to cognitive inconsistency
due to naming or responsibility compression.

### Inventory Declaration Construction

Current file:
- `declaration/InventoryDeclarationBuilder`

Issues:
- Combines inventory observation with declaration decision
- Mixes fact gathering and policy application
- Reads as a “do everything” entry point

Target conceptual shape (not yet implemented):

- Inventory observation (facts only)
- Declaration construction (policy + refusal)

**Rationale:**  
Declaration must read as a pipeline:
observe → decide → declare.
Even if kept in one file initially, responsibilities must be clearly separated.

---

### Mutation Execution Entry Point

Current file:
- `execution/FabricMutationExecutor`

Issues:
- Multiple execution paths
- Legacy scaffolding retained
- Overloaded responsibility (orchestration + results + gating)

Future direction (conceptual only):
- Narrow responsibility to execution entry point
- Push orchestration meaning into transaction logic
- Remove historical “no-op” execution paths

**Rationale:**  
Executors should feel decisive and narrow, not historical or experimental.

---

## SECTION C — FILES TO BE MERGED OR SIMPLIFIED

### Execution Result Modeling

Currently distributed across:
- `InventoryRemovalStep.ApplyResult`
- `BalanceCreditStep.ApplyResult`
- `MutationTransaction.Result`
- `FabricMutationExecutor.ExecutionResult`

Issues:
- Cognitive overhead
- Redundant success/failure modeling
- Error handling spread across layers

Recommended direction:
- Reduce to one or two simple, boring result shapes
- Avoid nested result records unless strictly necessary

**Rationale:**  
Results should communicate invariant failure, not architectural layering.

---

## SECTION D — FILES / CODE TO BE REMOVED AFTER CONSOLIDATION

These elements exist only to preserve phase history or scaffolding
and should not survive cleanup.

Candidates include:
- “Execute but do nothing” paths
- Legacy overloads
- Phase-specific comments or markers
- Comments referencing:
  - phases
  - pillars
  - “temporary”
  - “for now”
  - future promises

**Rationale:**  
The Consistency Charter forbids historical scaffolding.
If code is no longer needed, it must be removed, not explained.

---

## SECTION E — COMMAND SURFACE CLEANUP (LATER PASS)

Commands are largely correct but benefit from consistency refinement.

Targets:
- Harmonize registrar naming
- Clarify exploratory vs confirmatory intent
- Remove phase references
- Emphasize non-authoritative role of commands

Files include:
- `CofferCommandRegistrar`
- `SellCommandRegistrar`
- `ShopCommandRegistrar`

**Rationale:**  
Commands are entry points. They must feel calm, boring, and honest.

---

## SECTION F — COMMENT REWRITE PASS (GLOBAL)

Across the entire adapter:

### Remove:
- Phase numbers
- Historical context
- “Temporary” explanations
- Future promises

### Add:
- Clear responsibility statements
- Explicit non-responsibilities
- Enforced invariants

Every significant file must answer:
1. What this file is responsible for
2. What it explicitly does NOT do
3. What invariants it enforces

**Rationale:**  
Comments should explain purpose and constraints, not project history.

---

## SECTION G — TARGET OUTCOME

After consolidation, the Fabric adapter should:

- Read as a linear pipeline, not a maze
- Feel like a single, unified design
- Be understandable without oral history
- Enforce honesty structurally, not by convention
- Support `/sell` as the primary feature without conceptual debt

No correctness is lost.  
Only clarity is gained.

---

## STATUS

This consolidation map is **approved for execution** under the Consistency Charter.

No changes should be applied outside this plan.
