# Minecraft Item Descriptor Implemented

## Summary

`MinecraftItemDescriptor` is implemented as the first Java surface for `bindings/inventory`.

It implements TransferableValueAuthority's `TransferableValueDescriptor` and stores binding-owned Minecraft identity data:

- Item id as a string.
- Positive quantity exposed through `quantity()`.
- Optional full NBT payload as a string.

## Identity

Identity remains binding-owned. The platform-neutral Coffer domain receives the quantity contract and carries descriptor data without interpreting Minecraft item ids or NBT.

## NBT Handling

NBT is preserved exactly as provided.

The descriptor does not parse, normalize, split, or validate NBT structure yet. Blank NBT payloads are rejected when present.

Display text is not modeled as a descriptor field or behavior.

## Verification

`gradle :bindings:inventory:test` succeeded.

The task compiled the descriptor and ran focused unit tests for validation, quantity exposure, exact NBT preservation, absent NBT, blank NBT rejection, and absence of display-text behavior.

## Next

Container implementation remains next.

## Uncertainties

- Final NBT serialization format may change from string to another opaque representation.
- Future Minecraft versions may require component-aware descriptors.
- Equality behavior between descriptors is not specialized yet beyond object identity and explicit field access.
