# Minecraft TVAL Construction Surface Proof

## Summary

Audit the Minecraft inventory TransferableValue end-to-end proof against the
substrate TVAL construction surface and preserve the existing sharp-path test
until the consumed substrate artifact exposes that surface.

## Decision

`MinecraftTransferableValueEndToEndTest` remains on direct sharp-path request
construction for now.

The repository contract now states that normal alignment should use
`TransferableValueExchangePayloadConstruction.constructAtomicSwap(...)` once
the consumed substrate dependency exposes it.

## Rationale

The current test manually authors `ExchangeRequest` shape, TVAL mutation
requirements, and TVAL authority-defined requirement bodies. That sharp path is
not the authoritative paved integration path for current TVAL alignment.

An attempted refactor against the authoritative surface could not compile in
this repository because the resolved `dev.coffer:*:1.0.0` Maven artifacts do
not expose the construction-surface classes or the newer payload model types
seen in the sibling substrate source tree.

Preserving the existing test is safer than inventing a local workaround or
coupling this repository to unpublished substrate source.

## Scope

Included:

- verifying the current proof shape against the authoritative substrate surface
- documenting that coffer-minecraft should consume the TVAL construction surface
  once the dependency surface is available

Excluded:

- gameplay behavior
- Fabric feature work
- TVAL semantic changes
- fake inventory migration
- substrate artifact publication or cross-repository dependency rewiring
- changes to `bindingId` behavior

## Verification

The preserved proof remains responsible for:

- Core denial on insufficient quantity
- Runtime success on a valid swap
- Runtime failure after post-approval drift without reporting fake success

Verification commands were run after restoring the compilable sharp-path test.

## Uncertainties

The construction surface appears semantically sufficient for this proof shape,
but it is not yet available through the substrate artifacts consumed by this
repository.

Deferred:

- migrating the Minecraft proof to the paved construction surface after
  substrate publication or dependency alignment
- any broader adapter-facing helper surface beyond the substrate construction
  API
- any separate decision about making `bindingId` execution-critical or removing
  it
