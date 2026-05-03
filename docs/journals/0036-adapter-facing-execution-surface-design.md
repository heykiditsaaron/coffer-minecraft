# Adapter-Facing Execution Surface Design

## Purpose

Future Fabric adapters need a controlled service boundary for submitting Coffer `ExchangeRequest` values.

The boundary should avoid exposing raw Fabric lifecycle, authority wiring, Core arbitration, Runtime execution, or Minecraft inventory resolution details to adapters.

## API Shape

Recommended service method:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

The API is asynchronous by default.

The service owns server-thread normalization. If called on the Minecraft server thread, execution may run immediately. If called off-thread, the service schedules execution onto the server thread.

## Access Policy

The service may expose `submitExchange(...)` publicly for adapter use.

Execution internals such as direct Core arbitration, Runtime execution, authority wiring, server attachment, and scheduler helpers remain package-private or private.

Adapters must not access `CofferCore` or `CofferRuntime` directly.

## Result Semantics

`Denied` means Core denied the exchange before Runtime execution.

`Executed` means Core approved the exchange and Runtime attempted execution.

`Unavailable` means Fabric could not safely attempt arbitration or execution.

Adapters must not reinterpret `Outcome`, `ExecutionResult`, or `Unavailable` into stronger success or failure claims.

## Threading

Adapters may call `submitExchange(...)` from any thread.

The Fabric service owns scheduling and all live inventory access must occur on the Minecraft server thread.

Adapters must not mutate Minecraft inventory directly.

## Explicit Non-Goals

This design does not add:

- player-trade adapters
- commands or UI
- player-facing messages
- persistence
- retry or rollback behavior
- adapter-specific execution workflows

## Uncertainties

- Whether future adapters need blocking convenience helpers in addition to the async API.
- Whether `FabricCofferExecutionResult` needs richer diagnostics for adapter decisions or logging.
- How adapters will discover or receive the Fabric service instance.
