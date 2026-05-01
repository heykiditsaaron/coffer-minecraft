# Fabric Lifecycle and Authority Wiring

## Lifecycle Entrypoint

The Fabric platform layer initializes Coffer services during server startup and tears them down during server stop.

Initial scope should be server-wide rather than per-world. Inventory actors are player-scoped, and the first supported containers are live player inventories, so a single server-level service is the simplest boundary.

## Coffer Runtime Creation

`CofferCore` remains a platform-neutral static arbitration entrypoint.

`CofferRuntime` should be created by the Fabric platform layer during server startup and stored in a server-scoped service object. That service should be visible to future mod systems through a narrow platform API, not by exposing mutable globals throughout gameplay code.

## Authority Wiring

The Fabric service constructs `TransferableValueCoreAuthority` with:

- `MinecraftContainerResolver`
- `MinecraftDescriptorFactory`
- `MinecraftRuntimePayloadFactory`

The Fabric service constructs `TransferableValueRuntimeAuthority` with:

- `MinecraftContainerResolver`
- `MinecraftRuntimeValueSetResolver`
- `MinecraftRuntimePayloadInterpreter`
- a detail mapper that preserves Minecraft reason codes

The container resolver receives the platform player-inventory lookup implementation.

## Registration Model

The Core authority is registered with the authority resolver used when Coffer arbitration is invoked.

The Runtime authority is registered with the runtime authority collection used when executing mutation plans.

The platform service owns both registrations so future adapters can request Coffer arbitration/execution without constructing authorities directly.

## Execution Entry Surface

Future adapters should call a platform Coffer service boundary with an `ExchangeRequest` or higher-level request object.

The service performs arbitration through `CofferCore`, then executes approved mutation plans through `CofferRuntime` and the registered runtime authorities.

Adapter-specific workflows are outside this design.

## Threading Integration

Lifecycle setup and teardown should run on the server lifecycle path.

Runtime execution that touches live inventories must obey the server-thread execution policy. Authority construction is safe during lifecycle initialization, but container resolution and mutation must occur only through scheduled server-thread execution.

## Explicit Non-Goals

- No player-trade adapter.
- No UI or command integration.
- No persistence layer.
- No cross-server or multi-loader lifecycle abstraction.

## Uncertainties

- Exact Fabric lifecycle hooks.
- Whether server-wide singleton scope remains sufficient for all future worlds/dimensions.
- Hot-reload and server restart behavior.
- How future adapters discover or receive the platform Coffer service.
