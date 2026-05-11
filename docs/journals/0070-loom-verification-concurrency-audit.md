# 0070 Loom Verification Concurrency Audit

## Summary

Audited the transient Fabric verification instability observed during Codex
execution.

No repository-local build miswiring was identified. The observed failures are
most consistent with overlapping Gradle/Loom processes contending on shared
Loom cache state during separate concurrent invocations.

## Decision

Do not change build configuration in this step.

Use a workflow rule instead:

- do not run multiple independent Fabric Gradle invocations concurrently against
  this repository
- prefer one combined Gradle invocation for verification, or run Fabric checks
  serially

Safe verification forms:

- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`
- or the same tasks one after another, not overlapped

## Rationale

Evidence from this audit:

- repository Gradle files do not enable explicit parallel execution
- isolated `:platforms:fabric:test` passes
- isolated `:platforms:fabric:compileJava` passes
- one combined Gradle invocation also passes
- the observed failures only appeared while separate Fabric Gradle processes
  overlapped
- the overlapping runs reported Loom cache lock contention under
  `/home/aaron/.gradle/caches/fabric-loom`
- transient compile failures manifested as impossible same-package symbol
  resolution misses, which is consistent with unstable concurrent build state
  rather than a deterministic source-set bug

This is most safely treated as a local workflow/daemon/cache-contention issue.

## Scope

Included:

- audit of Gradle and Loom configuration
- narrow concurrency reproduction attempt
- single-invocation control verification
- workflow recommendation

Excluded:

- speculative Loom configuration changes
- cache deletion as a default fix
- broader Gradle refactors

## Verification

Reliable:

- `./gradlew :bindings:inventory:test`
- `./gradlew :platforms:fabric:test`
- `./gradlew :platforms:fabric:compileJava`
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`

Observed instability pattern:

- only during overlapping separate Fabric Gradle invocations
- associated with Loom cache lock messages

## Uncertainties

This audit does not prove an upstream Loom bug with a minimal standalone
reproducer.

It also must not be inferred that:

- Fabric verification is semantically flaky in isolated runs
- accountability code caused the instability
- bindings or platform source sets are miswired
