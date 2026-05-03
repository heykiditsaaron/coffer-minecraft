# AGENTS.md

## Core Workflow

All work in this repository MUST follow:

Design → Journal → Implement → Verify → Commit

- Journal entries MUST be written before commit.
- Do not skip steps.
- Do not batch unrelated changes.

---

## Repository Structure & Boundaries

### bindings/inventory
Owns:
- Minecraft inventory semantics
- Descriptor, matcher, container logic

Must NOT:
- Include platform/lifecycle/threading logic
- Depend on Fabric APIs
- Perform runtime wiring

---

### platforms/fabric
Owns:
- Lifecycle (startup/shutdown)
- Authority wiring
- Player inventory resolution
- Server-thread execution

Must NOT:
- Implement inventory logic
- Duplicate binding behavior
- Interpret descriptors beyond delegation

---

## Non-Negotiable Rules

- Do NOT duplicate logic across layers
- Do NOT move logic from bindings → platform
- Do NOT introduce hidden behavior
- Do NOT invent behavior not defined in journals
- Do NOT weaken existing guarantees

---

## Descriptor Rules

- Identity = item id + full NBT
- Quantity is NOT identity
- No fuzzy matching
- No partial NBT matching
- No display text as truth
- NBT must be preserved, not normalized

---

## Container Rules

Must implement:
- canRemove
- canReceive
- simulateAtomicSwap
- applyAtomicSwap

Requirements:
- Simulation MUST NOT mutate state
- Application MUST be guarded by simulation
- No success on partial or uncertain execution
- Post-simulation drift MUST return non-success (not success)
- No rollback or retry unless explicitly designed

---

## Threading Rules

- ALL inventory mutation MUST occur on the Minecraft server thread
- Off-thread calls MUST be scheduled onto the server thread
- Never mutate inventory off-thread

---

## Error Semantics

- Failed = known, deterministic denial
- Unknown = state cannot be safely determined

Do NOT:
- Collapse Unknown into Failed
- Return success under uncertainty

---

## Testing Rules

- All behavior MUST have tests
- Simulation MUST be verified as non-mutating
- Application MUST be verified as guarded
- E2E Core → Runtime → Binding path MUST remain valid

---

## Tooling Constraints

- Access widener is test-only
- `-Xverify:none` is test-only
- These MUST NOT become production requirements

---

## Explicit Non-Goals

Do NOT implement:
- Player-trade adapters
- Commands/UI
- Persistence
- Gameplay-facing logic

---

## Design Authority

- Journals are the source of truth for behavior
- If behavior is unclear → STOP and design before implementing
