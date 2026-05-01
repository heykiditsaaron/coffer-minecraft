# Minecraft Transferable Value End-to-End

## Summary

An integration-style test now exercises the real TransferableValueAuthority Core and Runtime wrappers with the Minecraft inventory collaborators.

Covered flow:

```text
CofferCore
  -> TransferableValueCoreAuthority
  -> TransferableValueRuntimeAuthority
  -> Minecraft collaborators
  -> MinecraftPlayerInventoryContainer
```

## Successful Swap

The test builds an `ExchangeRequest` using the TransferableValueAuthority atomic swap schema, arbitrates through `CofferCore`, verifies approval and mutation-plan production, executes through `CofferRuntime`, and verifies both Minecraft-backed inventories mutate.

## Denial Path

The insufficient-quantity case is denied by Core before runtime execution.

The denial preserves the Minecraft binding reason code:

- `minecraft.value.not_removable`

## Drift Runtime Path

The post-approval drift case verifies that Core can approve based on initial state, then Runtime execution reports a non-success when source inventory changes before application.

Current binding behavior reports:

- runtime status: `MUTATION_FAILED`
- reason code: `minecraft.value.not_removable`

## Boundaries

No production inventory logic, Fabric Loader/API, player-trade adapter, or Coffer Core/Runtime code was changed.

## Verification

`gradle :bindings:inventory:test` passed.

## Remaining Uncertainty

This proves wrapper compatibility, not real server/gameplay safety. Platform glue still needs server-thread or transaction strategy decisions before player-facing workflows use runtime execution.
