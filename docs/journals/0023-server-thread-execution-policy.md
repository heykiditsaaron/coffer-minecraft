# Server-Thread Execution Policy

## Core Rule

All Minecraft inventory mutation must occur on the Minecraft server thread.

Simulation should also run on the server thread unless a future platform layer can prove that the required inventory reads are stable and thread-safe. The default policy is server-thread execution for both simulation and application.

## Invocation Model

Coffer execution requests enter the Fabric layer through platform/runtime services exposed to gameplay code or future adapters.

Callers may originate on or off the server thread. The Fabric layer is responsible for normalizing execution onto the server thread before touching live player inventory.

## Scheduling Policy

If called on the server thread, execute immediately.

If called off-thread, schedule the work onto the server thread.

Scheduled Coffer inventory execution must be serialized through the server thread. The platform layer must not run multiple live inventory executions concurrently against Minecraft player inventories.

## Simulation vs Application

Simulation runs on the server thread by default.

Application also runs on the server thread.

For runtime execution, simulation and application should be performed in the same scheduled server-thread task when possible, reducing the window for inventory drift between feasibility and mutation.

## Drift Handling

Server-thread execution reduces cross-thread drift but does not eliminate all drift.

Other server-thread systems may still mutate inventories before the scheduled task runs, or between Core approval and Runtime execution. Existing binding behavior remains authoritative: if state differs at application time, the result must be non-success rather than assumed successful.

No stronger atomicity guarantee exists yet.

## Failure vs Unknown

A request that cannot be scheduled or cannot safely access the server thread should produce unknown/unavailable semantics.

Known inventory constraints discovered on the server thread, such as missing items or no space, remain failures reported by the binding.

Thread violations should not be reported as ordinary inventory failures.

## Explicit Non-Goals

- No rollback design.
- No transaction system.
- No custom concurrency primitives beyond server-thread enforcement.
- No adapter-specific execution workflow.

## Uncertainties

- Exact Fabric/Minecraft scheduling APIs.
- Whether off-thread callers should block for completion or receive an async result.
- How future adapters expose or consume scheduled execution.
- Whether additional server-thread guards are needed around Core approval, not only Runtime execution.
