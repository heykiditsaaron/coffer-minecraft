# Coffer Glossary
## Shared Vocabulary (Non-Authoritative)

---

## How This Glossary Is Maintained

This glossary exists to reduce implication and semantic drift.
It is not law, contract, or policy.

Because language shapes thought,
changes to this document must be made with care —
not to preserve correctness,
but to preserve shared understanding.

The following guidance exists to help future contributors
add clarity without rewriting authority.

### What This Glossary Is Allowed to Do

This glossary MAY be updated to:

- clarify how an existing term is used in practice,
- disambiguate terms that have begun to overlap,
- capture shared understanding that has emerged through implementation,
- or reduce confusion observed during collaboration.

Additions should reflect learned usage, not speculative intent.

### What This Glossary Must Not Do

This glossary MUST NOT be used to:

- introduce new rules or obligations,
- redefine law, contracts, or core semantics,
- justify behavior that contradicts authoritative documents,
- or retroactively legitimize decisions that violated constraints.

If a change would alter system behavior,
it belongs elsewhere.

### When a Term Should Be Added or Updated

A term should be added or revised when:

- collaborators consistently use the same word differently,
- a concept must be explained repeatedly in discussion,
- or implementation friction reveals hidden assumptions in language.

Glossary updates should follow understanding,
not attempt to create it in advance.

### How to Update the Glossary Safely

When adding or modifying an entry:

- prefer small, precise definitions,
- always include a “Not This” section,
- explain why the term exists,
- and reference authoritative documents rather than restating them.

If uncertainty exists, document the uncertainty plainly
instead of forcing precision.

### Relationship to Authority

If a glossary entry appears to conflict with Law, Contract, or Chronicle:

- the authoritative document takes precedence,
- and the glossary should be updated to reflect that truth.

The glossary explains language.
It does not decide outcomes.

### Guiding Principle

The glossary should make collaboration calmer,
not more rigid.

If maintaining this document begins to feel punitive,
argumentative, or defensive,
step back and reassess.

Clarity is the goal.
Authority already lives elsewhere.

---

## Truth

**Definition**  
Information that is explicitly known, declared without approximation,
and safe to be judged by the Core.

**Not This**  
- Best-effort guesses  
- Probable outcomes  
- Inferred intent  

**Why This Term Exists**  
To ensure the system never proceeds on information it does not actually possess.

**Where It Is Used**  
Law, Adapter Contract, Core Engine, all chronicles

---

## Declaration

**Definition**  
A structured statement constructed by an adapter that asserts specific facts
about platform reality.

**Not This**  
- A request for approval  
- A promise of outcome  
- A normalized or simplified summary  

**Why This Term Exists**  
To separate observing reality from judging reality.

**Where It Is Used**  
Adapter Contract, Phase 3 chronicles

---

## Refusal

**Definition**  
An explicit decision by a system component to not proceed
because truth cannot be established.

**Not This**  
- An error  
- A punishment  
- A temporary failure to be retried  

**Why This Term Exists**  
To allow honest non-action without inventing behavior.

**Where It Is Used**  
Law, Adapter Contract, Phase 3 chronicles

---

## Denial

**Definition**  
A negative judgment issued by the Core after evaluation.

**Not This**  
- Adapter refusal  
- Missing configuration  
- Execution failure  

**Why This Term Exists**  
To distinguish Core judgment from pre-evaluation honesty checks.

**Where It Is Used**  
Core Engine, Valuation, Audit records

---

## Evaluation

**Definition**  
The act of the Core judging a fully declared exchange exactly once
and producing a single verdict.

**Not This**  
- Negotiation  
- Aggregation  
- Execution  

**Why This Term Exists**  
To preserve determinism and auditability.

**Where It Is Used**  
Core Engine, Execution phases

---

## Mutation

**Definition**  
Any irreversible change to platform state
performed after a PASS verdict.

**Not This**  
- Evaluation  
- Preview  
- Estimation  

**Why This Term Exists**  
To enforce mutation-last invariants.

**Where It Is Used**  
Law, Adapter Contract, Phase 3D

---

## Aggregation

**Definition**  
The act of combining multiple independent platform facts
into a composite declaration.

**Not This**  
- Valuation  
- Normalization  
- Simplification for convenience  

**Why This Term Exists**  
To name where complexity must be absorbed explicitly.

**Where It Is Used**  
Adapter Contract, Phase 3C planning

---

## Partial

**Definition**  
A condition where some declared elements are valid
and others are not, without collapsing the distinction.

**Not This**  
- Partial mutation  
- Partial truth  
- Best-effort acceptance  

**Why This Term Exists**  
To allow mixed reality to be represented honestly.

**Where It Is Used**  
Valuation, Execution planning

---

## Absence

