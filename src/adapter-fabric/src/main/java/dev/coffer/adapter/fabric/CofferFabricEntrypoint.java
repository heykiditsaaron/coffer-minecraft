package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT (PHASE 3.A / 3.C).
 *
 * - Server-side only.
 * - Creates the single adapter runtime door.
 * - Registers diagnostic commands.
 *
 * No Core evaluation occurs here.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            // Phase 3.A: Bootstrap only.
            // Phase 3.C: Register diagnostic command surface.
            CofferCommandRegistrar.register();

            runtime.markReady();
        } catch (Throwable t) {
            runtime.markFailed(CofferFabricRefusal.of(
                    "ADAPTER_BOOT_FAILURE",
                    "Coffer failed to initialize (server entrypoint)."
            ));
        }
    }
}
