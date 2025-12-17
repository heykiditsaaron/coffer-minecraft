package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import dev.coffer.adapter.fabric.command.ShopCommandRegistrar;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT (PHASE 3.A / 3.C / 3.D).
 *
 * - Server-side only.
 * - Creates the single adapter runtime door.
 * - Registers diagnostic command surfaces.
 *
 * No Core evaluation occurs here.
 * No UI is opened here.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            // Phase 3.C: Diagnostic base command
            CofferCommandRegistrar.register();

            // Phase 3.D: Diagnostic shop access command
            ShopCommandRegistrar.register();

            runtime.markReady();
        } catch (Throwable t) {
            runtime.markFailed(CofferFabricRefusal.of(
                    "ADAPTER_BOOT_FAILURE",
                    "Coffer failed to initialize (server entrypoint)."
            ));
        }
    }
}
