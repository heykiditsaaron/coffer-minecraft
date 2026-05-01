# Minecraft Inventory Binding

The Minecraft inventory binding adapts Minecraft inventory semantics to the port interfaces exposed by TransferableValueAuthority.

Expected responsibilities include:

- Minecraft stack descriptor mapping.
- Container resolution.
- Storage slot boundaries.
- Native item equivalence.
- Simulation and application behavior.
- Reason codes for rejected, impossible, or no-op operations.

The binding should describe Minecraft inventory state and operations in terms TransferableValueAuthority can evaluate and execute. It should not implement platform-neutral authority behavior itself.

Fabric loader glue is deferred. Loader-specific lifecycle, registration, commands, screens, and runtime integration should be introduced later under a platform-specific module.
