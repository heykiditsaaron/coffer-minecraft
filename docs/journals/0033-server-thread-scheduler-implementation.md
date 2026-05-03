# Server-Thread Scheduler Implementation

## Summary

`CofferMinecraftFabricService` now has an internal scheduled exchange entry.

The scheduled method returns `CompletableFuture<Object>` and preserves the existing internal result shape:

- Core denial returns `Outcome`
- approved runtime execution returns `ExecutionResult`
- platform scheduling unavailability returns an internal platform unavailable result

## Scheduling Behavior

If no Minecraft server is attached, the method completes immediately with an internal unavailable result and does not throw.

If the caller is already on the Minecraft server thread, the method runs the existing `executeExchange(...)` path immediately and completes the future with that result.

If the caller is off-thread, the method uses the attached `MinecraftServer` execution queue to run `executeExchange(...)` on the server thread. The calling thread is not blocked, and no custom thread pool is introduced.

## Failure and Unknown Handling

Server absence and scheduling failure complete with an internal unavailable result. This avoids reporting success or inventing a deterministic inventory failure when platform execution cannot safely occur.

If `executeExchange(...)` throws while running through the scheduled entry, the returned future is completed exceptionally. The existing `executeExchange(...)` logic is unchanged and remains responsible for preserving Core `Outcome` vs Runtime `ExecutionResult` behavior.

If the attached server changes before deferred work runs, the future completes with the internal unavailable result instead of running against stale platform state.

## Boundaries

No gameplay adapter, command/UI surface, retry/rollback behavior, inventory logic, binding behavior, descriptor interpretation, or container logic was changed.

## Verification

Verified with:

```text
PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle :platforms:fabric:compileJava
```

The build passed.

## Uncertainties

- The internal unavailable result is temporary and should be replaced by a stronger adapter-facing result wrapper later.
- Future adapters still need a decision on blocking vs async consumption.
