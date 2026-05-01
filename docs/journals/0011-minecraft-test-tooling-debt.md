# Minecraft Test Tooling Debt - Short-Term Acceptance

## 1. Decision

The current matcher test tooling is acceptable short-term.

Container implementation may proceed, provided the next work remains narrow and does not treat the current test runtime setup as final release design.

## 2. Tooling Debt

- Access widener is needed for test-time Minecraft registry bootstrap.
- Test task uses test-only `-Xverify:none`.
- The project currently uses Fabric Loom `1.10.5` on Java 17.

## 3. Production Boundary

Minecraft bootstrap behavior is test-only.

`-Xverify:none` is test-only.

The access widener must not silently become production or release API. If this module is packaged, the access widener scope must be revisited.

## 4. Rationale

- No release packaging path exists yet.
- No Fabric runtime module exists yet.
- Current scope is narrow binding validation.

## 5. Future Cleanup

- Make access widening test-scoped or move it to integration-test setup.
- Revisit Java 21 and a newer Loom line.
- Define an integration-test strategy for real Minecraft inventory behavior.
- Decide whether the inventory binding should publish any access widener.

## 6. Next Step

Proceed to narrow player-inventory container implementation.
