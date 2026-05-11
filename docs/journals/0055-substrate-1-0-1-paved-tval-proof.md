# Substrate 1.0.1 Paved TVAL Proof

## Summary

Aligned `coffer-minecraft` from substrate `1.0.0` to `1.0.1` and migrated
`MinecraftTransferableValueEndToEndTest` from hand-authored TVAL mutation
construction to the published paved TVAL construction surface.

## Decision

`coffer-minecraft` now consumes `dev.coffer:*:1.0.1` and constructs the
Minecraft atomic-swap proof payload through
`TransferableValueExchangePayloadConstruction.constructAtomicSwap(...)`.

The Fabric-facing exchange service was updated only as needed to match the
published `1.0.1` Core/Runtime request surface, which now centers on
`ExchangePayload` rather than the older metadata-bearing `ExchangeRequest`.

## Rationale

The earlier sharp-path proof existed because the consumed `1.0.0` TVAL
authority artifact did not publish the public construction package needed to
author an atomic swap through the authoritative substrate surface.

That publication gap is resolved in `1.0.1`, so continuing to hand-author TVAL
truth and mutation requirement bodies locally would add avoidable coupling to
authority-owned construction details.

## Scope

Included:

- updating the centralized substrate version to `1.0.1`
- verifying local artifact availability against `mavenLocal`
- migrating the Minecraft TVAL end-to-end proof to paved payload construction
- making the minimal Fabric compile-surface adjustments required by the
  published `1.0.1` Core/Runtime API
- refreshing narrow contract/bootstrap documentation that became stale

Excluded:

- gameplay behavior
- new Fabric features
- TVAL semantic changes
- binding-owned law changes
- local reimplementation of TVAL construction or request law

## Verification

The migrated proof remains responsible for:

- Core denial on insufficient quantity
- Runtime success on a valid swap
- Runtime failure after post-approval drift without fake success

Verification commands and outcomes are recorded with this change.

## Uncertainties

No local workaround was required for the existing three proof goals.

Deferred:

- any broader adapter-facing request surface above `ExchangePayload`
- any new decision about making `bindingId` execution-critical or removing it
- any proof expansion beyond the existing Minecraft inventory atomic-swap goals
