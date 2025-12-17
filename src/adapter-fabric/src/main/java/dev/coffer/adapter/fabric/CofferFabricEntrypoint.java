package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import dev.coffer.adapter.fabric.command.ShopCommandRegistrar;
import dev.coffer.adapter.fabric.command.SellCommandRegistrar;
import dev.coffer.core.CoreEngine;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT (PHASE 3B).
 *
 * Fabric now executes real economic decisions.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            CoreEngine coreEngine = new CoreEngine();

            CofferCommandRegistrar.register();
            ShopCommandRegistrar.register();
            SellCommandRegistrar.register(coreEngine);

            runtime.markReady();
        } catch (Throwable t) {
            runtime.markFailed(CofferFabricRefusal.of(
                    "ADAPTER_BOOT_FAILURE",
                    "Coffer failed to initialize economic execution."
            ));
        }
    }
}
