# Coffer

Coffer is a policy-based commerce substrate for Minecraft servers.

It is designed to make honesty effortless — not by simplifying reality,
but by absorbing the burden of truth into the system itself.

Coffer treats honesty as a shared commons:
available to everyone, owned by no one, and never for sale.

---

## What Coffer Promises

**Coffer is built so players don’t have to guess what’s honest.**

You can trust that the system will:
- receive your intent,
- evaluate it truthfully,
- deny what is dishonest,
- explain that denial honestly,
- and never punish exploration or creativity.

If something isn’t allowed, Coffer will say so clearly.  
If something isn’t known, Coffer will refuse rather than guess.

The system absorbs the burden of honesty — not the player.

---

## What This Means in Practice

- Players are free to experiment without fear of hidden penalties.
- The system does not require players to know values, rules, or policies in advance.
- Denials are explicit, non-punitive, and auditable.
- Absence of information results in refusal, not approximation.
- Mixed outcomes (some items allowed, some denied) are handled honestly.

Coffer optimizes for **predictability and trust**, not convenience through guessing.

---

## What Coffer Is (and Is Not)

### Coffer *Is*
- A server-side system
- Policy-driven and auditable
- Explicit about refusals
- Designed for complex, modded environments
- Built to remain honest under uncertainty

### Coffer *Is Not*
- A guarantee of profit or success
- A market simulator
- A permission system
- A UI-first mod
- A system that invents value, intent, or outcomes

---

## Design Principles

Coffer follows a small set of non-negotiable principles:

- **Truth before usefulness**  
  If something cannot be evaluated honestly, it is refused.

- **Refusal over guessing**  
  Silence and absence are valid answers.

- **No punishment for exploration**  
  Trying something invalid results in clarity, not penalty.

- **Mutation happens last**  
  State changes occur only after truthful evaluation.

- **Authority is explicit**  
  The Core judges truth.  
  Adapters construct truth.  
  Execution applies consequence.

---

## Operational Integrity & Failure Boundaries

Coffer prioritizes honesty and state integrity over automatic recovery.

In rare circumstances, a mutation may fail in a way that prevents Coffer from guaranteeing a truthful rollback. When this occurs, Coffer will refuse to continue economic execution until the system state can be reviewed.

This behavior is intentional.

Coffer does not guess, silently correct, or partially recover from corruption. Doing so would risk dishonest outcomes, partial loss, or invisible inconsistency — all of which violate Coffer’s core principles.

When Coffer cannot proceed safely, it will stop.

This protects players from confusion, prevents hidden damage, and ensures administrators are always operating with a known and auditable system state.

Administrators are encouraged to test Coffer alongside other mods, inventory systems, and persistence layers before enabling it in production environments.

---

## Commons License

Coffer is distributed under the **Coffer Commons License (CCL)**.

- Coffer is free to use, modify, and redistribute.
- Coffer itself may **never be sold**, gated, or monetized.
- Derivative works (including adapters and forks) may not be sold.
- Monetized servers may use Coffer, but Coffer may not be the reason money is requested.
- No one may claim official affiliation, endorsement, or representation.

In short:  
**Coffer is a commons.  
Honesty may not be privatized.**

See [`LICENSE`](./LICENSE) for full terms.

---

## Status

Coffer is under active development.

Core semantics and governing law are frozen.  
Platform adapters are implemented incrementally,
with honesty and auditability taking precedence over feature completeness.

---

## Documentation

- **Law and Charter** — Foundational constraints and invariants  
- **Adapter Contract** — Binding obligations between Core and adapters  
- **Chronicles** — Historical record of decisions and discoveries  
- **Annotations** — Witnesses of friction without rewriting authority  
- **Glossary** — Shared vocabulary to prevent semantic drift  

These documents exist to make collaboration calm,
decisions durable,
and future work honest.

---

## Philosophy (Short)

Coffer does not try to make the “right” outcome easy.

It tries to make the **truthful** outcome unavoidable —
and transparent when it says no.
