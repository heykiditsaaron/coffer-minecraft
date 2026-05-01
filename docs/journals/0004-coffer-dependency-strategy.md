# Coffer Dependency Strategy - Composite Build for Development

## 1. Discovered Coordinates

- `dev.coffer:coffer-core:1.0.0`
- `dev.coffer:coffer-runtime:1.0.0`
- `dev.coffer:coffer-transferable-value-authority:1.0.0`

## 2. Current Local Publication State

Core and Runtime are currently published to Maven local.

TransferableValueAuthority is not currently found in Maven local.

## 3. Decision

Use a Gradle composite build with `includeBuild('../coffer')` for local development.

## 4. Rationale

- Avoids stale Maven local artifacts.
- Allows `coffer-minecraft` to compile against current sibling repo source.
- Preserves repository separation while enabling active co-development.
- Avoids flat project references across repositories.

## 5. Deferred

- Maven local or published artifact workflow for release.
- Artifact publication verification.
- Exact release versioning strategy.

## 6. Next Step

Apply composite build configuration and add inventory binding dependencies after this decision is recorded.
