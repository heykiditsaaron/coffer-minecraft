# Fabric Execution Result Wrapper Design

## Problem

`CofferMinecraftFabricService.executeExchange(...)` currently returns `Object`.

That object is either:

- a Core `Outcome` when arbitration denies the exchange
- a Runtime `ExecutionResult` when Core approves and Runtime attempts execution

`executeExchangeScheduled(...)` also returns this package-private object shape, with a temporary internal unavailable result for platform scheduling failures.

This is acceptable only as temporary internal plumbing. Adapter-facing code needs a typed result boundary before any gameplay API, command, UI, or player-trade adapter consumes Fabric execution.

## Decision

Create an internal Java 17-compatible result wrapper for Fabric exchange execution.

Suggested name:

```text
FabricCofferExecutionResult
```

Suggested shape:

```text
sealed interface FabricCofferExecutionResult permits Denied, Executed, Unavailable

record Denied(Outcome outcome) implements FabricCofferExecutionResult
record Executed(ExecutionResult result) implements FabricCofferExecutionResult
record Unavailable(String reasonCode) implements FabricCofferExecutionResult
```

An equivalent package-private sealed type, nested sealed type, or final class with explicit variants is acceptable if it preserves the same typed alternatives.

## Semantics

`Denied` means Core refused the exchange before runtime execution. The wrapped `Outcome` remains owned by Core and must not be reinterpreted by Fabric.

`Executed` means Core approved arbitration and Runtime attempted execution. The wrapped `ExecutionResult` remains owned by Runtime and must not be reinterpreted by Fabric.

`Unavailable` means Fabric could not safely attempt arbitration or execution, such as when no server is attached, scheduling is unavailable, or server state changes before deferred execution runs.

Fabric must never report success under uncertainty.

## Scheduler Integration

`executeExchangeScheduled(...)` should eventually return:

```text
CompletableFuture<FabricCofferExecutionResult>
```

The package-private `executeExchange(...)` should return:

```text
FabricCofferExecutionResult
```

The scheduled path should continue wrapping the direct execution path rather than duplicating Core arbitration or Runtime execution behavior.

## Boundaries

This design does not add:

- commands or UI
- player-trade adapters
- persistence
- gameplay-facing API
- Core, Runtime, or binding behavior changes
- reinterpretation of `Outcome` or `ExecutionResult`

Inventory semantics, descriptor identity, runtime execution guarantees, and server-thread mutation rules remain owned by the existing layers.

## Uncertainties

- Exact `Unavailable` reason code constants.
- Whether a future adapter-facing API needs richer detail than these three variants.
- Whether `Unavailable` should carry throwable detail internally while keeping uncertainty out of success paths.
