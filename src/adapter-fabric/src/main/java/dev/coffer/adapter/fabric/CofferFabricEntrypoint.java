package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import dev.coffer.adapter.fabric.command.ShopCommandRegistrar;
import dev.coffer.adapter.fabric.command.SellCommandRegistrar;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricPolicyAllowAll;
import dev.coffer.adapter.fabric.execution.FabricValuationServiceStub;
import dev.coffer.core.CoreEngine;
import dev.coffer.core.PolicyLayer;
import dev.coffer.core.ValuationService;
import net.fabricmc.api.DedicatedServerModInitializer;

import java.util.List;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT (PHASE 3B)
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            List<PolicyLayer> policyLayers = List.of(new FabricPolicyAllowAll());
            ValuationService valuationService = new FabricValuationServiceStub();
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
