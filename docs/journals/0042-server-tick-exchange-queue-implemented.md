## Summary

Fabric exchange scheduling now uses a coffer-minecraft-owned pending exchange queue.

## Decision

`submitExchange` snapshots the attached server, returns `Unavailable` when none is attached,
and otherwise enqueues a pending exchange for `ServerTickEvents.END_SERVER_TICK`.
The Fabric entrypoint registers the service drain method with the end-server-tick event.

## Rationale

The previous path depended on `MinecraftServer.isOnThread()` and `MinecraftServer.execute()`.
Live evidence showed `isOnThread()` could fail to return before exchange execution, leaving
the adapter future incomplete. Draining from Fabric's server tick event gives this repository
an explicit execution point already owned by the Minecraft server thread.

## Scope

Included:
- Pending exchange queue in the Fabric service.
- End-server-tick drain registration in the Fabric entrypoint.
- Completion of queued exchanges as unavailable on detach or shutdown.
- Removal of `isOnThread()` checks from exchange execution and inventory resolution.

Excluded:
- Adapter changes.
- Schema changes.
- Retry or rollback behavior.

## Verification

`PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:test :platforms:fabric:compileJava`
completed successfully. `:platforms:fabric:test` had no test sources; `:platforms:fabric:compileJava`
compiled successfully.

## Uncertainties

Live validation is still required to confirm the adapter session leaves `SUBMITTED` after
the next server tick.
