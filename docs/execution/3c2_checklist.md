# Phase 3C.2 — Inventory-Backed Declaration Checklist

**Status:** Non-binding implementation checklist  
**Scope:** Fabric Adapter — Phase 3C.2  
**Authority:** Informed by Coffer Law, Charter, and Phase 3 Chronicle  
**Purpose:** Preserve hard-earned truth boundaries during implementation

---

## Purpose

This checklist exists to guide the implementation of **Phase 3C.2**:
**Inventory-Backed Declaration**.

It captures lessons learned during Phase 3 and formalizes the boundary
between **truth construction** and **truth evaluation**.

This document is:
- not Law,
- not a contract,
- not a design spec.

It is a **working checklist** intended to sit beside the keyboard while coding.

If future discovery reveals friction, this document should be **annotated**, not rewritten.

---

## Core Principle

> **An item is eligible for exchange only if it is actually owned by the player.**

Ownership — not visibility, not slot position, not UI interaction —
is the sole gate for declaration.

---

## Checklist

### 0. Scope Guard

☐ This phase constructs **truthful declarations only**  
☐ No valuation, policy, mutation, or UX behavior is introduced  
☐ If a task does not serve possession truth, it is out of scope  

---

### 1. Ownership Gate (Non-Negotiable)

☐ An item may be declared **only if** it is owned by the player  
☐ Ownership means:
- persistent, player-bound state
- survives UI close
- survives logout / reconnect  

☐ Visibility or interactivity does **not** imply ownership  

If ownership cannot be proven truthfully, the item is **not eligible**.

---

### 2. Inventory Inclusion Rule

☐ All player-owned inventories are included **by default**  

No inventory is excluded by:
- category
- slot position
- vanilla vs modded origin  

☐ A slot is excluded **only** if it no longer represents player possession  

Examples:
- control surfaces (confirm / cancel buttons)
- preview or ghost items
- result or output slots
- system-owned UI elements  

These are excluded because they are **not owned**, not because they are special.

---

### 3. Source of Truth Rule

☐ Declarations are constructed from **player state**, not **UI state**  

☐ Screen handlers, slot lists, and UI aggregation are not trusted sources of possession  

Rule of thumb:
> *If closing the UI would make the item disappear, it must not be declared.*

---

### 4. Possession Extraction

☐ For each owned item, extract raw possession facts only  

Each extraction includes:
- item identity (namespace + id)
- exact quantity
- full metadata / NBT (unmodified)

☐ No normalization  
☐ No relevance filtering  
☐ No valuation  

Metadata is **observed**, not interpreted, at this phase.

---

### 5. Aggregation (Strictly Limited)

☐ Aggregation is permitted **only** when:
- item identity matches exactly
- metadata matches exactly  

☐ Aggregation must be deterministic  

No fuzzy matching  
No “similar enough”  
No metadata stripping  

Aggregation must not invent meaning.

---

### 6. Declaration Construction

☐ `DeclaredItem` objects are built **only** from owned possession  
☐ `DeclaredExchangeRequest` is built **only if ≥1 item exists**  

☐ No declaration exists without possession  

If no owned items are eligible:
- do not construct a declaration
- do not invoke Core  

Absence is a correct and valid outcome.

---

### 7. Honest Refusal Paths

☐ Refusal occurs when:
- the player owns no eligible items
- ownership cannot be truthfully established  

☐ Refusal happens **before** Core invocation  

Refusal is not an error.  
It is an honest result.

---

### 8. UX Independence Guarantee

☐ Declaration logic is fully UX-agnostic  

No assumptions about:
- commands
- GUIs
- confirmation models
- screen layout
- slot semantics  

Future UX must not require refactoring this phase.

---

### 9. Invariants Verification

Before marking Phase 3C.2 complete, confirm:

☐ No item is declared unless owned  
☐ No UI element can become declarable by accident  
☐ No future confirmation mechanism is precluded  
☐ No valuation or policy meaning is introduced  
☐ No mutation occurs  

If any box cannot be checked, stop and revise.

---

### 10. Chronicle Requirement

☐ Update the Phase 3 chronicle to record:
- ownership vs UI friction encountered
- Fabric abstractions that tempted misuse
- why refusal was chosen over guessing
- how modded inventories remain compatible
- confirmation that no UX assumptions were made  

This preserves knowledge for future collaborators.

---

## Completion Criteria

Phase **3C.2** is complete when the adapter can truthfully answer:

> **“What does the player actually own?”**

And that answer is:
- exact
- deterministic
- auditable
- UX-independent

At that point: **stop**.

Do not extend.  
Do not optimize.  
Do not “improve”.

Truth has been constructed.

---

## Note on Authority

This checklist does not override:
- Coffer Law
- Coffer Charter
- Adapter Contract  

It exists to **apply them faithfully** during implementation.

If future reality introduces friction,
annotate this document rather than rewriting it.

That is how knowledge is passed forward without breaking authority.
