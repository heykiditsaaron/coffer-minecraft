package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import dev.coffer.adapter.fabric.command.SellCommandRegistrar;
import dev.coffer.adapter.fabric.command.ShopCommandRegistrar;
import dev.coffer.adapter.fabric.config.ValuationConfig;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricValuationService;
import dev.coffer.core.CoreEngine;
import net.fabricmc.api.DedicatedServerModInitializer;

import java.util.List;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT
 *
 * Responsibility:
 * - Wire Core with adapter-owned valuation and audit sink.
 * - Register command entry points.
 * - Drive the adapter runtime through READY/FAILED states.
 *
 * Not responsible for:
 * - Policy logic (none configured yet).
 * - Valuation config loading (uses empty deny-by-default config).
 * - Any economic meaning beyond wiring.
 *
 * Invariants:
 * - Zero-config boots; valuation denies by default.
 * - Runtime enters READY only after wiring succeeds.
 * - Adapter failure is surfaced explicitly.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            var auditSink = new FabricAuditSink();
            var valuationService = new FabricValuationService(ValuationConfig.empty());

            CoreEngine coreEngine = new CoreEngine(
                    /* policyLayers */ List.of(),
                    valuationService,
                    auditSink
            );

            FabricCoreExecutor executor = new FabricCoreExecutor(coreEngine);

            CofferCommandRegistrar.register();
            ShopCommandRegistrar.register();
            SellCommandRegistrar.register(executor);

            runtime.markReady();
        } catch (Throwable t) {
            runtime.markFailed(
                    CofferFabricRefusal.of(
                            "ADAPTER_BOOT_FAILURE",
                            "Coffer failed to initialize economic execution."
                    )
            );
        }
    }
}
