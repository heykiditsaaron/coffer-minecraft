# Fabric Platform

`platforms/fabric` is the reserved home for Fabric-specific platform work.

It exists for Fabric lifecycle integration, server-thread execution boundaries,
Minecraft/Fabric resolution concerns, and the Fabric-facing execution surface.

During re-foundation, existing source under `src/` is preserved as
migration/reference material. New implementation should stay deferred until the
repository boundary and dependency baseline is intentionally resumed.

This directory must not take on:

- Minecraft inventory binding semantics
- Core arbitration logic
- Runtime orchestration logic
- gameplay behavior

