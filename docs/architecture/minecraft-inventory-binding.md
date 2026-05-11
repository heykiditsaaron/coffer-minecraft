# Minecraft Inventory Binding

The Minecraft inventory binding adapts Minecraft inventory semantics to the
TransferableValue authority ports consumed from substrate artifacts.

The current implementation is candidate living architecture, not preserved
migration residue. It already carries the effective Minecraft-specific semantics
for:

- descriptor identity and quantity reconstruction
- exact item and NBT equivalence
- player inventory container boundaries
- runtime payload interpretation
- runtime value-set reconstruction
- atomic swap simulation and application

The binding should describe Minecraft inventory state and operations in terms
that TransferableValue authority can evaluate and execute. It must not implement
platform-neutral authority behavior itself.

`bindingId` is currently a runtime payload compatibility check, not part of
descriptor identity and not a separate execution-routing mechanism inside the
binding. The present behavior is:

- runtime payload generation includes `bindingId`
- runtime payload interpretation rejects a mismatched `bindingId`
- runtime payload interpretation accepts missing `bindingId`
- container resolution and value reconstruction do not derive semantics from
  `bindingId`

That behavior is intentionally documented rather than changed in this step.
Making `bindingId` execution-critical or removing it would require a separate
behavioral decision and new proof.

See [inventory-binding.md](/home/aaron/dev/coffer-minecraft/docs/contracts/inventory-binding.md)
for the current contract.
