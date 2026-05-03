# Adapter-Facing Execution Surface Implemented

## Summary

`CofferMinecraftFabricService` now exposes:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

The method is the minimal async boundary intended for future Fabric adapters.

## Behavior

`submitExchange(...)` delegates to the existing scheduled execution path.

The Fabric service continues to own server-thread scheduling. Callers may submit from any thread, and live Minecraft inventory access remains normalized onto the Minecraft server thread by the service.

`Denied`, `Executed`, and `Unavailable` result semantics are unchanged.

## Boundaries

No player-trade adapter, command/UI surface, player-facing messages, blocking helper, persistence, retry, rollback, Core behavior, Runtime behavior, binding behavior, descriptor interpretation, or container logic was added.

Adapters still must not access `CofferCore` or `CofferRuntime` directly.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- Whether future adapters need blocking convenience helpers.
- Whether `FabricCofferExecutionResult` needs richer diagnostics.
- How adapters will discover or receive the Fabric service instance.
- Whether additional type visibility changes are needed when the first real adapter is introduced.
