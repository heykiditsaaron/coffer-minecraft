# Service Discovery And Type Visibility

## Decision

Expose a narrow public adapter-facing service interface instead of exposing Fabric platform internals directly.

The Fabric service may implement the public interface, but lifecycle, Core/Runtime fields, authority wiring, player inventory resolution, scheduler helpers, and binding details remain internal implementation concerns.

## Proposed Public Interface

Suggested interface:

```text
CofferMinecraftExchangeService
```

Suggested method:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

This is the only adapter-facing exchange execution entry currently designed.

## Type Visibility

`FabricCofferExecutionResult` should become public because external adapters must inspect the explicit result variants:

- `Denied`
- `Executed`
- `Unavailable`

`CofferMinecraftFabricService` may remain an implementation detail if it implements the public `CofferMinecraftExchangeService` interface.

Core/Runtime fields and authority wiring remain private or package-private and are not part of the adapter contract.

## Service Discovery

The Fabric entrypoint or a nearby Fabric-owned service holder should expose a minimal static accessor for the public interface.

Suggested accessor:

```text
CofferMinecraftFabricEntrypoint.exchangeService()
```

The accessor should return `CofferMinecraftExchangeService`, not the concrete service, unless implementation constraints require otherwise.

## Adapter Rules

Adapters submit `ExchangeRequest` values only through the public service.

Adapters must not construct Core, Runtime, authorities, or inventory containers.

Adapters must not access `bindings/inventory` directly unless that access is explicitly designed in a later journal.

## Result Handling

Adapters must handle:

- `Denied`
- `Executed`
- `Unavailable`

Adapters must not assume non-success means no mutation occurred. Runtime execution is guarded by simulation, but rollback and retry are not designed, and uncertainty must not be reported as clean failure.

Adapter-facing messaging remains future work.

## Non-Goals

This design does not add:

- player-trade adapter implementation
- commands or UI
- blocking helper
- persistence
- rollback or retry

## Uncertainties

- Whether the accessor should return `Optional<CofferMinecraftExchangeService>` or expose another unavailable state before initialization.
- Whether current result diagnostics are sufficient for player-facing adapters.
- Whether future multi-adapter registration needs a registry.
