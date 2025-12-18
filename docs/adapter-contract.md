# ADAPTER CONTRACT — CORE ↔ PLATFORM
## BINDING AND AUTHORITATIVE
Version: 1.3

---

## PURPOSE

This document defines the **binding contract** between the Coffer Core Engine
and any platform adapter (Fabric, NeoForge, Bukkit/Spigot, or otherwise).

The contract exists to:
- preserve Core honesty,
- prevent semantic drift,
- eliminate implicit behavior,
- and protect future collaborators from inheritance ambiguity.

Adapters that do not conform to this contract are **invalid by definition**.

---

## CORE SOVEREIGNTY

The Core Engine is the sole authority on:
- evaluation flow,
- denial semantics,
- valuation acceptance,
- audit emission requirements.

Adapters MUST conform to Core meaning.
Adapters MUST NOT reinterpret Core results.

---

## ADAPTER OBLIGATIONS (MUST PROVIDE)

Adapters are responsible for supplying the Core with the following,
truthfully and without approximation:

### 1. ExchangeRequest Construction
Adapters MUST construct ExchangeRequest objects that truthfully represent:
- the acting identity (opaque to Core),
- the offered and/or requested items or value,
- the execution context (shop, bulk liquidation, command, etc.).

Adapters MUST NOT:
- normalize items,
- flatten identity,
- strip meaning,
- invent value.

---

### 1.1 Composite Truth / Aggregation Obligation

Adapters MUST NOT collapse mixed platform reality into an all-or-nothing declaration
for convenience.

When an invocation involves multiple independent elements (items, stacks, quantities,
metadata-bearing variants, permissions, or other platform facts), the adapter MUST:

- determine which elements can be declared truthfully,
- determine which elements cannot be declared truthfully,
- and refuse to invent or flatten that distinction.

If no truthful exchange can be declared, the adapter MUST refuse explicitly and MUST
NOT invoke Core evaluation.

This obligation is platform-agnostic.
It does not mandate any specific UX, batching model, or execution surface.
It exists to ensure adapters carry the burden of truthful aggregation so the Core
may remain singular, deterministic, and non-negotiating.

---

### 2. Policy Layer Answers
Adapters MUST provide policy layers that:
- evaluate context legitimacy,
- evaluate permissions,
- evaluate blacklists / allowlists,
- evaluate ownership and sufficiency.

Policy layers:
- MAY deny explicitly with a reason,
- MAY allow and exit,
- MUST NOT mutate state,
- MUST NOT infer downstream behavior,
- MUST NOT stack denials.

The first denial ends evaluation.

---

### 3. Valuation Data
Adapters MUST supply valuation data that is:
- explicit,
- auditable,
- and truthful.

Valuation MUST:
- return immutable valuation snapshots,
- report partial acceptance explicitly,
- reject zero or negative value,
- refuse valuation when truth cannot be established.

Adapters MUST NOT:
- approximate value,
- “best effort” valuation,
- silently ignore uncertainty.

---

### 3.1 Valuation Absence Invariant

Adapters MUST remain operational when no valuation rules are declared.

In the absence of declared valuation truth:
- All economic actions MUST be evaluated
- Valuation MUST reject all items
- Core MUST deny the exchange with `INVALID_VALUE`
- No default, inferred, or placeholder values may be applied

An undeclared economy is valid and intentionally inert.

---

### 4. Metadata / Item Identity Handling
Adapters MUST NOT assume item fungibility.

If item metadata (including NBT or equivalent) is present:
- and is undeclared or unknown → valuation MUST be denied,
- unless an explicit configuration declares that metadata irrelevant.

Ignoring metadata is permitted ONLY when:
- explicitly configured,
- auditable,
- scoped to valuation relevance.

Silent normalization or omission of metadata is forbidden.

---

### 5. Audit Sink Provision
Adapters MUST provide an AuditSink.

Adapters are responsible for:
- where audits go,
- how audits are stored or displayed.

Adapters MUST emit audits for:
- every exchange evaluation,
- every explicit mutation.

Audit emission MUST NOT be optional.

---

### 6. Explicit Mutation Execution
Adapters are responsible for executing mutation ONLY AFTER:
- Core evaluation returns PASS.

Mutation includes:
- inventory changes,
- balance credit/debit,
- item destruction or issuance.

Adapters MUST NOT:
- mutate during evaluation,
- partially mutate,
- retry mutation without re-evaluation.

Mutation is the final step. Always.

---

### 7. Player-Facing Output Decoration

Adapters MAY decorate player-facing output derived from Core evaluation results.

Decoration exists to:
- present canonical outcomes in humane, non-punitive language,
- reduce user confusion or anxiety,
- and preserve trust without weakening refusal.

Decoration MUST be:
- lossless (canonical reason codes are preserved internally),
- adapter-local (Core remains unaware),
- non-inferential (no new meaning introduced),
- and non-punitive in tone.

Decoration MUST NOT:
- alter or replace Core reason codes,
- suppress or hide denial,
- imply user fault or misuse,
- reinterpret PASS or DENY,
- or invent justification beyond the canonical reason.

Canonical reasons remain authoritative for:
- audits,
- logs,
- diagnostics,
- and future interfaces.

Decoration affects presentation only.
Judgment remains unchanged.

---

## CORE GUARANTEES (PROMISES)

In exchange, the Core guarantees:

1. Exactly one evaluation result per request.
2. Exactly one audit record per evaluation.
3. Deterministic evaluation order.
4. Explicit denial reasons.
5. No mutation during evaluation.
6. No platform assumptions.
7. No hidden behavior.
8. No silent acceptance.

PASS means “mutation is permitted”, not “mutation occurred”.

DENY is final and explicit.

---

## FORBIDDEN ADAPTER BEHAVIOR

Adapters MUST NOT:

- mutate state during evaluation,
- infer value the Core did not approve,
- bypass denial reasoning,
- reinterpret PASS or DENY,
- assume item equivalence,
- hide uncertainty,
- introduce side effects into policy layers,
- encode platform logic into Core types,
- flatten mixed reality into a single dishonest declaration.

Any such behavior is a contract violation.

---

## ENFORCEMENT RULE

From the moment this contract is ratified:

- All Phase 3 work MUST conform to this document.
- Existing Phase 3 work MAY be invalidated and corrected.
- No adapter code may proceed without review against this contract.

The contract is the boundary.
The boundary is the law.

---

## CLOSING STATEMENT

The Core exists to refuse dishonesty.

Adapters exist to translate reality into truth the Core can judge.

When truth cannot be established,
the system must refuse to proceed.

This is not a limitation.
It is the design.
