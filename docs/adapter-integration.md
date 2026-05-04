# Adapter Integration

## Purpose

This document describes how external adapters integrate with `coffer-minecraft`.

Adapters are consumers of the Fabric exchange service. They build Coffer `ExchangeRequest` values, submit them to `coffer-minecraft`, and handle the returned result.

## Getting The Service

Use the Fabric entrypoint accessor:

```text
CofferMinecraftFabricEntrypoint.exchangeService()
```

The accessor returns a `CofferMinecraftExchangeService`.

## Submitting An Exchange

Adapters are responsible for building an `ExchangeRequest`.

Submit the request through:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

Exchange submission is asynchronous. The returned `CompletableFuture` completes with a `FabricCofferExecutionResult` when the service has denied, attempted, or declined to attempt the exchange.

## Handling Results

Adapters must handle all result variants:

- `Denied`: Coffer Core refused the exchange before runtime execution.
- `Executed`: Coffer Runtime attempted the approved exchange.
- `Unavailable`: the Fabric platform could not safely attempt or continue execution.

Do not assume a non-success result means no inventory changed. Execution is guarded, but rollback and retry are not implemented.

## Adapter Responsibilities

Adapters must:

- construct valid `ExchangeRequest` values
- submit exchanges only through `CofferMinecraftExchangeService`
- inspect and interpret `FabricCofferExecutionResult`
- own any player interaction or gameplay flow outside this repository

Adapters must not:

- access Minecraft inventory directly for exchange execution
- construct Coffer Core, Runtime, authorities, or inventory containers
- call Coffer Core or Runtime directly
- depend on `bindings/inventory` internals

## Threading

Adapters may call `submitExchange(...)` from any thread.

The Fabric service handles scheduling and ensures live inventory access occurs on the Minecraft server thread.

## Explicit Non-Goals

This guide does not define:

- UI or commands
- player messaging examples
- gameplay protocol

## Known Limitations

- No rollback support.
- Partial mutation is possible if inventory state drifts during application.
- Result diagnostics are limited.
