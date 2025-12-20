# GLOSSARY

## Purpose and Scope

This glossary exists to **explain terminology as it is used within the Coffer project**.

It is:
- explanatory, not authoritative,
- descriptive, not prescriptive,
- a map for readers, not a source of law.

Terms are listed **alphabetically** for ease of reference.
Ordering does **not** imply priority, dependency, evaluation order, or authority.

When conflict exists, **Coffer Core Law and binding contracts take precedence**.

---

## Adapter

A platform-specific implementation responsible for translating real-world or platform reality
into truthful declarations the Core can evaluate.

Adapters:
- construct truth,
- perform no judgment,
- execute mutation only after Core approval,
- and carry the burden of platform complexity.

Adapters are the sole owners of execution and presentation concerns.

---

## Annotation

A non-authoritative addition to documentation that records
discoveries, friction, or lessons learned **without rewriting history**.

Annotations:
- do not change law,
- do not invalidate prior intent,
- and exist to pass clarity forward honestly.

---

## Audit

A canonical, immutable record emitted for each exchange evaluation
and each mutation attempt.

Audits are machine-facing and authoritative.
They exist independently of player-facing output or UI.

---

## Audit Sink

An adapter-provided destination for audit records.

Examples include logs, files, databases, or future UIs.
The Core emits audits; adapters decide where they go.

---

## Chronicle

A witnessed record of what was built, learned, or discovered
during a specific phase or sub-phase.

Chronicles:
- record reality as it occurred,
- may include discoveries surfaced during integration,
- do not rewrite Core law or prior chronicles.

Chronicles preserve institutional memory.

---

## Core

The platform-agnostic engine that evaluates exchange requests
according to immutable law.

The Core:
- judges truth,
- issues canonical decisions,
- emits audits,
- and never mutates state directly.

---

## Decoration

Adapter-local presentation of canonical system outcomes
in language intended for human users.

Decoration:
- preserves underlying reason codes,
- affects presentation only,
- reduces confusion or anxiety,
- and must not introduce new meaning.

Decoration is **not** reinterpretation, suppression, or judgment.
It exists solely to render outcomes humane without weakening truth.

---

## Denial Reason

A canonical, machine-facing explanation for why an exchange was refused.

Denial reasons are authoritative and auditable.
They are not required to be human-friendly.

Adapters may decorate denial reasons for player-facing output,
but must preserve the canonical reason internally.

---

## Evaluation

The Core process of determining whether a declared exchange
is permitted under law.

Evaluation:
- produces exactly one result,
- never mutates state,
- and always terminates explicitly.

---

## Exchange

A declared intent to trade, sell, buy, or otherwise transform
items or value.

An exchange is evaluated by the Core
and may or may not be permitted to mutate state.

---

## Exchange Request

The opaque object submitted to the Core for evaluation.

It contains:
- actor identity (opaque),
- context,
- declared payload.

The Core does not interpret platform meaning inside the request.

---

### Intent Confirmation

An **Intent Confirmation** is a server-owned, non-mutating representation of a
specific, fully evaluated outcome that *could* occur if execution were to proceed.

It exists to allow a player to knowingly consent to an exact result **without
committing mutation**.

An Intent Confirmation:
- is derived solely from authoritative server state,
- describes exactly what would happen if executed,
- may be invalidated at any time if truth changes,
- does not guarantee execution,
- and does not authorize mutation on its own.

Intent Confirmation is agreement to outcome, not agreement to rules.

---

## Mutation

A real-world change resulting from a permitted exchange.

Examples include:
- inventory modification,
- balance updates,
- item issuance or removal.

Mutation occurs **only after** Core approval
and is executed by the adapter.

---

## Policy Layer

A Core-integrated mechanism that evaluates contextual legitimacy,
such as permissions or constraints.

Policy layers:
- may allow or deny,
- must not mutate,
- and terminate evaluation on first denial.

---

## Refusal

An explicit adapter-side decision to **not invoke Core evaluation**
because truthful declaration is not possible.

Refusal occurs before evaluation
and must be explicit and auditable.

---

## Truthful Declaration

An adapter-constructed representation of platform reality
that contains only facts the adapter can justify.

Truthful declarations:
- include only owned, sufficient, and known elements,
- never invent or approximate,
- and may exclude elements that cannot be declared honestly.

---

## Valuation

The assignment of economic meaning to declared items
based on explicit configuration.

Valuation:
- is adapter-supplied,
- deny-by-default,
- and produces immutable snapshots.

Absence of valuation is valid and intentional.

---

## Valuation Snapshot

A Core-consumed, immutable summary of valuation results
for all declared elements in an exchange.

The Core uses this snapshot to determine acceptance or denial.

---

## Zero-Configuration Invariant

The guarantee that the system boots and operates
even when no economic configuration exists.

In this state:
- all valuation is denied,
- no mutation occurs,
- and behavior remains explicit and honest.

---

END OF GLOSSARY
