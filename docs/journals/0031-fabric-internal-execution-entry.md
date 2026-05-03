# Fabric Internal Execution Entry

## Summary

The Fabric platform service now has a minimal internal exchange execution entry.

## Execution Path

The internal service path is:

```text
ExchangeRequest
  -> CofferCore.arbitrate(...)
  -> registered TransferableValueCoreAuthority
  -> approved MutationPlan
  -> CofferRuntime.execute(...)
  -> registered TransferableValueRuntimeAuthority
  -> Minecraft binding container path
```

Denied arbitration returns the Core `Outcome` directly from the internal method.

Approved arbitration executes the mutation plan through `CofferRuntime` and returns the runtime `ExecutionResult`.

## Scope

The method remains package-private on the platform service. It is not a public gameplay API and is not exposed through commands, UI, adapters, or any mod-facing service boundary.

## Threading

The method assumes the caller is already on the Minecraft server thread.

If a server is attached and the caller is off-thread, the method rejects execution before arbitration. No scheduling is added yet.

## Result Semantics

The service does not reinterpret Coffer Core or Runtime results.

Failed and unknown semantics remain owned by Core, Runtime, TransferableValueAuthority, and the Minecraft binding.

## Boundaries

No adapter logic, command integration, UI integration, scheduler, player lookup policy change, inventory mutation logic, rollback, retry behavior, or binding logic change was added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- The internal method currently generates deterministic infrastructure IDs from the request ID.
- The Core API requires denial reason IDs before arbitration, so the service supplies a bounded internal list.
- A future adapter-facing API may need a stronger typed result wrapper instead of returning either Core `Outcome` or Runtime `ExecutionResult`.
