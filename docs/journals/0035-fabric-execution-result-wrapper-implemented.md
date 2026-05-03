# Fabric Execution Result Wrapper Implemented

## Summary

Fabric exchange execution now uses a typed internal result wrapper.

`FabricCofferExecutionResult` was added as a package-private sealed interface with explicit variants:

- `Denied(Outcome outcome)`
- `Executed(ExecutionResult result)`
- `Unavailable(String reasonCode)`

## Result Semantics

`Denied` represents Core arbitration denial before Runtime execution. Fabric does not reinterpret the wrapped Core `Outcome`.

`Executed` represents Core approval followed by a Runtime execution attempt. Fabric does not reinterpret the wrapped Runtime `ExecutionResult`.

`Unavailable` represents platform inability to safely attempt or continue execution, including unavailable server state or scheduler unavailability. Fabric does not report success under uncertainty.

## Service Integration

`CofferMinecraftFabricService.executeExchange(...)` now returns `FabricCofferExecutionResult`.

`CofferMinecraftFabricService.executeExchangeScheduled(...)` now returns `CompletableFuture<FabricCofferExecutionResult>`.

The temporary package-private `Object` result shape and private platform unavailable record were removed from the service.

The scheduler behavior is unchanged beyond the typed return boundary.

## Boundaries

No gameplay-facing API, command/UI surface, player-trade adapter, persistence, Core behavior, Runtime behavior, binding behavior, descriptor interpretation, inventory logic, retry, or rollback behavior was added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- Exact `Unavailable` reason code constants remain provisional.
- Future adapter-facing APIs may need richer detail than the current three variants.
- `Unavailable` may later need throwable detail for internal diagnostics without changing success semantics.