**Definition**  
The intentional lack of declared truth.

**Not This**  
- An error  
- A misconfiguration  
- A temporary state  

**Why This Term Exists**  
To establish that “nothing” can be the most honest answer.

**Where It Is Used**  
Law, Valuation rules

---

## Authority

**Definition**  
The right to make final, binding decisions within a defined scope.

**Not This**  
- Convenience  
- Proximity to data  
- Execution capability  

**Why This Term Exists**  
To prevent responsibility drift.

**Where It Is Used**  
Law, Structure, Adapter Contract

---

## Jurisdiction

**Definition**  
The boundary within which a component is responsible for truth or action.

**Not This**  
- Ownership of outcome  
- Permission to invent behavior  

**Why This Term Exists**  
To keep responsibility explicit and enforceable.

**Where It Is Used**  
Structure, Adapter design

---

## Core

**Definition**  
The engine that judges declared truth and issues verdicts.

**Not This**  
- A data source  
- An execution engine  
- A negotiator  

**Why This Term Exists**  
To centralize judgment without absorbing complexity.

**Where It Is Used**  
Throughout the system

---

## Adapter

**Definition**  
A platform-specific component responsible for observing reality,
constructing declarations, and executing mutation after PASS.

**Not This**  
- A thin wrapper  
- A policy engine  
- A UI layer  

**Why This Term Exists**  
To assign the burden of complexity explicitly.

**Where It Is Used**  
Adapter Contract, Phase 3 chronicles

---

## Execution

**Definition**  
The orchestration of evaluation and (if permitted) mutation.

**Not This**  
- Authority  
- Judgment  
- Success  

**Why This Term Exists**  
To separate calling the system from being acted upon.

**Where It Is Used**  
Phase 3B, Phase 3D

---

## Policy Layer

**Definition**  
A denial-only filter that evaluates contextual legitimacy
without mutating state.

**Not This**  
- Permission system  
- Rule engine  
- Business logic  

**Why This Term Exists**  
To stop invalid exchanges early and explicitly.

**Where It Is Used**  
Core Engine, Adapter execution

---

## Valuation

**Definition**  
The act of assigning explicit value to declared items
or denying value when truth is insufficient.

**Not This**  
- Estimation  
- Market simulation  
- Economic balancing  

**Why This Term Exists**  
To prevent value from being implied or assumed.

**Where It Is Used**  
Phase 3C.1, Phase 3C.3

---

## Phase

**Definition**  
A bounded unit of work that freezes structure, semantics, or capability.

**Not This**  
- Discovery narrative  
- Movement  
- Iteration  

**Why This Term Exists**  
To establish stable checkpoints.

**Where It Is Used**  
Roadmap, Chronicles

---

## Movement

**Definition**  
A retrospective explanation of how understanding evolved
while remaining lawful.

**Not This**  
- A plan  
- A requirement  
- A gate  

**Why This Term Exists**  
To transmit learned wisdom without rewriting authority.

**Where It Is Used**  
Chronicle annotations

---

## Chronicle

**Definition**  
The factual historical record of what was decided,
frozen, deferred, or rejected.

**Not This**  
- A justification  
- A plan  
- A tutorial  

**Why This Term Exists**  
To preserve decisions beyond memory.

**Where It Is Used**  
docs/chronicle/

---

## Annotation

**Definition**  
A retrospective witness explaining how authority
was upheld through difficulty.

**Not This**  
- A rule change  
- A correction  
- A new decision  

**Why This Term Exists**  
To allow documents to acknowledge friction without losing integrity.

**Where It Is Used**  
Chronicles, Contract, Roadmap

---

## Intent

**Definition**  
What a user is trying to achieve,
distinct from what is permitted.

**Not This**  
- Outcome  
- Entitlement  
- Guarantee  

**Why This Term Exists**  
To allow expression without obligation.

**Where It Is Used**  
Execution design, UX planning

---

## Estimate

**Definition**  
A non-binding projection labeled explicitly as uncertain.

**Not This**  
- A promise  
- A valuation  
- An approval  

**Why This Term Exists**  
To inform without misleading.

**Where It Is Used**  
Sell menu planning

---

## Trust

**Definition**  
The confidence that the system will behave
exactly as it states, even when refusing.

**Not This**  
- Reward  
- Success  
- Optimism  

**Why This Term Exists**  
To define trust as consistency, not generosity.

**Where It Is Used**  
UX philosophy, Phase 4–5 planning

---

## Vigilance

**Definition**  
The burden placed on a user to prevent dishonesty or error.

**Not This**  
- Skill  
- Mastery  
- Engagement  

**Why This Term Exists**  
To name the cost the system is designed to absorb.

**Where It Is Used**  
Project philosophy, design goals
