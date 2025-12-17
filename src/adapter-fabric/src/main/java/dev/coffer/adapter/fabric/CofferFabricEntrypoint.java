package dev.coffer.adapter.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT (PHASE 3.A).
 *
 * - Server-side only.
 * - Creates the single adapter runtime door.
 * - Establishes explicit operational state transitions.
 *
 * No commands are registered here.
 * No UI is opened here.
 * No Core evaluation occurs here.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            // Phase 3.A: Bootstrap only.
            // Future phases will wire Core construction and adapter boundary services here.
            runtime.markReady();
        } catch (Throwable t) {
            // Explicit refusal, no silent failure.
            runtime.markFailed(CofferFabricRefusal.of(
                    "ADAPTER_BOOT_FAILURE",
                    "Coffer failed to initialize (server entrypoint)."
            ));
        }
    }
}
