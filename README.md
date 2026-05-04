# Coffer Minecraft

`coffer-minecraft` is the Fabric platform implementation of the Coffer TransferableValue system for Minecraft inventories.

It provides Minecraft inventory-backed exchange execution and owns the Fabric lifecycle, Coffer wiring, server-thread scheduling, and the adapter-facing execution surface.

## What This Is Not

This project is not:

- a gameplay system
- a player-trade implementation
- a UI or command layer
- a permission system

Gameplay adapters are expected to live outside this repository and consume the public Fabric service.

## Architecture

`bindings/inventory` owns Minecraft inventory semantics:

- item descriptors
- item matching
- player inventory containers
- removability and receivability checks
- atomic swap simulation and application

`platforms/fabric` owns Fabric integration:

- startup and shutdown lifecycle
- Coffer service wiring
- Minecraft server attachment
- server-thread execution
- public exchange submission

Adapters are external consumers. They build Coffer requests and submit them through the Fabric service. They are not included in this repository.

## Core Flow

At a high level:

1. An adapter builds an `ExchangeRequest`.
2. The adapter calls `CofferMinecraftFabricEntrypoint.exchangeService().submitExchange(...)`.
3. The Fabric service schedules execution on the Minecraft server thread when needed.
4. Coffer Core arbitrates the exchange.
5. Coffer Runtime executes through the Minecraft inventory binding.
6. The caller receives a `FabricCofferExecutionResult`.

## Public API

The public adapter-facing service is:

```text
CofferMinecraftExchangeService
```

It exposes:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

Adapters discover the service through:

```text
CofferMinecraftFabricEntrypoint.exchangeService()
```

`FabricCofferExecutionResult` has three variants:

- `Denied`: Coffer Core denied the exchange before runtime execution.
- `Executed`: Coffer Runtime attempted the approved exchange.
- `Unavailable`: Fabric could not safely attempt or continue execution.

## Behavior Notes

Exchange execution is asynchronous.

Adapters may call `submitExchange(...)` from any thread. The Fabric service is responsible for server-thread scheduling before live inventory access occurs.

Adapters must not construct Core, Runtime, authorities, or inventory containers directly.

Adapters must not treat every non-success result as proof that no inventory changed. Execution is guarded, but rollback and retry are not implemented.

## Example Usage

Pseudo-code:

```text
request = buildExchangeRequest(...)

future = CofferMinecraftFabricEntrypoint
    .exchangeService()
    .submitExchange(request)

future.thenAccept(result -> {
    switch result:
        Denied:
            handleCoreDenial(result.outcome)
        Executed:
            inspectRuntimeResult(result.result)
        Unavailable:
            handleUnavailableService(result.reasonCode)
})
```

## Development Status

The internal inventory binding and Fabric execution surface are in place.

Adapter integration is ready through the public service interface.

Gameplay adapters, including player-trade adapters, have not been implemented in this repository.

## Known Limitations

- No rollback support.
- Partial mutation is possible if inventory state drifts during application.
- Result diagnostics are intentionally limited.
- No persistence layer.
- Binding test tooling still carries test-only runtime debt.
