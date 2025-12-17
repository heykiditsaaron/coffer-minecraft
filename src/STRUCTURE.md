# STRUCTURE — AUTHORITY & DEPENDENCY DECLARATION

This document declares the **structural law** of the Coffer repository.

It exists to encode **authority, reuse, and dependency direction** directly into the filesystem, so that correct behavior survives context loss, collaborator turnover, and time.

This document is **declarative**, not instructional.

---

## PURPOSE

Coffer relies on **structure as authority**.

Meaning is not inferred from code patterns, comments, or conventions alone — it is enforced by **where code is allowed to live**.

If logic appears in the wrong location, it is incorrect by definition, even if it compiles.

---

## CANONICAL SOURCE LAYOUT

All executable code lives under `/src`.

The canonical structure is:

- `/src/common/core`
- `/src/common/platform`
- `/src/adapter-fabric`
- `/src/adapter-neoforge`
- `/src/adapter-bukkit`

No other top-level source directories are permitted.

---

## AUTHORITY BY LOCATION

### `/src/common/core`

This directory is **sovereign**.

It is the sole authority for:
- policy ordering
- valuation
- mutation rules
- denial semantics
- audit semantics

No adapter or platform code may redefine or reinterpret meaning established here.

---

### `/src/common/platform`

This directory defines **abstraction seams** between Core and adapters.

It may contain:
- interfaces
- bridge types
- capability descriptions

It must not:
- define policy
- perform valuation
- mutate state
- emit denials

---

### `/src/adapter-*`

Adapter directories exist solely to **translate platform reality into Core calls**.

They may:
- gather inputs
- invoke Core
- express results in platform-appropriate ways

They must not:
- define policy
- assign value
- short-circuit Core
- invent denial reasons
- mutate state outside Core-approved paths

---

## DEPENDENCY DIRECTION (BINDING)

Dependencies must flow **in one direction only**:

Adapters  
→ Platform Abstractions  
→ Core

Reverse dependencies are prohibited.

If a dependency violates this direction, it is a **semantic error**, regardless of tooling support.

---

## ENFORCEMENT NOTE

At this phase, dependency direction is not enforced by build tooling.

This is intentional.

Structural violations are considered **design failures**, not tooling failures, and must be corrected by moving code — not by justifying exceptions.

---

## FINAL STATEMENT

This structure is not a suggestion.

It is the mechanism by which:
- authority is preserved
- reuse is enforced
- reinvention is discouraged
- intent survives relay-style collaboration

If a future change requires altering this structure, it must be accompanied by a new Phase Chronicle explaining **why the structure itself failed**.

Until then, this structure stands.
