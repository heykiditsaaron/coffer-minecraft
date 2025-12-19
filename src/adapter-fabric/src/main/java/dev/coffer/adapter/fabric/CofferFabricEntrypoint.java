package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import dev.coffer.adapter.fabric.command.ShopCommandRegistrar;
import dev.coffer.adapter.fabric.command.SellCommandRegistrar;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricPolicyAllowAll;
import dev.coffer.adapter.fabric.execution.FabricValuationServicePillar1Test;
import dev.coffer.core.CoreEngine;
import dev.coffer.core.PolicyLayer;
import dev.coffer.core.ValuationService;
import net.fabricmc.api.DedicatedServerModInitializer;

import java.util.List;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT (PHASE 3B/3D)
 *
 * NOTE (PHASE 3D PILLAR 1):
 * This entrypoint temporarily installs a test-only valuation service
 * so that the execution boundary can be reached without inventing mutation.
 *
 * This is scaffold code and MUST be removed after Pillar 1 is proven.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            List<PolicyLayer> policyLayers = List.of(new FabricPolicyAllowAll());

            // PHASE 3D PILLAR 1 (TEMPORARY):
            // Provide a test-only valuation that allows minecraft:dirt to be valued.
            // This is required to reach the mutation execution boundary.
            ValuationService valuationService = new FabricValuationServicePillar1Test();

            FabricAuditSink auditSink = new FabricAuditSink();

            CoreEngine coreEngine =
                    new CoreEngine(policyLayers, valuationService, auditSink);

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
