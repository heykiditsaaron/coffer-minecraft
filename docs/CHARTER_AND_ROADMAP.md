# COFFER — CANONICAL CHARTER & ROADMAP
*(Structure-First, Relay-Safe, Honesty-Preserving)*

---

## PURPOSE

Coffer is a **server-side, honesty-first commerce substrate** for Minecraft.

Its sole job is to reduce economic tedium by **refusing dishonest actions explicitly**, while making honest actions frictionless.

> **Exploits are permitted — merely unrewarding.**

---

## CANONICAL DOCUMENT LOCATIONS

All collaborators must know these locations before working.

**Law (Immutable Authority)**  
Location: `/docs/law`  
Defines meaning, invariants, and constraints. These documents are frozen and non-negotiable.

**Phase Chronicles (Immutable History)**  
Location: `/docs/chronicle`  
Records what was done, why it was done, what was excluded, and what to watch next.

**Source Code (Executable Truth)**  
Location: `/src`  
Authority is encoded by structure.

---

## GLOBAL GUARD RAILS (BINDING)

These rules apply to **all phases** and **all collaborators**.

### G-1. One Door In
All exchange attempts — UI, command, mod, adapter — must enter through a single engine entry point.  
No bypasses.

### G-2. Negative Authority
If a phase is not explicitly granted authority to define meaning, it must not do so.  
When unsure, refuse or escalate.

### G-3. Short-Circuit Law
Policy evaluation halts immediately on first denial.  
No stacking. No aggregation.

### G-4. Freeze the “Why”
Design decisions are frozen at the level of intent and rationale.  
Implementation may vary. Reasons may not.

### G-5. Vocabulary Lock
The following distinctions are canonical and must never be collapsed:
- Value ≠ Price
- Exchange ≠ Issuance
- Preview ≠ Promise
- Denial ≠ Failure
- Permission ≠ Shop Rule

### G-6. If Unsure, Say No
Any choice that risks dishonesty, ambiguity, or inference must refuse rather than approximate.

---

## PHASE 0 — LAW & CONSTITUTION (COMPLETE)

**Sovereignty**  
This phase is the sole authority on meaning.

**Location**  
`/docs/law`

**Defines (immutably):**
- Honesty-first doctrine
- Server as single source of truth
- Deny-only policy layering
- Central valuation
- No zero or negative value
- Mutation-last execution
- Explicit denials
- Auditable outcomes

**Non-Authority**
- No code
- No structure
- No adapters
- No UX

---

## PHASE 1 — STRUCTURE & SOVEREIGNTY DECLARATION
*(No logic, no features)*

**Purpose**  
Encode authority and reuse directly into structure so correct behavior survives relay collaboration.

### Canonical Project Layout

Rooted at `/src`:

- `/src/common/core` — policy, valuation, mutation, audit
- `/src/common/platform` — abstraction seams
- `/src/adapter-fabric` — Fabric translation only
- `/src/adapter-neoforge` — NeoForge translation only
- `/src/adapter-bukkit` — Bukkit / Spigot / Paper translation only

### Authority by Location
- Policy ordering lives only in `/src/common/core`
- Valuation lives only in `/src/common/core`
- Mutation lives only in `/src/common/core`
- Denial semantics live only in `/src/common/core`
- Adapters may call Core, never re-implement it

### Dependency Direction
Dependencies must flow in this order:

Adapters → Platform Abstractions → Core

Reverse dependencies are prohibited.

### Discoverability Litmus Test
A collaborator unfamiliar with the project must be able to identify where core logic belongs by folder name alone.

---

## PHASE 2 — CORE ENGINE IMPLEMENTATION
*(Platform-agnostic)*

**Sovereignty**  
Defines how decisions are made.

### Policy Engine
Canonical policy spine, evaluated in order:
1. Evaluation / Exchange Validity
2. Context Legitimacy
3. External Authority (Permissions)
4. Local Intent (Shop Rules)
5. Transaction Facts (Ownership & Sufficiency)

Rules:
- Short-circuit on first denial
- Exactly one denial
- Explicit reason with policy identifier
- No mutation

### Valuation Core
- Single-source base value registry
- Modifiers
- Currency definitions
- Precision handling
- Rejection of zero or negative results

Evaluation must fail rather than invent value.

### Issuance / Destruction (Non-Exchange)
- Explicit credit and debit paths
- Permission-gated
- Audited
- Never routed through exchange engine
- Enforces non-negative balances

### Audit Model
Canonical records for:
- Exchange success and denial
- Issuance and destruction
- Reload events

No UI concerns.

---

## PHASE 3 — CANONICAL ADAPTER: FABRIC

**Sovereignty**  
Expresses Core semantics. Does not invent meaning.  
Fabric is the truth reference.

### Fabric Bootstrap
- Server-side only
- Lifecycle wiring
- Denial during reload

### Commands
- `/sell`
- `/balance`
- Issuance and debit
- Reload and validation
- Audit inspection

Commands are alternate inputs, not alternate logic paths.

### Admin Shop UI (Diagnostic First)
Purpose: prove the core is correct.
- Inventory-based listing
- Per-listing modifiers
- Tooltip price shown only inside shop UI
- Permission-gated
- Explicit refusals
- Confirmation before mutation
- Full audit trail

No base value editing.

### Bulk Liquidation UI (Convergence)
- Inventory workspace
- Player-placed items
- Visual refusal markers
- Aggregate price preview (non-binding)
- Partial acceptance
- Explicit confirmation
- Mutation after confirmation only

### UX Feedback
Allowed:
- Boss bar
- Title
- Sounds (supplemental)

Disallowed:
- Scoreboard
- Always-on HUD
- Ambiguous indicators

---

## PHASE 4 — COMPATIBILITY ADAPTER: NEOFORGE

**Sovereignty**  
Parity confirmation only.
- Same engine
- Same policy order
- Same semantics
- Lifecycle differences only

---

## PHASE 5 — CONSTRAINT ADAPTER: BUKKIT / SPIGOT / PAPER

**Sovereignty**  
Proves honesty under constraint.
- Reduced UX acceptable
- Dishonest UX not acceptable
- Command-first where required
- Placeholder integration allowed

If something cannot be expressed honestly, it is omitted.

---

## PHASE 6 — HARDENING & MVP FREEZE

Includes:
- Abuse testing
- Reload safety testing
- Documentation

**MVP Cut Line (Frozen):**
- Admin shops
- Bulk liquidation
- Commands
- Issuance
- Auditing

---

## PHASE 7 — FUTURE (OUT OF SCOPE)

- Player shops
- Auctions
- Async settlement
- Exchange rates
- Premium UX polish

These must not influence earlier phases.

---

## PHASE CHRONICLES (REQUIRED)

**Location**  
`/docs/chronicle`

Exactly one Chronicle per phase, written after completion and immutable once published.

Each Chronicle records:
1. What was done
2. Why it was done this way
3. What was explicitly not done
4. What to watch for next

A phase is incomplete until its Chronicle exists.

---

## FINAL STATEMENT

This system is designed to survive collaborator turnover, loss of context, and the passage of time by anchoring:

- Meaning in law
- Location in structure
- Time in chronicles

**Freeze acknowledged.**
