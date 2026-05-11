# Inventory Binding

`bindings/inventory` is the repository area for Minecraft inventory binding
semantics.

It is the place for Minecraft-specific descriptor mapping, matching, container
interpretation, simulation, and mutation behavior.

During re-foundation, existing source under `src/` is preserved as
migration/reference material. Do not treat it as permission to duplicate
substrate authority behavior locally.

This directory must not take on:

- Fabric lifecycle work
- server-thread scheduling
- gameplay workflow logic
- platform-agnostic authority behavior

