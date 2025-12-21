# Copilot / AI Agent Instructions — Coffer

Short, actionable guidance to help an AI contributor be productive in this repository.

1. Big picture
- Coffer is a policy-first commerce substrate for Minecraft. Core semantics live under `src/common/core` (the sovereign authority). Adapters under `src/adapter-*` translate platform events into Core calls. Platform abstractions live under `src/common/platform`.
- Data flow: Adapter (gather inputs) → Platform/bridge types (`src/common/platform`) → Core (evaluate policy & valuation) → Adapter (express results / apply consequences).

2. Where to edit
- Policy, valuation, mutation rules: only in `src/common/core`. Core is intentionally dependency-free (see `src/common/core/build.gradle`).
- Adapter translation code: put in `src/adapter-*` (example: `src/adapter-fabric`). Adapters must not implement policy or mutate authoritative rules.
- Platform abstractions/interfaces: `src/common/platform`.

3. Build & run (developer workflows)
- Use the Gradle wrapper in repo root. Unix/mac: `./gradlew <task>`. Windows: `gradlew.bat <task>`.
- Build the Fabric adapter: `./gradlew :src:adapter-fabric:build` (Windows: `gradlew.bat :src:adapter-fabric:build`).
- Run a development server (loom run task): `./gradlew :src:adapter-fabric:runServer`. This uses the `src/adapter-fabric/run/` folder as the devserver workspace; do not commit changes from this runtime folder unless intentional.
- Java toolchain: project targets Java 21 (see adapter and core `build.gradle`).

4. Project-specific conventions (do not assume usual defaults)
- Structural authority: placement of code determines responsibility. If logic belongs to policy/valuation it must move to `src/common/core` (see `src/STRUCTURE.md`).
- Core must remain dependency-free. If you need to add a library, justify it in the repo docs and avoid adding runtime dependencies to `src/common/core`.
- Adapters may use Fabric/Loom-specific APIs; core code must remain plain Java library code.

5. Integration points & external deps
- Fabric/Loom is used for `adapter-fabric` (see `src/adapter-fabric/build.gradle`). Minecraft version: 1.21.1; Fabric Loader and Fabric API versions are declared there.
- The devserver assets and world lives under `src/adapter-fabric/run/` (configs, `world/`, `server.properties`). Treat those as ephemeral dev resources.

6. How to make safe changes (advice for automated edits)
- If changing policy/valuation: modify `src/common/core`, add unit tests, and update the appropriate Chronicle or docs under `docs/chronicle/` explaining the intent.
- If changing adapter behavior: limit changes to `src/adapter-*` and `src/common/platform`. Ensure adapter code only translates inputs/outputs and delegates decisions to Core.
- When relocating logic from adapter→core, also update `docs/adapter-contract.md` and `src/STRUCTURE.md` if authority boundaries are affected.

7. Useful files to consult
- Authority and layout: `src/STRUCTURE.md`
- Core semantics and rules: `src/common/core/`
- Adapter example and dev server: `src/adapter-fabric/` and `src/adapter-fabric/run/`
- Design docs and history: `docs/` (especially `chronicle/` and `adapter-contract.md`).

8. Asking for clarification (how agents should request human input)
- For changes that alter core semantics, create a short PR with: diff, unit tests, and a one-paragraph rationale referencing `docs/chronicle/` or `adapter-contract.md`. If unsure, ask a human reviewer before merging.

If any section is unclear or you want more examples (e.g. sample PRs, test commands), say which area and I will expand.
