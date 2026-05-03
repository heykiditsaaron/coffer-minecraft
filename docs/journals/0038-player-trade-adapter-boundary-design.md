# Player-Trade Adapter Boundary Design

## Scope Decision

Player-trade adapter design may begin.

Implementation remains deferred until adapter access to the Fabric service and result types is explicitly resolved.

## Service Discovery

Future adapters need a controlled way to obtain the Fabric Coffer service.

Minimal recommendation: expose a small Fabric-owned service access point that returns the initialized `CofferMinecraftFabricService` or an adapter-facing service interface. The access point should be owned by `platforms/fabric`, not by bindings or adapter logic.

Options remain:

- static accessor from the Fabric entrypoint or a nearby service holder
- Fabric-facing registry
- injected reference when adapter construction is introduced

The first implementation should choose the smallest mechanism that supports one in-process adapter without creating a broad service registry.

## Type Visibility

`CofferMinecraftFabricService` currently remains package-private.

If adapters live outside `dev.coffer.minecraft.platform.fabric`, either the service must become public or a public adapter-facing interface must expose only:

```text
CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request)
```

Minimal recommendation: prefer a public narrow interface if the adapter lives outside the platform package, keeping lifecycle, server attachment, authority wiring, direct execution, and scheduler helpers package-private or private.

`FabricCofferExecutionResult` must become visible to any adapter that receives it. Its variants should remain the only supported result alternatives.

## Execution Contract For Adapters

Adapters submit Coffer `ExchangeRequest` values through `submitExchange(...)`.

Adapters must handle:

- `Denied` as Core arbitration denial
- `Executed` as Runtime attempted execution
- `Unavailable` as Fabric inability to safely attempt or continue execution

Adapters must not call `CofferCore`, `CofferRuntime`, binding containers, or Minecraft inventory mutation paths directly.

## Non-Success And Partial Mutation Semantics

Adapters must not assume non-success means no inventory changed.

`applyAtomicSwap(...)` is guarded by simulation and returns non-success under drift or uncertainty, but rollback is intentionally not implemented. A non-success result can therefore require conservative player-facing handling.

Future player-facing messaging must account for uncertainty and must not claim a clean failure or success when the execution result cannot safely support that claim.

No retry or rollback behavior exists yet.

## Explicit Non-Goals

This design does not add:

- UI or commands
- player-facing text
- trade protocol
- adapter implementation
- blocking helpers
- persistence
- retry or rollback behavior

## Hardening Before Implementation

Before implementing the player-trade adapter:

- choose service accessor, registry, or injection
- choose public service/interface visibility
- make `FabricCofferExecutionResult` visible where needed
- add service-level tests where feasible
- define an explicit player-facing uncertainty policy later, before user-visible messaging

## Uncertainties

- Accessor vs registry vs injection.
- Public API stability and whether to expose the concrete service or a narrow interface.
- How much detail adapters need from Runtime `ExecutionResult`.
- Whether result diagnostics need to grow before player-facing behavior exists.
