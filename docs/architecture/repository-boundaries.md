# Repository Boundaries

The platform-neutral `coffer` repository owns Coffer Core, Coffer Runtime, and the first-party TransferableValueAuthority module.

The `coffer-minecraft` repository owns Minecraft-specific bindings, gameplay adapters, and platform glue. This includes adapting Minecraft inventory and interaction semantics to the platform-neutral authority and runtime contracts provided by `coffer`.

No platform-neutral Coffer logic should be reimplemented in this repository. This repository should consume published or local artifacts from `coffer` once artifact coordinates and API contracts are confirmed.
