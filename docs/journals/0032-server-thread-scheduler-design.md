# Server-Thread Scheduler Design

## Decision

Fabric execution must normalize Coffer exchange execution onto the Minecraft server thread.

If a caller is already on the server thread, execution may run immediately. If a caller is off-thread, Fabric should enqueue the work onto the server thread. Direct off-thread execution remains forbidden.

## Execution Model

The scheduled path should wrap the existing internal execution entry rather than duplicate its arbitration or runtime behavior.

The model is:

```text
ExchangeRequest
  -> Fabric scheduler boundary
  -> immediate execution when already on server thread
  -> server-thread enqueue when off-thread
  -> existing Core arbitration and Runtime execution path
```

Core denial still returns the Core `Outcome`. Approved execution still returns the Runtime `ExecutionResult`. The service should preserve this `Outcome` vs `ExecutionResult` distinction for now rather than reinterpret either result.

## Return Shape

The internal scheduled method should return `CompletableFuture<Object>`.

`Object` is acceptable only because the method remains internal and currently represents either a Core `Outcome` or Runtime `ExecutionResult`. This should not become an adapter-facing contract. A future adapter boundary needs a stronger typed result wrapper that explicitly models arbitration denial, runtime execution result, and platform unavailability.

## Failure and Unknown Behavior

If no server is attached, the scheduled method should complete with an unknown or unavailable style result if the current Coffer types can represent that safely.

If scheduling fails, the scheduled method should also complete with an unknown or unavailable style result if possible.

Fabric must not report success when execution could not be scheduled, could not reach the server thread, or could not safely determine state. Known inventory denials discovered on the server thread remain failures owned by Core, Runtime, and the Minecraft binding path.

## Boundaries

This design adds only the scheduler boundary for the internal Fabric service.

It does not add:

- gameplay adapters
- commands or UI
- retry or rollback behavior
- inventory logic changes
- binding behavior changes
- descriptor interpretation changes

## Uncertainties

- Exact Minecraft/Fabric server execute API usage.
- Whether future adapters need blocking calls, async calls, or both.
- Final typed wrapper for adapter-facing results.
- Exact unknown or unavailable representation available from current Coffer result types.
