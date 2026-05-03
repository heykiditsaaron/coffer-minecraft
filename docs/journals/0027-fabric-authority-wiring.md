# Fabric Authority Wiring

## Summary

The Fabric platform service now constructs the Coffer Runtime and TransferableValueAuthority wrappers during service construction.

## Core Wiring

`CofferCore` remains a static arbitration entrypoint in the current Coffer API. The platform service therefore stores the registered core authority resolver that future execution-surface code can pass to `CofferCore.arbitrate(...)`.

The resolver currently registers the Minecraft-backed `TransferableValueCoreAuthority`.

## Runtime Wiring

The platform service constructs:

- `CofferRuntime`
- `TransferableValueRuntimeAuthority`
- a runtime authority collection containing the TransferableValue runtime authority

Future execution-surface code can pass that runtime authority collection to `CofferRuntime.execute(...)`.

## Minecraft Collaborators

The TransferableValue authorities are wired with:

- `MinecraftDescriptorFactory`
- `MinecraftContainerResolver`
- `MinecraftRuntimeValueSetResolver`
- `MinecraftRuntimePayloadFactory`
- `MinecraftRuntimePayloadInterpreter`

Player inventory lookup remains unresolved for now. The resolver is constructed with a placeholder lookup that returns unavailable state rather than resolving live players.

## Boundaries

No execution surface, player inventory resolution, exchange execution, threading scheduler, adapter logic, command, UI, persistence, rollback, retry behavior, or binding logic change was added.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- `CofferCore` is static-only, so platform registration is currently represented by a stored `AuthorityResolver`, not by a constructed `CofferCore` instance.
- Future adapter discovery of the platform service remains undecided.
- Server-thread execution remains future work before live inventory access.
