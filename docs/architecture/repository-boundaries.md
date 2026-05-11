# Repository Boundaries

The authoritative substrate lives outside this repository.

Substrate ownership belongs to separate repositories, including:

- `coffer-core`
- `coffer-runtime`
- `coffer-transferable-value-authority`

`coffer-minecraft` owns Minecraft-specific platform work only.

That includes:

- Minecraft inventory binding semantics
- Fabric-specific platform integration
- Minecraft-specific contracts and integration documentation
- Minecraft-specific tests and fixtures

This repository does not own:

- Core arbitration behavior
- Runtime execution orchestration
- TransferableValue authority behavior
- platform-agnostic architecture
- generic adapter SDK abstractions

No platform-neutral Coffer logic should be reimplemented here. This repository
must consume substrate artifacts or included builds once the split-repository
dependency wiring is finalized.
