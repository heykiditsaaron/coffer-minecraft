# Service Discovery And Type Visibility Implemented

## Summary

Added the public Fabric adapter-facing exchange service:

```text
CofferMinecraftExchangeService
```

The interface exposes only:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

## Entrypoint Accessor

`CofferMinecraftFabricEntrypoint` now exposes:

```text
public static CofferMinecraftExchangeService exchangeService()
```

The accessor returns the public interface, not the concrete `CofferMinecraftFabricService`.

Before initialization, the accessor returns a minimal unavailable service. Submitting an exchange through that service completes with:

```text
FabricCofferExecutionResult.Unavailable("SERVICE_UNINITIALIZED")
```

This avoids exposing a concrete service before initialization and keeps pre-initialization adapter behavior non-mutating.

The concrete Fabric service is published to the accessor only after initialization completes.

## Type Visibility

`FabricCofferExecutionResult` is now public.

Its adapter-inspectable variants remain:

- `Denied`
- `Executed`
- `Unavailable`

No result semantics were changed.

## Internal Boundaries

`CofferMinecraftFabricService` remains package-private and implements `CofferMinecraftExchangeService`.

Core, Runtime, authority wiring, scheduler helpers, player inventory resolution, and binding/container details remain internal to the Fabric platform implementation.

No player-trade adapter, command/UI, blocking helper, persistence, rollback, retry, execution behavior change, or binding/container logic change was added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- Whether `SERVICE_UNINITIALIZED` is sufficient as the long-term pre-initialization diagnostic.
- Whether current result diagnostics are sufficient for player-facing adapters.
- Whether future multi-adapter registration needs a registry.
