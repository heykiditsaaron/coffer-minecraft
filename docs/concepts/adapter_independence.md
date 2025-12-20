# Adapter Independence — Conceptual Principle

Coffer adapters must function independently.

They must:
- not rely on external APIs for truth,
- not leak authority to dependencies,
- and not require other mods to function honestly.

External systems may provide **inputs**, never authority.

If external data is unavailable or uncertain:
- the system refuses,
- explains why,
- and performs no mutation.

Independence preserves honesty.
