# Inventory Binding

`bindings/inventory` is the repository area for Minecraft inventory binding
semantics.

It is the place for Minecraft-specific descriptor mapping, matching, container
interpretation, simulation, and mutation behavior.

The current source under `src/` is candidate living architecture for
Minecraft-specific inventory binding semantics. It should be treated as current
binding implementation subject to explicit documentation and narrowly scoped
semantic stabilization. It is not permission to duplicate substrate authority
behavior locally.

This directory must not take on:

- Fabric lifecycle work
- server-thread scheduling
- gameplay workflow logic
- platform-agnostic authority behavior
