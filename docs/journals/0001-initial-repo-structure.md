# 0001: Initial Repository Structure

The official `coffer-minecraft` repository starts from a clean scaffold focused on Minecraft inventory binding work.

Initial structure:

- `docs/architecture` for repository and system boundary documentation.
- `docs/decisions` for durable design decisions.
- `docs/journals` for chronological implementation notes.
- `bindings/inventory` for Minecraft inventory binding work.

Boundary rationale:

- `coffer` owns platform-neutral Core, Runtime, and TransferableValueAuthority behavior.
- `coffer-minecraft` owns Minecraft-specific binding, adapter, and platform integration code.
- This repository will consume Coffer artifacts after coordinates and APIs are confirmed.

No player-trade adapter, Fabric platform glue, Minecraft dependencies, or Coffer artifact dependencies are included in this initial scaffold.
