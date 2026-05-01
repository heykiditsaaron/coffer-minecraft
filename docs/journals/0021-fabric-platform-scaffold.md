# Fabric Platform Scaffold

## Summary

The `platforms/fabric` module shell has been added for future Fabric lifecycle and runtime wiring work.

## Structure

The scaffold includes:

- `platforms/fabric/build.gradle`
- `platforms/fabric/src/main/java`
- `platforms/fabric/src/main/resources`
- `platforms/fabric/src/test/java`

## Dependencies

`platforms/fabric` depends on `:bindings:inventory`.

No Fabric Loader or Fabric API dependency was added yet because the scaffold contains no lifecycle entrypoint, runtime registration, player resolver, or server-thread scheduler code.

## Boundaries

No runtime lifecycle or authority wiring has been implemented.

No player inventory resolver has been implemented.

No player-trade adapter or gameplay workflow has been scaffolded.

## Verification

`gradle :platforms:fabric:compileJava` passed.

## Uncertainties

- Exact Fabric lifecycle hooks.
- Whether platform code should use Fabric Loader only or Fabric API as well.
- Server-thread scheduling strategy.
- Real player inventory resolution policy for offline or unavailable players.
